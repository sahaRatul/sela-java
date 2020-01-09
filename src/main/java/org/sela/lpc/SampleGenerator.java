package org.sela.lpc;

import org.sela.data.*;

public final class SampleGenerator {
    private final int[] residues;
    private final int[] samples;
    private final LinearPredictor linearPredictor;

    public SampleGenerator(final LpcEncodedData encodedData) {
        this.linearPredictor = new LinearPredictor(encodedData.quantizedReflectionCoefficients, encodedData.optimalLpcOrder);
        this.residues = encodedData.residues;
        this.samples = new int[residues.length];
    }

    private void generateSamples() {
        final long correction = (long) 1 << (linearPredictor.correctionFactor - 1);

        samples[0] = residues[0];

        for (int i = 1; i <= linearPredictor.optimalLpcOrder; i++) {
            long temp = correction;
            for (int j = 1; j <= i; j++) {
                temp -= linearPredictor.linearPredictionCoefficients[j] * samples[i - j];
            }
            samples[i] = residues[i] - (int) (temp >> linearPredictor.correctionFactor);
        }

        for (int i = linearPredictor.optimalLpcOrder + 1; i < residues.length; i++) {
            long temp = correction;
            for (int j = 0; j <= linearPredictor.optimalLpcOrder; j++)
                temp -= (linearPredictor.linearPredictionCoefficients[j] * samples[i - j]);
            samples[i] = residues[i] - (int) (temp >> linearPredictor.correctionFactor);
        }
    }

    public LpcDecodedData process() {
        linearPredictor.dequantizeReflectionCoefficients();
        linearPredictor.generatelinearPredictionCoefficients();
        generateSamples();
        return new LpcDecodedData(samples);
    }
}