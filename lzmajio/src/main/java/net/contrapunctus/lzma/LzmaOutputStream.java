// LzmaOutputStream.java -- transparently compress LZMA while writing
// Copyright (c)2007 Christopher League <league@contrapunctus.net>

// This is free software, but it comes with ABSOLUTELY NO WARRANTY.
// GNU Lesser General Public License 2.1 or Common Public License 1.0

package net.contrapunctus.lzma;

import info.ata4.io.lzma.LzmaEncoderProps;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class LzmaOutputStream extends FilterOutputStream {

    private static final PrintStream dbg = System.err;
    private static final boolean DEBUG;

    static {
        String ds = null;
        try {
            ds = System.getProperty("DEBUG_LzmaStreams");
        } catch (SecurityException e) {
        }
        DEBUG = ds != null;
    }
    
    private EncoderThread eth;

    public LzmaOutputStream(OutputStream _out) {
        this(_out, new LzmaEncoderProps());
    }

    public LzmaOutputStream(OutputStream _out, LzmaEncoderProps props) {
        super(null);
        eth = new EncoderThread(_out, props);
        out = ConcurrentBufferOutputStream.create(eth.q);
        if (DEBUG) {
            dbg.printf("%s >> %s (%s)%n", this, out, eth.q);
        }
        eth.start();
    }

    @Override
    public void write(int i) throws IOException {
        if (eth.exn != null) {
            throw eth.exn;
        }
        out.write(i);
    }

    @Override
    public void close() throws IOException {
        if (DEBUG) {
            dbg.printf("%s closed%n", this);
        }
        out.close();
        try {
            eth.join();
            if (DEBUG) {
                dbg.printf("%s joined %s%n", this, eth);
            }
        } catch (InterruptedException exn) {
            throw new InterruptedIOException(exn.getMessage());
        }
        if (eth.exn != null) {
            throw eth.exn;
        }
    }

    @Override
    public String toString() {
        return String.format("lzmaOut@%x", hashCode());
    }
}
