package net.team33.hashrename;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;

import static java.lang.String.format;

public class Args {

    public static final Charset CHARSET = Charset.forName("UTF-8");

    private static final Properties EMPTY_PROPERTIES = new Properties();
    private static final String PATH = "path";
    private static final String RECURSIVE = "recursive";
    private static final String FORMAT = "format";
    private static final String INDEX_FORMAT = "format.I";

    private final String path;
    private final boolean recursive;
    private final String format;
    private final String indexFormat;

    Args() {
        this(EMPTY_PROPERTIES);
    }

    Args(final Properties properties) {
        path = Optional.ofNullable(properties.getProperty(PATH)).orElse(".");
        recursive = Boolean.valueOf(Optional.ofNullable(properties.getProperty(RECURSIVE)).orElse("false"));
        format = Optional.ofNullable(properties.getProperty(FORMAT)).orElse("%I.{%H}.%X");
        indexFormat = Optional.ofNullable(properties.getProperty(INDEX_FORMAT)).orElse("04");
    }

    public static Args build(final String[] args) throws Problem {
        try (final InputStream in = new FileInputStream(args[0])) {
            final Properties properties = new Properties();
            properties.load(new InputStreamReader(in, CHARSET));
            return new Args(properties);
        } catch (final IOException | RuntimeException caught) {
            throw new Problem(format("Problem while parsing arguments <%s>", Arrays.toString(args)), caught);
        }
    }

    @Override
    public final String toString() {
        return format("Args(path(%s), recursive(%s), format(%s)}", path, recursive, format);
    }

    public final Properties toProperties() {
        final Properties properties = new Properties();
        properties.setProperty(PATH, path);
        properties.setProperty(RECURSIVE, Boolean.toString(recursive));
        properties.setProperty(FORMAT, format);
        properties.setProperty(INDEX_FORMAT, indexFormat);
        return properties;
        // throw new UnsupportedOperationException("not yet implemented");
    }

    public final Path getPath() {
        return Paths.get(path).toAbsolutePath().normalize();
    }

    public static class Problem extends Exception {
        public Problem(final String message, final Throwable cause) {
            super(message, cause);
        }
    }
}
