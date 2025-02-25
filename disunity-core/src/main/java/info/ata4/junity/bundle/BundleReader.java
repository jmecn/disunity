/*
 ** 2014 September 25
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.junity.bundle;

import info.ata4.io.DataReader;
import info.ata4.io.DataReaders;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Path;

import static info.ata4.junity.Const.*;
import static java.nio.file.StandardOpenOption.READ;

import java.util.Comparator;
import java.util.List;

import info.ata4.util.lz4.LZ4JavaSafeFastDecompressor;
import net.contrapunctus.lzma.LzmaInputStream;
import org.apache.commons.io.input.BoundedInputStream;
import org.apache.commons.io.input.CountingInputStream;

/**
 * Streaming reader for Unity asset bundles.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class BundleReader implements Closeable {

    private final DataReader in;
    private Bundle bundle;
    private CountingInputStream lzma;
    private boolean closed;

    public BundleReader(Path file) throws IOException {
        in = DataReaders.forFile(file, READ);
    }

    public Bundle read() throws IOException {
        bundle = new Bundle();

        in.position(0);

        BundleHeader header = bundle.header();
        in.readStruct(header);

        // check signature
        if (!header.hasValidSignature()) {
            throw new BundleException("Invalid signature");
        }

        List<StreamingInfo> entryInfos = bundle.entryInfos();
        if (header.compressedDataHeaderSize() > 0) {
            // UnityFS stream type 6 header

            if (header.dataHeaderAtEndOfFile()) {
                in.position(header.completeFileSize() - header.compressedDataHeaderSize());
            }

            // build an input stream for the uncompressed data header
            InputStream headerIn = new BoundedInputStream(in.stream(), header.compressedDataHeaderSize());
            DataReader inData;
            int compressionType = header.getFlags() & COMPRESSION_TYPE_MASK;
            switch(compressionType) {
                case COMPRESSION_TYPE_NONE:
                    // Not compressed
                    inData = DataReaders.forInputStream(headerIn);
                    break;
                case COMPRESSION_TYPE_LZMA:
                    // lzma
                    inData = DataReaders.forInputStream(new CountingInputStream(new LzmaInputStream(headerIn)));
                    break;
                case COMPRESSION_TYPE_LZ4:// LZ4
                case COMPRESSION_TYPE_LZ4HC:// lz4hc
                    // LZ4
                    byte[] compressed = new byte[header.compressedDataHeaderSize()];
                    byte[] decompressed = new byte[(int)header.dataHeaderSize()];
                    headerIn.read(compressed);
                    LZ4JavaSafeFastDecompressor.INSTANCE.decompress(compressed, decompressed);
                    inData = DataReaders.forByteBuffer(ByteBuffer.wrap(decompressed));
                    break;
                default:
                    throw new BundleException("Unsupported data header compression scheme: " + compressionType);
            }

            // Block info: not captured for now
            {
                // 16 bytes unknown
                byte[] unknown = new byte[16];
                inData.readBytes(unknown);

                int storageBlocks = inData.readInt();
                for (int i = 0; i < storageBlocks; ++i) {
                    inData.readUnsignedInt();
                    inData.readUnsignedInt();
                    inData.readUnsignedShort();
                }
            }

            int files = inData.readInt();

            for (int i = 0; i < files; i++) {
                StreamingInfo entryInfo = new StreamingInfoFS();
                inData.readStruct(entryInfo);
                entryInfos.add(entryInfo);
            }
        } else {
            // raw or web header
            long dataHeaderSize = header.dataHeaderSize();
            if (dataHeaderSize == 0) {
                // old stream versions don't store the data header size, so use a large
                // fixed number instead
                dataHeaderSize = 4096;
            }

            InputStream is = dataInputStream(0, dataHeaderSize);
            DataReader inData = DataReaders.forInputStream(is);
            int files = inData.readInt();

            for (int i = 0; i < files; i++) {
                StreamingInfo entryInfo = new StreamingInfo();
                inData.readStruct(entryInfo);
                entryInfos.add(entryInfo);
            }
        }

        // sort entries by offset so that they're in the order in which they
        // appear in the file, which is convenient for compressed bundles
        entryInfos.sort(Comparator.comparingLong(StreamingInfo::getOffset));

        List<BundleEntry> entries = bundle.entries();
        entryInfos.forEach(entryInfo -> entries.add(new BundleInternalEntry(entryInfo, this::inputStreamForEntry)));

        return bundle;
    }

    private InputStream dataInputStream(long offset, long size) throws IOException {
        InputStream is;

        // use LZMA stream if the bundle is compressed
        if (bundle.header().compressed()) {
            // create initial input stream if required
            if (lzma == null) {
                lzma = lzmaInputStream();
            }

            // recreate stream if the offset is behind
            long lzmaOffset = lzma.getByteCount();
            if (lzmaOffset > offset) {
                lzma.close();
                lzma = lzmaInputStream();
            }

            // skip forward if required
            if (lzmaOffset < offset) {
                lzma.skip(offset - lzmaOffset);
            }

            is = lzma;
        } else {
            in.position(bundle.header().headerSize() + offset);
            is = in.stream();
        }

        return new BoundedInputStream(is, size);
    }

    private CountingInputStream lzmaInputStream() throws IOException {
        in.position(bundle.header().headerSize());
        return new CountingInputStream(new LzmaInputStream(in.stream()));
    }

    private InputStream inputStreamForEntry(StreamingInfo info) throws IOException {
        if (closed) {
            throw new BundleException("Bundle reader is closed");
        }
        return dataInputStream(info.getOffset(), info.getSize());
    }

    @Override
    public void close() throws IOException {
        closed = true;
        if (lzma != null) {
            lzma.close();
        }
        in.close();
    }
}
