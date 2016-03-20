package net.team33.hashrename;

import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BigIntegerTest {

    @Test
    public void withoutSignum() throws Exception {
        assertTrue(BigInteger.ZERO.compareTo(new BigInteger(new byte[]{-1, 0, 0, 0})) > 0);
        assertTrue(BigInteger.ZERO.compareTo(new BigInteger(new byte[]{1, 0, 0, 0})) < 0);
    }

    @Test
    public void zeroWithSignum() throws Exception {
        assertEquals(BigInteger.ZERO, new BigInteger(1, new byte[]{0, 0, 0, 0}));
        assertEquals(BigInteger.ZERO, new BigInteger(-1, new byte[]{0, 0, 0, 0}));
        assertEquals(BigInteger.ZERO, new BigInteger(0, new byte[]{0, 0, 0, 0}));
    }

    @Test
    public void withSignum() throws Exception {
        assertTrue(BigInteger.ZERO.compareTo(new BigInteger(1, new byte[]{-1, 0, 0, 0})) < 0);
        assertTrue(BigInteger.ZERO.compareTo(new BigInteger(1, new byte[]{1, 0, 0, 0})) < 0);
        assertTrue(BigInteger.ZERO.compareTo(new BigInteger(-1, new byte[]{-1, 0, 0, 0})) > 0);
        assertTrue(BigInteger.ZERO.compareTo(new BigInteger(-1, new byte[]{1, 0, 0, 0})) > 0);
    }

    @Test
    public void nonZeroWithSignum() throws Exception {
        assertEquals(
                new BigInteger(new byte[]{0, -1, -1}),
                new BigInteger(1, new byte[]{-1, -1}));
        assertEquals(
                BigInteger.ZERO.subtract(new BigInteger(new byte[]{1, 0, 0})),
                new BigInteger(-1, new byte[]{1, 0, 0}));
    }
}