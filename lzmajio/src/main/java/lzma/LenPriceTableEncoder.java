package lzma;

import java.io.IOException;
import lzma.rangecoder.RangeEncoder;

class LenPriceTableEncoder extends LenEncoder {

    int[] _prices = new int[LzmaState.kNumLenSymbols << LzmaState.kNumPosStatesBitsEncodingMax];
    int _tableSize;
    int[] _counters = new int[LzmaState.kNumPosStatesEncodingMax];

    void setTableSize(int tableSize) {
        _tableSize = tableSize;
    }

    int getPrice(int symbol, int posState) {
        return _prices[posState * LzmaState.kNumLenSymbols + symbol];
    }

    void updateTable(int posState) {
        setPrices(posState, _tableSize, _prices, posState * LzmaState.kNumLenSymbols);
        _counters[posState] = _tableSize;
    }

    void updateTables(int numPosStates) {
        for (int posState = 0; posState < numPosStates; posState++) {
            updateTable(posState);
        }
    }

    @Override
    void encode(RangeEncoder rangeEncoder, int symbol, int posState) throws IOException {
        super.encode(rangeEncoder, symbol, posState);
        if (--_counters[posState] == 0) {
            updateTable(posState);
        }
    }
    
}
