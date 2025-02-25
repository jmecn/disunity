package lzma;

import java.io.IOException;
import lzma.rangecoder.BitTreeEncoder;
import lzma.rangecoder.RangeEncoder;

class LenEncoder {
    
    private short[] _choice = new short[2];
    private BitTreeEncoder[] _lowCoder = new BitTreeEncoder[LzmaState.kNumPosStatesEncodingMax];
    private BitTreeEncoder[] _midCoder = new BitTreeEncoder[LzmaState.kNumPosStatesEncodingMax];
    private BitTreeEncoder _highCoder = new BitTreeEncoder(LzmaState.kNumHighLenBits);

    LenEncoder() {
        for (int posState = 0; posState < LzmaState.kNumPosStatesEncodingMax; posState++) {
            _lowCoder[posState] = new BitTreeEncoder(LzmaState.kNumLowLenBits);
            _midCoder[posState] = new BitTreeEncoder(LzmaState.kNumMidLenBits);
        }
    }

    void init(int numPosStates) {
        RangeEncoder.initBitModels(_choice);
        for (int posState = 0; posState < numPosStates; posState++) {
            _lowCoder[posState].init();
            _midCoder[posState].init();
        }
        _highCoder.init();
    }

    void encode(RangeEncoder rangeEncoder, int symbol, int posState) throws IOException {
        if (symbol < LzmaState.kNumLowLenSymbols) {
            rangeEncoder.encode(_choice, 0, 0);
            _lowCoder[posState].encode(rangeEncoder, symbol);
        } else {
            symbol -= LzmaState.kNumLowLenSymbols;
            rangeEncoder.encode(_choice, 0, 1);
            if (symbol < LzmaState.kNumMidLenSymbols) {
                rangeEncoder.encode(_choice, 1, 0);
                _midCoder[posState].encode(rangeEncoder, symbol);
            } else {
                rangeEncoder.encode(_choice, 1, 1);
                _highCoder.encode(rangeEncoder, symbol - LzmaState.kNumMidLenSymbols);
            }
        }
    }

    void setPrices(int posState, int numSymbols, int[] prices, int st) {
        int a0 = RangeEncoder.getPrice0(_choice[0]);
        int a1 = RangeEncoder.getPrice1(_choice[0]);
        int b0 = a1 + RangeEncoder.getPrice0(_choice[1]);
        int b1 = a1 + RangeEncoder.getPrice1(_choice[1]);
        int i = 0;
        for (i = 0; i < LzmaState.kNumLowLenSymbols; i++) {
            if (i >= numSymbols) {
                return;
            }
            prices[st + i] = a0 + _lowCoder[posState].getPrice(i);
        }
        for (; i < LzmaState.kNumLowLenSymbols + LzmaState.kNumMidLenSymbols; i++) {
            if (i >= numSymbols) {
                return;
            }
            prices[st + i] = b0 + _midCoder[posState].getPrice(i - LzmaState.kNumLowLenSymbols);
        }
        for (; i < numSymbols; i++) {
            prices[st + i] = b1 + _highCoder.getPrice(i - LzmaState.kNumLowLenSymbols - LzmaState.kNumMidLenSymbols);
        }
    }
    
}
