package net.contrapunctus.lzma;

import info.ata4.io.lzma.LzmaEncoderProps;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ArrayBlockingQueue;
import net.contrapunctus.lzma.ConcurrentBufferInputStream;
import net.contrapunctus.lzma.ConcurrentBufferOutputStream;
import net.contrapunctus.lzma.DecoderThread;
import net.contrapunctus.lzma.EncoderThread;
import net.contrapunctus.lzma.LzmaInputStream;
import net.contrapunctus.lzma.LzmaOutputStream;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * A very simple set of tests to detect API drift over time.
 * Essentially, we just access all the public classes, methods, and
 * fields and check their types.  Then, any changes that affect the
 * API should cause a compilation error or test failure.
 */
public class ApiDriftTest
{
    /**
     * Access the public API of the {@link LzmaOutputStream} class.
     */
    @Test public void outstreamAPI()
    {
        LzmaOutputStream los;
        los = new LzmaOutputStream(new ByteArrayOutputStream());
        
        LzmaEncoderProps props = new LzmaEncoderProps();
        props.setDictionarySize(1 << 8);
        props.setNumFastBytes(5);
        los = new LzmaOutputStream(new ByteArrayOutputStream(), props);
        assertTrue(los instanceof OutputStream);
    }

    /**
     * Access the public API of the {@link LzmaInputStream} class.
     */
    @Test public void instreamAPI()
    {
        LzmaInputStream lis;
        lis = new LzmaInputStream(new ByteArrayInputStream(new byte[0]));
        assertTrue(lis instanceof InputStream);
    }

    @Test public void entryPoints() throws IOException
    {
        RoundTripTest.main(new String[0]);
        RoundTripTest.main(new String[] {"pom.xml"});
    }

    @Test public void strings()
    {
        ArrayBlockingQueue<byte[]> q =
            ConcurrentBufferOutputStream.newQueue();
        // ConcurrentBufferInputStream
        InputStream is = ConcurrentBufferInputStream.create(q);
        System.out.println(is);
        // ConcurrentBufferOutputStream
        OutputStream os = ConcurrentBufferOutputStream.create(q);
        System.out.println(os);
        // DecoderThread
        Thread th = new DecoderThread(is);
        System.out.println(th);
        // EncoderThread
        LzmaEncoderProps props = new LzmaEncoderProps();
        props.setDictionarySize(1 << 8);
        props.setNumFastBytes(5);
        th = new EncoderThread(os, props);
        System.out.println(th);
        // LzmaInputStream
        is = new LzmaInputStream(is);
        System.out.println(is);
        // LzmaOutputStream
        os = new LzmaOutputStream(os);
        System.out.println(os);
    }
}
