// DecoderThread.java -- run LZMA decoder in a separate thread
// Copyright (c)2007 Christopher League <league@contrapunctus.net>

// This is free software, but it comes with ABSOLUTELY NO WARRANTY.
// GNU Lesser General Public License 2.1 or Common Public License 1.0

package net.contrapunctus.lzma;

import info.ata4.io.lzma.LzmaDecoderProps;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.concurrent.ArrayBlockingQueue;
import lzma.LzmaDecoder;

class DecoderThread extends Thread {

    private static final PrintStream dbg = System.err;
    private static final boolean DEBUG;
    
    static {
        String ds = null;
        try {
            ds = System.getProperty("DEBUG_LzmaCoders");
        } catch (SecurityException e) {
        }
        DEBUG = ds != null;
    }
    
    protected ArrayBlockingQueue<byte[]> q;
    protected InputStream in;
    protected OutputStream out;
    protected LzmaDecoder dec;
    protected LzmaDecoderProps props = new LzmaDecoderProps();
    protected IOException exn;

    DecoderThread(InputStream _in) {
        q = ConcurrentBufferOutputStream.newQueue();
        in = _in;
        out = ConcurrentBufferOutputStream.create(q);
        dec = new LzmaDecoder();
        exn = null;
        if (DEBUG) {
            dbg.printf("%s >> %s (%s)%n", this, out, q);
        }
    }
    
    @Override
    public void run() {
        try {
            try {
                props.fromInputStream(in);
            } catch (IllegalArgumentException ex) {
                throw new IOException("Invalid LZMA properties", ex);
            }
            props.apply(dec);
            if (DEBUG) {
                dbg.printf("%s begins%n", this);
            }
            dec.code(in, out, props.getUncompressedSize());
            if (DEBUG) {
                dbg.printf("%s ends%n", this);
            }
            in.close(); //?
        } catch (IOException _exn) {
            exn = _exn;
            if (DEBUG) {
                dbg.printf("%s exception: %s%n", this, exn.getMessage());
            }
        }
        // close either way, so listener can unblock
        try {
            out.close();
        } catch (IOException _exn) {
        }
    }

    public void maybeThrow() throws IOException {
        if (exn != null) {
            throw exn;
        }
    }
    
    public LzmaDecoderProps getProps() {
        return props;
    }

    @Override
    public String toString() {
        return String.format("Dec@%x", hashCode());
    }
}
