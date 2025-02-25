// EncoderThread.java -- run LZMA encoder in a separate thread
// Copyright (c)2007 Christopher League <league@contrapunctus.net>

// This is free software, but it comes with ABSOLUTELY NO WARRANTY.
// GNU Lesser General Public License 2.1 or Common Public License 1.0

package net.contrapunctus.lzma;

import info.ata4.io.lzma.LzmaEncoderProps;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.concurrent.ArrayBlockingQueue;
import lzma.LzmaEncoder;

class EncoderThread extends Thread {

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
    protected LzmaEncoder enc;
    protected LzmaEncoderProps props;
    protected IOException exn;

    EncoderThread(OutputStream _out, LzmaEncoderProps _props) {
        q = ConcurrentBufferOutputStream.newQueue();
        in = ConcurrentBufferInputStream.create(q);
        out = _out;
        enc = new LzmaEncoder();
        exn = null;
        props = _props;
        if (DEBUG) {
            dbg.printf("%s << %s (%s)%n", this, in, q);
        }
    }

    @Override
    public void run() {
        try {
            props.apply(enc);
            props.toOutputStream(out);
            if (DEBUG) {
                dbg.printf("%s begins%n", this);
            }
            enc.code(in, out);
            if (DEBUG) {
                dbg.printf("%s ends%n", this);
            }
            out.close();
        } catch (IOException _exn) {
            exn = _exn;
            if (DEBUG) {
                dbg.printf("%s exception: %s%n", exn.getMessage());
            }
        }
    }
    
    public LzmaEncoderProps getProps() {
        return props;
    }

    @Override
    public String toString() {
        return String.format("Enc@%x", hashCode());
    }
}
