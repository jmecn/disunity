// LzmaInputStream.java -- transparently decompress LZMA while reading
// Copyright (c)2007 Christopher League <league@contrapunctus.net>

// This is free software, but it comes with ABSOLUTELY NO WARRANTY.
// GNU Lesser General Public License 2.1 or Common Public License 1.0

package net.contrapunctus.lzma;

import info.ata4.io.lzma.LzmaDecoderProps;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.PrintStream;

public class LzmaInputStream extends FilterInputStream {

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
    
    private DecoderThread dth;

    public LzmaInputStream(InputStream _in) {
        super(null);
        dth = new DecoderThread(_in);
        in = ConcurrentBufferInputStream.create(dth.q);
        if (DEBUG) {
            dbg.printf("%s << %s (%s)%n", this, in, dth.q);
        }
        dth.start();
    }
    
    public LzmaDecoderProps getProperties() {
        return dth.getProps();
    }

    @Override
    public int read() throws IOException {
        int k = in.read();
        dth.maybeThrow();
        return k;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int k = in.read(b, off, len);
        dth.maybeThrow();
        return k;
    }

    @Override
    public void close() throws IOException {
        if (DEBUG) {
            dbg.printf("%s closed%n", this);
        }
        
        in.close();
        
        dth.interrupt();
        
        if (dth.exn != null) {
            throw dth.exn;
        }
    }

    @Override
    public String toString() {
        return String.format("lzmaIn@%x", hashCode());
    }
}
