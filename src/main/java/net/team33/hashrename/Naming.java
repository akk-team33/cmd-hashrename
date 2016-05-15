package net.team33.hashrename;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static net.team33.hashrename.Package.DIGITS;
import static net.team33.hashrename.Package.ENDOF_HASH;
import static net.team33.hashrename.Package.START_HASH;

public class Naming {

    public static final String START_OF_EXTENSION = ".";

    private final Original original;
    private final Hashed hashed;
    private final Sieve sieve;

    public Naming(final Path originalPath) throws IOException {
        original = new Original(originalPath);
        hashed = new Hashed(originalPath, original);
        sieve = new Sieve(hashed);
    }

    public Path resolve(final Path root) throws IOException {
        final Path path = root.resolve(sieve.path);
        Files.createDirectories(path);
        return path.resolve(hashed.getFileName());
    }

    private static class Original {
        private final String name;
        private final String extension;

        private Original(final Path original) {
            final String oldName = original.getFileName().toString();
            final int indexOfExtension = oldName.lastIndexOf(START_OF_EXTENSION);
            if (0 > indexOfExtension) {
                name = oldName;
                extension = "";
            } else {
                name = oldName.substring(0, indexOfExtension);
                extension = oldName.substring(indexOfExtension);
            }
        }
    }

    private static class Hashed {
        private static final int START_LENGTH = START_HASH.length();

        private final String prefix;
        private final String value;
        private final String postfix;
        private final String name;
        private final String extension;

        private Hashed(final Path originalPath, final Original original) throws IOException {
            this(originalPath, original.name, original.extension);
        }

        private Hashed(final Path originPath, final String originName, final String originExt)
                throws IOException {

            final int start = start(originName);
            final int limit = limit(originName, start);
            if (start < limit) {
                prefix = originName.substring(0, start);
                value = originName.substring(start, limit);
                postfix = originName.substring(limit);
            } else {
                prefix = START_HASH;
                postfix = ENDOF_HASH;
                value = Hash.from(originPath);
            }
            name = String.format("%s%s%s", prefix, value, postfix);
            extension = originExt.toLowerCase();
        }

        private static int start(final String original) {
            return original.indexOf(START_HASH) + START_LENGTH;
        }

        private static int limit(final String original, final int start) {
            if (START_LENGTH > start) {
                return start;
            } else {
                int limit = start;
                while (DIGITS.indexOf(original.charAt(limit)) >= 0) {
                    limit += 1;
                }
                if (original.substring(limit).startsWith(ENDOF_HASH)) {
                    return limit;
                } else {
                    return start;
                }
            }
        }

        private String getFileName() {
            return name + extension;
        }
    }

    private static class Sieve {
        private final Path path;

        private Sieve(final Hashed hashed) {
            this(new StringBuilder(Package.LEADING).append(hashed.value).toString());
        }

        private Sieve(final String hashed) {
            final int i3 = hashed.length();
            final int i2 = i3 - 2;
            final int i1 = i2 - 2;

            final String first = hashed.substring(i1, i2);
            final String second = hashed.substring(i2, i3);

            path = Paths.get(first, second);
        }
    }
}
