package lzma;

class LiteralDecoder {

    private Decoder2[] m_Coders;
    private int m_NumPrevBits;
    private int m_NumPosBits;
    private int m_PosMask;

    void create(int numPosBits, int numPrevBits) {
        if (m_Coders != null && m_NumPrevBits == numPrevBits && m_NumPosBits == numPosBits) {
            return;
        }
        m_NumPosBits = numPosBits;
        m_PosMask = (1 << numPosBits) - 1;
        m_NumPrevBits = numPrevBits;
        int numStates = 1 << (m_NumPrevBits + m_NumPosBits);
        m_Coders = new Decoder2[numStates];
        for (int i = 0; i < numStates; i++) {
            m_Coders[i] = new Decoder2();
        }
    }

    void init() {
        int numStates = 1 << (m_NumPrevBits + m_NumPosBits);
        for (int i = 0; i < numStates; i++) {
            m_Coders[i].init();
        }
    }

    Decoder2 getDecoder(int pos, byte prevByte) {
        return m_Coders[((pos & m_PosMask) << m_NumPrevBits) + ((prevByte & 0xFF) >>> (8 - m_NumPrevBits))];
    }
    
}
