/*
 ** 2014 February 03
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.junity;

import java.util.Objects;

/**
 * Unity engine version string container.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class UnityVersion implements Comparable<UnityVersion> {

    private int major;
    private int minor;
    private int patch;
    private String build;
    private String raw;

    public UnityVersion(String version) {
        try {
            String[] array = version.split("\\.");

            major = partFromString(array[0]);
            minor = partFromString(array[1]);
            if (array[2].length() == 1) {
                patch = partFromString(array[2]);
                build = array[2];
            } else {
                String[] patches = array[2].split("f|a|b|rc");
                patch = partFromString(patches[0]);
                build = patches[1];
            }
        } catch (NumberFormatException | IndexOutOfBoundsException ex) {
            // invalid format, save raw string
            raw = version;
        }
    }

    public UnityVersion() {
        this("1.0.0f1");
    }

    private int partFromString(String part) {
        if (part.equals("x")) {
            return -1;
        } else {
            return Integer.valueOf(part);
        }
    }

    private String partToString(int part) {
        if (part == -1) {
            return "x";
        } else {
            return String.valueOf(part);
        }
    }

    public boolean isValid() {
        return raw == null;
    }

    public int major() {
        return major;
    }

    public void major(int major) {
        this.major = major;
    }

    public int minor() {
        return minor;
    }

    public void minor(int minor) {
        this.minor = minor;
    }

    public int patch() {
        return patch;
    }

    public void patch(int patch) {
        this.patch = patch;
    }

    public String build() {
        return build;
    }

    public void build(String build) {
        this.build = build;
    }

    @Override
    public String toString() {
        if (raw != null) {
            return raw;
        } else {
            return String.format("%s.%s.%s%s", partToString(major),
                    partToString(minor), partToString(patch), build);
        }
    }

    @Override
    public int hashCode() {
        if (raw != null) {
            return raw.hashCode();
        } else {
            int hash = 5;
            hash = 97 * hash + this.major;
            hash = 97 * hash + this.minor;
            hash = 97 * hash + this.patch;
            hash = 97 * hash + Objects.hashCode(this.build);
            return hash;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final UnityVersion other = (UnityVersion) obj;
        if (raw != null) {
            if (!Objects.equals(this.raw, other.raw)) {
                return false;
            }
        } else {
            if (this.major != other.major) {
                return false;
            }
            if (this.minor != other.minor) {
                return false;
            }
            if (this.patch != other.patch) {
                return false;
            }
            if (!Objects.equals(this.build, other.build)) {
                return false;
            }
            if (!Objects.equals(this.raw, other.raw)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int compareTo(UnityVersion that) {
        if (!this.isValid() && !that.isValid()) {
            return this.raw.compareTo(that.raw);
        }

        if (this.major < that.major) {
            return 1;
        } else if (this.major > that.major) {
            return -1;
        } else {
            if (this.minor < that.minor) {
                return 1;
            } else if (this.minor > that.minor) {
                return -1;
            } else {
                if (this.patch < that.patch) {
                    return 1;
                } else if (this.patch > that.patch) {
                    return -1;
                } else {
                    return this.build.compareTo(that.build);
                }
            }
        }
    }

    public boolean lesserThan(UnityVersion that) {
        return this.compareTo(that) == 1;
    }

    public boolean greaterThan(UnityVersion that) {
        return this.compareTo(that) == -1;
    }
}
