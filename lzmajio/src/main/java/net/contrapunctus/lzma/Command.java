// Command.java -- 
// Copyright (c)2008 Christopher League <league@contrapunctus.net>

// This is free software, but it comes with ABSOLUTELY NO WARRANTY.
// GNU Lesser General Public License 2.1 or Common Public License 1.0

package net.contrapunctus.lzma;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Command
{
    static void copy(InputStream in, OutputStream out) throws IOException
    {
        final int BUFSIZE = 4096;
        byte[] buf = new byte[BUFSIZE];
        int n = in.read(buf);
        while(n != -1) {
            out.write(buf, 0, n);
            n = in.read(buf);
        }
        out.close();
    }

    public static void main(String[] args) throws IOException
    {
        copy(System.in, new LzmaOutputStream(System.out));
    }
}
