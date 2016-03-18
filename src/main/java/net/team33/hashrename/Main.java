package net.team33.hashrename;

import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Iterator;
import java.util.logging.Logger;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
public final class Main {

    private static final int BUFFER_SIZE = 16;
    private static final Logger LOGGER = Logger.getLogger(Main.class.getCanonicalName());

    private Main() {
    }

    public static void main(final String[] args) throws IOException {
        rename(new Cursor(args));
    }

    private static void rename(final Iterator<Path> paths) {
        while (paths.hasNext()) {
            rename(paths.next());
        }
    }

    private static void rename(final Path path) {
        if (Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) {
            renameRegularFile(path);
        } else if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
            renameContent(path);
        } else {
            LOGGER.log(INFO, String.format("Not a regular file or directory: <%s>", path));
        }
    }

    private static void renameContent(final Path path) {
        try (DirectoryStream<Path> paths = Files.newDirectoryStream(path)) {
            rename(paths.iterator());
        } catch (final IOException e) {
            LOGGER.log(WARNING, String.format("Could not read directory <%s>", path), e);
        }
    }

    private static void renameRegularFile(final Path path) {
        final String oldName = path.getFileName().toString();
        if (isHashName(oldName)) {
            LOGGER.log(INFO, String.format("Nothing to do: <%s>", path));
        } else {
            final int extStart = oldName.lastIndexOf('.');
            final String extension = (0 > extStart) ? "" : oldName.substring(extStart);
            try {
                final String newName = String.format("(#%s)%s", Hash.from(path), extension);
                //Files.move(path, path.getParent().resolve(newName));
                LOGGER.log(INFO, String.format("Moved <%s> to <%s>", path, newName));
            } catch (IOException e) {
                LOGGER.log(WARNING, String.format("Could not access or rename <%s>", path), e);
            }
        }
    }

    private static boolean isHashName(String name) {
        final int start = name.indexOf("(#");
        if (0 <= start) {
            int i = start;
            do {
                i += 1;
            } while ("0123456789abcdefghijklmnopqrstuvwxyz".indexOf(name.charAt(i)) >= 0);
            if (')' == name.charAt(i)) {
                return true;
            }
        }
        return false;
    }

    private static class Cursor implements Iterator<Path> {

        private final Iterator<String> backing;

        private Cursor(String[] paths) {
            this.backing = Arrays.asList(paths).iterator();
        }

        @Override
        public final boolean hasNext() {
            return backing.hasNext();
        }

        @Override
        public final Path next() {
            return Paths.get(backing.next()).toAbsolutePath().normalize();
        }
    }
}
