package net.team33.hashrename;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import static net.team33.hashrename.Package.DIGITS;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
public final class Main {

    private static final String START_HASH = "[#";
    private static final String ENDOF_HASH = "]";
    private static final Logger LOGGER = Logger.getLogger(Main.class.getCanonicalName());
    private static final ExecutorService EXECUTOR
            = Executors.newWorkStealingPool(4 + (4 * Runtime.getRuntime().availableProcessors()));

    private Main() {
    }

    public static void main(final String[] args) throws IOException {
        rename(new AbstractList<Path>() {
            @Override
            public Path get(final int index) {
                return Paths.get(args[index]).toAbsolutePath().normalize();
            }

            @Override
            public int size() {
                return args.length;
            }
        });
    }

    private static void rename(final Iterator<Path> iterator) {
        final List<Path> paths = new LinkedList<>();
        while (iterator.hasNext()) {
            paths.add(iterator.next());
        }
        rename(paths);
    }

    private static void rename(final List<Path> paths) {
        for (final Path path : paths) {
            EXECUTOR.execute(() -> rename(path));
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
                final String newName = String.format("%s%s%s%s", START_HASH, Hash.from(path), ENDOF_HASH, extension);
                Files.move(path, path.getParent().resolve(newName));
                LOGGER.log(INFO, String.format("Moved <%s> to <%s>", path, newName));
            } catch (final IOException e) {
                LOGGER.log(WARNING, String.format("Could not access or rename <%s>", path), e);
            }
        }
    }

    private static boolean isHashName(final String name) {
        final int start = name.indexOf(START_HASH);
        if (0 <= start) {
            int i = start + START_HASH.length();
            while (DIGITS.indexOf(name.charAt(i)) >= 0) {
                i += 1;
            }
            if (name.substring(i).startsWith(ENDOF_HASH)) {
                return true;
            }
        }
        return false;
    }
}
