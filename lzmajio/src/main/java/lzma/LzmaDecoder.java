package lzma;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import lzma.lz.OutWindow;
import lzma.rangecoder.BitTreeDecoder;
import lzma.rangecoder.RangeDecoder;

public class LzmaDecoder {
    
    private OutWindow m_OutWindow = new OutWindow();
    private RangeDecoder m_RangeDecoder = new RangeDecoder();
    private short[] m_IsMatchDecoders = new short[LzmaState.kNumStates << LzmaState.kNumPosStatesBitsMax];
    private short[] m_IsRepDecoders = new short[LzmaState.kNumStates];
    private short[] m_IsRepG0Decoders = new short[LzmaState.kNumStates];
    private short[] m_IsRepG1Decoders = new short[LzmaState.kNumStates];
    private short[] m_IsRepG2Decoders = new short[LzmaState.kNumStates];
    private short[] m_IsRep0LongDecoders = new short[LzmaState.kNumStates << LzmaState.kNumPosStatesBitsMax];
    private BitTreeDecoder[] m_PosSlotDecoder = new BitTreeDecoder[LzmaState.kNumLenToPosStates];
    private short[] m_PosDecoders = new short[LzmaState.kNumFullDistances - LzmaState.kEndPosModelIndex];
    private BitTreeDecoder m_PosAlignDecoder = new BitTreeDecoder(LzmaState.kNumAlignBits);
    private LenDecoder m_LenDecoder = new LenDecoder();
    private LenDecoder m_RepLenDecoder = new LenDecoder();
    private LiteralDecoder m_LiteralDecoder = new LiteralDecoder();
    private int m_DictionarySize = -1;
    private int m_DictionarySizeCheck = -1;
    private int m_PosStateMask;

    public LzmaDecoder() {
        for (int i = 0; i < LzmaState.kNumLenToPosStates; i++) {
            m_PosSlotDecoder[i] = new BitTreeDecoder(LzmaState.kNumPosSlotBits);
        }
    }

    public boolean setDictionarySize(int dictionarySize) {
        if (dictionarySize < 0) {
            return false;
        }
        if (m_DictionarySize != dictionarySize) {
            m_DictionarySize = dictionarySize;
            m_DictionarySizeCheck = Math.max(m_DictionarySize, 1);
            m_OutWindow.create(Math.max(m_DictionarySizeCheck, (1 << 12)));
        }
        return true;
    }

    public boolean setLcLpPb(int lc, int lp, int pb) {
        if (lc > LzmaState.kNumLitContextBitsMax || lp > 4 || pb > LzmaState.kNumPosStatesBitsMax) {
            return false;
        }
        m_LiteralDecoder.create(lp, lc);
        int numPosStates = 1 << pb;
        m_LenDecoder.create(numPosStates);
        m_RepLenDecoder.create(numPosStates);
        m_PosStateMask = numPosStates - 1;
        return true;
    }

    private void init() throws IOException {
        m_OutWindow.init(false);

        RangeDecoder.initBitModels(m_IsMatchDecoders);
        RangeDecoder.initBitModels(m_IsRep0LongDecoders);
        RangeDecoder.initBitModels(m_IsRepDecoders);
        RangeDecoder.initBitModels(m_IsRepG0Decoders);
        RangeDecoder.initBitModels(m_IsRepG1Decoders);
        RangeDecoder.initBitModels(m_IsRepG2Decoders);
        RangeDecoder.initBitModels(m_PosDecoders);

        m_LiteralDecoder.init();
        int i;
        for (i = 0; i < LzmaState.kNumLenToPosStates; i++) {
            m_PosSlotDecoder[i].init();
        }
        m_LenDecoder.init();
        m_RepLenDecoder.init();
        m_PosAlignDecoder.init();
        m_RangeDecoder.init();
    }

