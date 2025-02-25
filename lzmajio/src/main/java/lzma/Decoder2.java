package lzma;

import java.io.IOException;
import lzma.rangecoder.RangeDecoder;

class Decoder2 {
    
    private short[] m_Decoders = new short[0x300];

    public void init() {
        RangeDecoder.initBitModels(m_Decoders);
    }

    public byte decodeNormal(RangeDecoder rangeDecoder) throws IOException {
        int symbol = 1;
        do {
            symbol = (symbol << 1) | rangeDecoder.decodeBit(m_Decoders, symbol);
        } while (symbol < 0x100);
        return (byte) symbol;
    }

    public byte decodeWithMatchByte(RangeDecoder rangeDecoder, byte matchByte) throws IOException {
        int symbol = 1;
        do {
            int matchBit = (matchByte >> 7) & 1;
            matchByte <<= 1;
            int bit = rangeDecoder.decodeBit(m_Decoders, ((1 + matchBit) << 8) + symbol);
            symbol = (symbol << 1) | bit;
            if (matchBit != bit) {
                while (symbol < 0x100) {
                    symbol = (symbol << 1) | rangeDecoder.decodeBit(m_Decoders, symbol);
                }
                break;
            }
        } while (symbol < 0x100);
        return (byte) symbol;
    }
    
}
