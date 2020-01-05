package org.sela;

import org.junit.Test;
import org.sela.lpc.*;

import static org.junit.Assert.*;

public class LpcTest {
    @Test
    public void testResidueSampleGenerator() {
        // Generate a sine wave
        int[] samples = new int[2048];
        for(int i = 0; i < samples.length; i++) {
            samples[i] = (int)(32767 * Math.sin(Math.toRadians(i)));
        }

        //Generate residues
        ResidueGenerator resGen = new ResidueGenerator(samples);
        int[] residues = resGen.process();
        int[] quantizedReflectionCoefficients = resGen.getQuantizedReflectionCoefficients();
        byte optimalOrder = resGen.getOptimalOrder();

        //Generate samples
        SampleGenerator sampleGen = new SampleGenerator(residues, quantizedReflectionCoefficients, optimalOrder);
        int[] decodedSamples = sampleGen.process();

        assertArrayEquals(samples, decodedSamples);
    }
}