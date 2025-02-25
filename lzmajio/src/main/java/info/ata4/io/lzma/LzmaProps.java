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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import lzma.LzmaState;

/**
 * Abstract LZMA properties class
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class LzmaProps {
    
    protected static final int kDicLogSizeMaxCompress = 29;
    
    private boolean includeProps = true;
    private boolean includeSize = true;
    private int numLiteralContextBits = 3;
    private int numLiteralPosStateBits = 0;
    private int posStateBits = 2;
    private int dictionarySize = 1 << 22;
    private long uncompressedSize = -1;
    
    public boolean isIncludeProps() {
        return includeProps;
    }

    public void setIncludeProps(boolean includeProps) {
        this.includeProps = includeProps;
    }
    
    public boolean isIncludeSize() {
        return includeSize;
    }
    
    public void setIncludeSize(boolean includeSize) {
        this.includeSize = includeSize;
    }
    
    public int getPropSize() {
        int size = 0;
        if (isIncludeSize()) {
            size += 8;
        }
        if (isIncludeProps()) {
            size += 5;
        }
        return size;
    }
    
    public int getNumLiteralContextBits() {
        return numLiteralContextBits;
    }

    public void setNumLiteralContextBits(int lc) {
        if (lc < 0 || lc > LzmaState.kNumLitContextBitsMax) {
            throw new IllegalArgumentException();
        }
        this.numLiteralContextBits = lc;
    }

    public int getNumLiteralPosStateBits() {
        return numLiteralPosStateBits;
    }

    public void setNumLiteralPosStateBits(int lp) {
        if (lp < 0 || lp > LzmaState.kNumLitPosStatesBitsEncodingMax) {
            throw new IllegalArgumentException();
        }
        this.numLiteralPosStateBits = lp;
    }

    public int getPosStateBits() {
        return posStateBits;
    }

    public void setPosStateBits(int pb) {
        if (pb < 0 || pb > LzmaState.kNumPosStatesBitsEncodingMax) {
            throw new IllegalArgumentException();
        }
        this.posStateBits = pb;
    }

    public int getDictionarySize() {
        return dictionarySize;
    }

    public void setDictionarySize(int dictSize) {
        if (dictSize < (1 << LzmaState.kDicLogSizeMin) || dictSize > (1 << kDicLogSizeMaxCompress)) {
            throw new IllegalArgumentException();
        }
        this.dictionarySize = dictSize;
    }
    
    public long getUncompressedSize() {
        return uncompressedSize;
    }
    
    public void setUncompressedSize(long uncompressedSize) {
        this.uncompressedSize = uncompressedSize;
    }
    
    public void fromByteBuffer(ByteBuffer propBuffer) {
        if (propBuffer.limit() != getPropSize()) {
            throw new IllegalArgumentException("Wrong buffer size, expected " + getPropSize() + " bytes");
        }
        
        propBuffer.rewind();
        propBuffer.order(ByteOrder.LITTLE_ENDIAN);
        
        if (isIncludeProps()) {
            byte first = propBuffer.get();
            setNumLiteralContextBits(first % 9);
            int remainder = first / 9;
            setNumLiteralPosStateBits(remainder % 5);
            setPosStateBits(remainder / 5);
            setDictionarySize(propBuffer.getInt());
        }
        
        if (isIncludeSize()) {
            setUncompressedSize(propBuffer.getLong());
        }
    }
    
    public void fromArray(byte[] properties) {
        fromByteBuffer(ByteBuffer.wrap(properties));
    }
    
    public ByteBuffer toByteBuffer() {
        ByteBuffer propBuffer = ByteBuffer.allocate(getPropSize());
        propBuffer.order(ByteOrder.LITTLE_ENDIAN);
        
        if (isIncludeProps()) {
            propBuffer.put((byte) ((getPosStateBits() * 5 + getNumLiteralPosStateBits()) * 9 + getNumLiteralContextBits()));
            propBuffer.putInt(getDictionarySize());
        }
        
        if (isIncludeSize()) {
            propBuffer.putLong(getUncompressedSize());
        }
        
        return propBuffer;
    }
    
    public byte[] toArray() {
        return toByteBuffer().array();
    }

    @Override
    public String toString() {
        return String.format("lc: %d, lp: %d, pb: %d, dictSize: %d",
                getNumLiteralContextBits(), getNumLiteralPosStateBits(),
                getPosStateBits(), getDictionarySize());
    }
}