    public boolean code(InputStream inStream, OutputStream outStream,
            long outSize) throws IOException {
        m_RangeDecoder.setStream(inStream);
        m_OutWindow.setStream(outStream);
        init();

        int state = LzmaState.stateInit();
        int rep0 = 0, rep1 = 0, rep2 = 0, rep3 = 0;

        long nowPos64 = 0;
        byte prevByte = 0;
        while (outSize < 0 || nowPos64 < outSize) {
            int posState = (int) nowPos64 & m_PosStateMask;
            if (m_RangeDecoder.decodeBit(m_IsMatchDecoders, (state << LzmaState.kNumPosStatesBitsMax) + posState) == 0) {
                Decoder2 decoder2 = m_LiteralDecoder.getDecoder((int) nowPos64, prevByte);
                if (!LzmaState.stateIsCharState(state)) {
                    prevByte = decoder2.decodeWithMatchByte(m_RangeDecoder, m_OutWindow.getByte(rep0));
                } else {
                    prevByte = decoder2.decodeNormal(m_RangeDecoder);
                }
                m_OutWindow.putByte(prevByte);
                state = LzmaState.stateUpdateChar(state);
                nowPos64++;
            } else {
                int len;
                if (m_RangeDecoder.decodeBit(m_IsRepDecoders, state) == 1) {
                    len = 0;
                    if (m_RangeDecoder.decodeBit(m_IsRepG0Decoders, state) == 0) {
                        if (m_RangeDecoder.decodeBit(m_IsRep0LongDecoders, (state << LzmaState.kNumPosStatesBitsMax) + posState) == 0) {
                            state = LzmaState.stateUpdateShortRep(state);
                            len = 1;
                        }
                    } else {
                        int distance;
                        if (m_RangeDecoder.decodeBit(m_IsRepG1Decoders, state) == 0) {
                            distance = rep1;
                        } else {
                            if (m_RangeDecoder.decodeBit(m_IsRepG2Decoders, state) == 0) {
                                distance = rep2;
                            } else {
                                distance = rep3;
                                rep3 = rep2;
                            }
                            rep2 = rep1;
                        }
                        rep1 = rep0;
                        rep0 = distance;
                    }
                    if (len == 0) {
                        len = m_RepLenDecoder.decode(m_RangeDecoder, posState) + LzmaState.kMatchMinLen;
                        state = LzmaState.stateUpdateRep(state);
                    }
                } else {
                    rep3 = rep2;
                    rep2 = rep1;
                    rep1 = rep0;
                    len = LzmaState.kMatchMinLen + m_LenDecoder.decode(m_RangeDecoder, posState);
                    state = LzmaState.stateUpdateMatch(state);
                    int posSlot = m_PosSlotDecoder[LzmaState.getLenToPosState(len)].decode(m_RangeDecoder);
                    if (posSlot >= LzmaState.kStartPosModelIndex) {
                        int numDirectBits = (posSlot >> 1) - 1;
                        rep0 = ((2 | (posSlot & 1)) << numDirectBits);
                        if (posSlot < LzmaState.kEndPosModelIndex) {
                            rep0 += BitTreeDecoder.reverseDecode(m_PosDecoders,
                                    rep0 - posSlot - 1, m_RangeDecoder, numDirectBits);
                        } else {
                            rep0 += (m_RangeDecoder.decodeDirectBits(
                                    numDirectBits - LzmaState.kNumAlignBits) << LzmaState.kNumAlignBits);
                            rep0 += m_PosAlignDecoder.reverseDecode(m_RangeDecoder);
                            if (rep0 < 0) {
                                if (rep0 == -1) {
                                    break;
                                }
                                return false;
                            }
                        }
                    } else {
                        rep0 = posSlot;
                    }
                }
                if (rep0 >= nowPos64 || rep0 >= m_DictionarySizeCheck) {
                    // m_OutWindow.Flush();
                    return false;
                }
                m_OutWindow.copyBlock(rep0, len);
                nowPos64 += len;
                prevByte = m_OutWindow.getByte(0);
            }
        }
        m_OutWindow.flush();
        m_OutWindow.releaseStream();
        m_RangeDecoder.releaseStream();
        return true;
    }
}
