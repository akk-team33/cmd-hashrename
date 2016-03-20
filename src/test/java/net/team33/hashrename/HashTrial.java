package net.team33.hashrename;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import static java.util.logging.Level.WARNING;
import static org.junit.Assert.assertEquals;

public class HashTrial {

    private static final Logger LOGGER = Logger.getLogger(HashTrial.class.getCanonicalName());

    @Test
    public final void test() {
        final List<String> list = hashes(Paths.get("tmp/src").toAbsolutePath().normalize());
        final Set<String> set = new HashSet<>(list);
        assertEquals(list.size(), set.size());
    }

    private List<String> hashes(final Path path) {
        return hashes(new ArrayList<String>(0), path);
    }

    private List<String> hashes(final List<String> target, final Path path) {
        if (Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) {
            return hashFile(target, path);
        } else if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
            return hashDir(target, path);
        } else {
            return target;
        }
    }

    private List<String> hashFile(final List<String> target, final Path path) {
        try {
            final String hash = Hash.from(path);
            final String message = path.toString() + " -> " + hash;
            LOGGER.info(message);
            target.add(hash);
        } catch (IOException e) {
            LOGGER.log(WARNING, "could not read file", e);
        }
        return target;
    }

    private List<String> hashDir(final List<String> target, final Path path) {
        try (final DirectoryStream<Path> content = Files.newDirectoryStream(path)) {
            for (final Path entry : content) {
                hashes(target, entry);
            }
        } catch (final IOException e) {
            LOGGER.log(WARNING, "could not read directory", e);
        }
        return target;
    }
}