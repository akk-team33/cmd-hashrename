package net.team33.hashrename;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
public final class Main {

    private static final Logger LOGGER
            = Logger.getLogger(Main.class.getCanonicalName());
    private static final ExecutorService EXECUTOR
            = Executors.newWorkStealingPool(4 + (4 * Runtime.getRuntime().availableProcessors()));
    private static final Activity ACTIVITY
            = new Activity();
    private static final AtomicInteger MOVED = new AtomicInteger();
    private static final AtomicInteger FAILED = new AtomicInteger();

    private Main() {
    }

    public static void main(final String[] args) throws IOException, InterruptedException {
        rename(
                Paths.get(args[0]).toAbsolutePath().normalize(), Paths.get(args[1]).toAbsolutePath().normalize()
        );
        ACTIVITY.join();
        LOGGER.info("MOVED:  " + MOVED + " files");
        LOGGER.info("FAILED: " + FAILED + " files");
    }

    private static void rename(final Path sievedRoot, final Iterator<Path> iterator) {
        final List<Path> paths = new LinkedList<>();
        while (iterator.hasNext()) {
            paths.add(iterator.next());
        }
        rename(sievedRoot, paths);
    }

    private static void rename(final Path sievedRoot, final List<Path> paths) {
        for (final Path path : paths) {
            EXECUTOR.execute(ACTIVITY.add(() -> rename(path, sievedRoot)));
        }
    }

    private static void rename(final Path path, final Path sievedRoot) {
        if (Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) {
            renameRegularFile(path, sievedRoot);
        } else if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
            renameContent(path, sievedRoot);
        } else {
            LOGGER.log(INFO, String.format("Not a regular file or directory: <%s>", path));
        }
    }

    private static void renameContent(final Path path, final Path sievedRoot) {
        try (DirectoryStream<Path> paths = Files.newDirectoryStream(path)) {
            if (!path.equals(sievedRoot)) {
                rename(sievedRoot, paths.iterator());
            }
        } catch (final IOException e) {
            LOGGER.log(WARNING, String.format("Could not read directory <%s>", path), e);
        }
    }

    private static void renameRegularFile(final Path path, final Path sievedRoot) {
        try {
            final Naming naming = new Naming(path);
            final Path target = naming.resolve(sievedRoot);
            Files.move(path, target);
            MOVED.addAndGet(1);
            LOGGER.log(INFO, String.format("Moved <%s>\n\t-> <%s>", path, target));
        } catch (final IOException e) {
            FAILED.addAndGet(1);
            LOGGER.log(WARNING, String.format("Could not access or rename <%s>", path), e);
        }
    }
}
