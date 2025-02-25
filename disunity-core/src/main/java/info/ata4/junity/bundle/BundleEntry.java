/*
 ** 2014 September 29
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.junity.bundle;

import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public abstract class BundleEntry {

    public static boolean isLibrary(BundleEntry entry) {
        String ext = FilenameUtils.getExtension(entry.getName());
        return ext.equals("dll") || ext.equals("mdb");
    }

    public static boolean isResource(BundleEntry entry) {
        String ext = FilenameUtils.getExtension(entry.getName());
        return ext.equals("resource");
    }

    public abstract String getName();

    public abstract long getSize();

    public abstract InputStream inputStream() throws IOException;
}
