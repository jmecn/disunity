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
import java.io.OutputStream;
import lzma.LzmaEncoder;
import lzma.LzmaState;

/**
 * LZMA encoder properties class
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class LzmaEncoderProps extends LzmaProps {
    
    private int numFastBytes = 0x20;
    private boolean endMarkerMode = false;

    public int getNumFastBytes() {
        return numFastBytes;
    }

    public void setNumFastBytes(int numFastBytes) {
        if (numFastBytes < 5 || numFastBytes > LzmaState.kMatchMaxLen) {
            throw new IllegalArgumentException();
        }
        this.numFastBytes = numFastBytes;
    }
    
    public boolean isEndMarkerMode() {
        return endMarkerMode;
    }

    public void setEndMarkerMode(boolean endMarkerMode) {
        this.endMarkerMode = endMarkerMode;
    }
    
    public void apply(LzmaEncoder encoder) {
        encoder.setLcLpPb(getNumLiteralContextBits(), getNumLiteralPosStateBits(), getPosStateBits());
        encoder.setDictionarySize(getDictionarySize());
        encoder.setNumFastBytes(getNumFastBytes());
        encoder.setEndMarkerMode(isEndMarkerMode());
    }
    
    public void toOutputStream(OutputStream out) throws IOException {
        out.write(toArray());
    }
}
