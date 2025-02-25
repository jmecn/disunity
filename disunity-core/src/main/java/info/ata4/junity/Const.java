package info.ata4.junity;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public final class Const {

    private Const() {}

    // ArchiveFlags
    public static final int COMPRESSION_TYPE_MASK = 0x3F;
    public static final int BLOCKS_AND_DIRECTORY_INFO_COMBINED = 0x40;
    public static final int BLOCKS_INFO_AT_THE_END = 0x80;
    public static final int OLD_WEB_PLUGIN_COMPATIBILITY = 0x100;
    public static final int BLOCK_INFO_NEED_PADDING_AT_START = 0x200;

    // CompressionType
    public static final int COMPRESSION_TYPE_NONE = 0;
    public static final int COMPRESSION_TYPE_LZMA = 1;
    public static final int COMPRESSION_TYPE_LZ4 = 2;
    public static final int COMPRESSION_TYPE_LZ4HC = 3;
    public static final int COMPRESSION_TYPE_LZHAM = 4;
}