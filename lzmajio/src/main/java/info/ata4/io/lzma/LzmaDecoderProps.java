/*
 ** 2014 December 13
 **
 ** The author disclaims copyright to this source code. In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.io.lzma;

import java.io.IOException;
import java.io.InputStream;
import lzma.LzmaDecoder;

/**
 * LZMA decoder properties class
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class LzmaDecoderProps extends LzmaProps {
    
    public void apply(LzmaDecoder decoder) {
        decoder.setLcLpPb(getNumLiteralContextBits(), getNumLiteralPosStateBits(), getPosStateBits());
        decoder.setDictionarySize(getDictionarySize());
    }
    
    public void fromInputStream(InputStream in) throws IOException {
        byte[] prop = new byte[getPropSize()];
        if (in.read(prop) != prop.length) {
            throw new IOException("Stream is too short");
        }
        fromArray(prop);
    }
}
