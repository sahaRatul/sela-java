package org.sela.lpc;

import org.sela.data.LpcDecodedData;
import org.sela.data.LpcEncodedData;

public final class ResidueGenerator extends LinearPredictionBase {
    private int[] samples;
    private int[] residues;
    private double[] quantizedSamples;
    private double[] autocorrelationFactors;

    private final int maxShort = 32767;
    private final double sqrt2 = 1.4142135623730950488016887242096;

    public ResidueGenerator(LpcDecodedData data) {
        this.samples = data.samples;
        this.quantizedSamples = new double[samples.length];
        this.autocorrelationFactors = new double[super.maxLpcOrder + 1];
        this.residues = new int[samples.length];
    }

    private void quantizeSamples() {
        for (int i = 0; i < samples.length; i++) {
            quantizedSamples[i] = (double) samples[i] / maxShort;
        }
    }

    private void generateAutoCorrelation() {
        double sum = 0, mean = 0;

        // Generate Mean of samples
        for (int i = 0; i < quantizedSamples.length; i++) {
            sum += quantizedSamples[i];
        }
        mean = sum / quantizedSamples.length;

        // Generate autocorrelation coefficients
        for (int i = 0; i <= super.maxLpcOrder; i++) {
            autocorrelationFactors[i] = 0.0;
            for (int j = i; j < quantizedSamples.length; j++) {
                autocorrelationFactors[i] += (quantizedSamples[j] - mean) * (quantizedSamples[j - i] - mean);
            }
        }

        // Normalise the coefficients
        for (int i = 1; i <= super.maxLpcOrder; i++) {
            autocorrelationFactors[i] /= autocorrelationFactors[0];
        }
        autocorrelationFactors[0] = 1.0;
    }

    private void generateReflectionCoefficients() {
        double error;
        double[][] gen = new double[2][super.maxLpcOrder];

        for (int i = 0; i < super.maxLpcOrder; i++) {
            gen[0][i] = gen[1][i] = autocorrelationFactors[i + 1];
        }

        error = autocorrelationFactors[0];
        super.reflectionCoefficients[0] = -gen[1][0] / error;
        error += gen[1][0] * super.reflectionCoefficients[0];

        for (int i = 1; i < super.maxLpcOrder; i++) {
            for (int j = 0; j < super.maxLpcOrder - i; j++) {
                gen[1][j] = gen[1][j + 1] + super.reflectionCoefficients[i - 1] * gen[0][j];
                gen[0][j] = gen[1][j + 1] * super.reflectionCoefficients[i - 1] + gen[0][j];
            }
            super.reflectionCoefficients[i] = -gen[1][0] / error;
            error += gen[1][0] * super.reflectionCoefficients[i];
        }
    }

    private void generateoptimalLpcOrder() {
        for (int i = super.maxLpcOrder - 1; i >= 0; i--) {
            if (Math.abs(super.reflectionCoefficients[i]) > 0.05) {
                super.optimalLpcOrder = (byte) (i + 1);
                break;
            }
        }
    }

    private void quantizeReflectionCoefficients() {
        super.quantizedReflectionCoefficients = new int[super.optimalLpcOrder];

        if (super.quantizedReflectionCoefficients.length > 0) {
            super.quantizedReflectionCoefficients[0] = (int) Math
                    .floor(64 * (-1 + (sqrt2 * Math.sqrt(super.reflectionCoefficients[0] + 1))));
        }
        if (super.quantizedReflectionCoefficients.length > 1) {
            super.quantizedReflectionCoefficients[1] = (int) Math
                    .floor(64 * (-1 + (sqrt2 * Math.sqrt(-super.reflectionCoefficients[1] + 1))));
        }
        for (int i = 2; i < super.optimalLpcOrder; i++) {
            super.quantizedReflectionCoefficients[i] = (int) Math.floor(64 * super.reflectionCoefficients[i]);
        }
    }

    private void generateResidues() {
        long correction = (long) 1 << (super.correctionFactor - 1);

        residues[0] = samples[0];

        for (int i = 1; i <= super.optimalLpcOrder; i++) {
            long temp = correction;
            for (int j = 1; j <= i; j++) {
                temp += super.linearPredictionCoefficients[j] * samples[i - j];
            }
            residues[i] = samples[i] - (int) (temp >> super.correctionFactor);
        }

        for (int i = super.optimalLpcOrder + 1; i < samples.length; i++) {
            long temp = correction;
            for (int j = 0; j <= super.optimalLpcOrder; j++) {
                temp += (super.linearPredictionCoefficients[j] * samples[i - j]);
            }
            residues[i] = samples[i] - (int) (temp >> super.correctionFactor);
        }
    }

    public LpcEncodedData process() {
        quantizeSamples();
        generateAutoCorrelation();
        generateReflectionCoefficients();
        generateoptimalLpcOrder();
        quantizeReflectionCoefficients();
        super.dequantizeReflectionCoefficients();
        super.generatelinearPredictionCoefficients();
        generateResidues();
        return new LpcEncodedData(optimalLpcOrder, quantizedReflectionCoefficients, residues);
    }
}