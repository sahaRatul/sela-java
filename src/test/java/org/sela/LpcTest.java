package org.sela;

import org.junit.Test;
import org.sela.data.*;
import org.sela.lpc.*;

import static org.junit.Assert.*;

public class LpcTest {
    @Test
    public void testResidueSampleGenerator() {
        // Generate a sine wave
        int[] samples = new int[2048];
        for (int i = 0; i < samples.length; i++) {
            samples[i] = (int) (32767 * Math.sin(Math.toRadians(i)));
        }

        LpcDecodedData input = new LpcDecodedData(samples, (byte)16);

        // Generate residues
        ResidueGenerator resGen = new ResidueGenerator(input);
        LpcEncodedData encoded = resGen.process();

        // Generate samples
        SampleGenerator sampleGen = new SampleGenerator(encoded);
        LpcDecodedData decoded = sampleGen.process();

        assertArrayEquals(input.samples, decoded.samples);
    }
}