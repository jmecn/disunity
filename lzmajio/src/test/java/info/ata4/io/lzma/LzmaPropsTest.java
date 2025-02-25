/*
 ** 2014 December 15
 **
 ** The author disclaims copyright to this source code. In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.io.lzma;
import javax.xml.bind.DatatypeConverter;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class LzmaPropsTest {
    
    private static final byte[] REF_DEFAULT = DatatypeConverter.parseHexBinary("5D00004000FFFFFFFFFFFFFFFF");
    private static final byte[] REF_EXAMPLE = DatatypeConverter.parseHexBinary("5D000008006C2B1F0400000000");
    
    @Test
    public void testArraysDefault() {
        LzmaProps props = new LzmaProps();
        
        assertArrayEquals(REF_DEFAULT, props.toArray());
    }

    @Test
    public void testArraysExample() {
        LzmaProps props = new LzmaProps();
        props.fromArray(REF_EXAMPLE);
        
        assertArrayEquals(REF_EXAMPLE, props.toArray());
    }
    
    @Test
    public void testGetSet() {
        LzmaProps props1 = new LzmaProps();
        props1.fromArray(REF_EXAMPLE);
        
        LzmaProps props2 = new LzmaProps();
        props2.setNumLiteralContextBits(props1.getNumLiteralContextBits());
        props2.setNumLiteralPosStateBits(props1.getNumLiteralPosStateBits());
        props2.setPosStateBits(props1.getPosStateBits());
        props2.setDictionarySize(props1.getDictionarySize());
        props2.setUncompressedSize(props1.getUncompressedSize());
        
        assertArrayEquals(REF_EXAMPLE, props2.toArray());
    }
}
