package org.sela.lpc;

import org.sela.data.LpcDecodedData;
import org.sela.data.LpcEncodedData;

public final class ResidueGenerator {
    private final int[] samples;
    private final int[] residues;
    private final double[] quantizedSamples;
    private final double[] autocorrelationFactors;
    private final LinearPredictor linearPredictor;
    private final byte bitsPerSample;

    private final int quantizationFactor;
    private final double sqrt2 = 1.4142135623730950488016887242096;

    public ResidueGenerator(final LpcDecodedData data) {
        this.linearPredictor = new LinearPredictor();
        this.samples = data.samples;
        this.quantizedSamples = new double[samples.length];
        this.autocorrelationFactors = new double[linearPredictor.maxLpcOrder + 1];
        this.residues = new int[samples.length];
        this.bitsPerSample = data.bitsPerSample;
        this.quantizationFactor = data.bitsPerSample == 16 ? 32767 : 2147483647;
    }

    private void quantizeSamples() {
        for (int i = 0; i < samples.length; i++) {
            quantizedSamples[i] = (double) samples[i] / quantizationFactor;
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
        for (int i = 0; i <= linearPredictor.maxLpcOrder; i++) {
            autocorrelationFactors[i] = 0.0;
            for (int j = i; j < quantizedSamples.length; j++) {
                autocorrelationFactors[i] += (quantizedSamples[j] - mean) * (quantizedSamples[j - i] - mean);
            }
        }

        // Normalise the coefficients
        for (int i = 1; i <= linearPredictor.maxLpcOrder; i++) {
            autocorrelationFactors[i] /= autocorrelationFactors[0];
        }
        autocorrelationFactors[0] = 1.0;
    }

    private void generateReflectionCoefficients() {
        double error;
        final double[][] gen = new double[2][linearPredictor.maxLpcOrder];

        for (int i = 0; i < linearPredictor.maxLpcOrder; i++) {
            gen[0][i] = gen[1][i] = autocorrelationFactors[i + 1];
        }

        error = autocorrelationFactors[0];
        linearPredictor.reflectionCoefficients[0] = -gen[1][0] / error;
        error += gen[1][0] * linearPredictor.reflectionCoefficients[0];

        for (int i = 1; i < linearPredictor.maxLpcOrder; i++) {
            for (int j = 0; j < linearPredictor.maxLpcOrder - i; j++) {
                gen[1][j] = gen[1][j + 1] + linearPredictor.reflectionCoefficients[i - 1] * gen[0][j];
                gen[0][j] = gen[1][j + 1] * linearPredictor.reflectionCoefficients[i - 1] + gen[0][j];
            }
            linearPredictor.reflectionCoefficients[i] = -gen[1][0] / error;
            error += gen[1][0] * linearPredictor.reflectionCoefficients[i];
        }
    }

    private void generateoptimalLpcOrder() {
        for (int i = linearPredictor.maxLpcOrder - 1; i >= 0; i--) {
            if (Math.abs(linearPredictor.reflectionCoefficients[i]) > 0.05) {
                linearPredictor.optimalLpcOrder = (byte) (i + 1);
                break;
            }
        }
    }

    private void quantizeReflectionCoefficients() {
        linearPredictor.quantizedReflectionCoefficients = new int[linearPredictor.optimalLpcOrder];

        if (linearPredictor.quantizedReflectionCoefficients.length > 0) {
            linearPredictor.quantizedReflectionCoefficients[0] = (int) Math
                    .floor(64 * (-1 + (sqrt2 * Math.sqrt(linearPredictor.reflectionCoefficients[0] + 1))));
        }
        if (linearPredictor.quantizedReflectionCoefficients.length > 1) {
            linearPredictor.quantizedReflectionCoefficients[1] = (int) Math
                    .floor(64 * (-1 + (sqrt2 * Math.sqrt(-linearPredictor.reflectionCoefficients[1] + 1))));
        }
        for (int i = 2; i < linearPredictor.optimalLpcOrder; i++) {
            linearPredictor.quantizedReflectionCoefficients[i] = (int) Math
                    .floor(64 * linearPredictor.reflectionCoefficients[i]);
        }
    }

    private void generateResidues() {
        final long correction = (long) 1 << (linearPredictor.correctionFactor - 1);

        residues[0] = samples[0];

        for (int i = 1; i <= linearPredictor.optimalLpcOrder; i++) {
            long temp = correction;
            for (int j = 1; j <= i; j++) {
                temp += linearPredictor.linearPredictionCoefficients[j] * samples[i - j];
            }
            residues[i] = samples[i] - (int) (temp >> linearPredictor.correctionFactor);
        }

        for (int i = linearPredictor.optimalLpcOrder + 1; i < samples.length; i++) {
            long temp = correction;
            for (int j = 0; j <= linearPredictor.optimalLpcOrder; j++) {
                temp += (linearPredictor.linearPredictionCoefficients[j] * samples[i - j]);
            }
            residues[i] = samples[i] - (int) (temp >> linearPredictor.correctionFactor);
        }
    }

    public LpcEncodedData process() {
        quantizeSamples();
        generateAutoCorrelation();
        generateReflectionCoefficients();
        generateoptimalLpcOrder();
        quantizeReflectionCoefficients();
        linearPredictor.dequantizeReflectionCoefficients();
        linearPredictor.generatelinearPredictionCoefficients();
        generateResidues();
        return new LpcEncodedData(linearPredictor.optimalLpcOrder, bitsPerSample,
                linearPredictor.quantizedReflectionCoefficients, residues);
    }
}