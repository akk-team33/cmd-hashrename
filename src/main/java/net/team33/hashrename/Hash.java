package net.team33.hashrename;

import net.team33.numcodec.PlainCodec;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;

import static net.team33.hashrename.Package.DIGITS;

public class Hash {

    private static final int BUFFER_SIZE = 15;
    private static final PlainCodec CODEC = PlainCodec.using(DIGITS);

    public static String from(final Path path) throws IOException {
        return CODEC.encode(value(path));
    }

    private static BigInteger value(final Path path) throws IOException {
        final byte[] hash = new byte[BUFFER_SIZE];
        final byte[] buffer = new byte[BUFFER_SIZE];
        try (final InputStream in = Files.newInputStream(path)) {
            long k = 0;
            int read = in.read(buffer);
            while (0 < read) {
                for (int i = 0; i < read; ++i, ++k) {
                    final int m = (int) (k % hash.length);
                    hash[m] += buffer[i];
                }
                read = in.read(buffer);
            }
        }
        return new BigInteger(1, hash);
    }
}
