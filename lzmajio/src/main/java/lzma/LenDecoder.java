package lzma;

import java.io.IOException;
import lzma.rangecoder.BitTreeDecoder;
import lzma.rangecoder.RangeDecoder;

class LenDecoder {
    
    private short[] m_Choice = new short[2];
    private BitTreeDecoder[] m_LowCoder = new BitTreeDecoder[LzmaState.kNumPosStatesMax];
    private BitTreeDecoder[] m_MidCoder = new BitTreeDecoder[LzmaState.kNumPosStatesMax];
    private BitTreeDecoder m_HighCoder = new BitTreeDecoder(LzmaState.kNumHighLenBits);
    private int m_NumPosStates = 0;

    void create(int numPosStates) {
        for (; m_NumPosStates < numPosStates; m_NumPosStates++) {
            m_LowCoder[m_NumPosStates] = new BitTreeDecoder(LzmaState.kNumLowLenBits);
            m_MidCoder[m_NumPosStates] = new BitTreeDecoder(LzmaState.kNumMidLenBits);
        }
    }

    void init() {
        RangeDecoder.initBitModels(m_Choice);
        for (int posState = 0; posState < m_NumPosStates; posState++) {
            m_LowCoder[posState].init();
            m_MidCoder[posState].init();
        }
        m_HighCoder.init();
    }

    int decode(RangeDecoder rangeDecoder, int posState) throws IOException {
        if (rangeDecoder.decodeBit(m_Choice, 0) == 0) {
            return m_LowCoder[posState].decode(rangeDecoder);
        }
        int symbol = LzmaState.kNumLowLenSymbols;
        if (rangeDecoder.decodeBit(m_Choice, 1) == 0) {
            symbol += m_MidCoder[posState].decode(rangeDecoder);
        } else {
            symbol += LzmaState.kNumMidLenSymbols + m_HighCoder.decode(rangeDecoder);
        }
        return symbol;
    }
    
}
