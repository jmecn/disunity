/*
 ** 2014 September 25
 **
 ** The author disclaims copyright to this source code. In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.junity.bundle;

import info.ata4.io.DataReader;
import info.ata4.io.DataWriter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.IOException;

/**
 * UnityFS-format bundle entry info
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class StreamingInfoFS extends StreamingInfo {

    // unknown extra field, guessing flags
    private long flags;

    @Override
    public void read(DataReader in) throws IOException {
        setOffset(in.readLong());
        setSize(in.readLong());
        setFlags(in.readUnsignedInt());
        setName(in.readStringNull());
    }

    @Override
    public void write(DataWriter out) throws IOException {
        out.writeLong(getOffset());
        out.writeLong(getSize());
        out.writeUnsignedInt(getFlags());
        out.writeStringNull(getName());
    }
}
