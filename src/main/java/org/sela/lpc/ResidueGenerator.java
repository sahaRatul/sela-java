package org.sela.lpc;

public final class ResidueGenerator {
    private int[] samples;
    private int[] quantizedReflectionCoefficients;
    private int[] residues;
    private double[] quantizedSamples;
    private double[] autocorrelationFactors;
    private double[] reflectionCoefficients;
    private long[] linearPredictionCoefficients;

    private byte optimalOrder;

    private final int correctionFactor = 35;
    private final int maxShort = 32767;
    private final int maxLpcOrder = 100;
    private final double sqrt2 = 1.4142135623730950488016887242096;

    public ResidueGenerator(int[] samples) {
        this.samples = samples;
        this.quantizedSamples = new double[samples.length];
        this.autocorrelationFactors = new double[maxLpcOrder + 1];
        this.reflectionCoefficients = new double[maxLpcOrder];
        this.residues = new int[samples.length];
        optimalOrder = 1;
    }

    private void quantizeSamples() {
        for (int i = 0; i < samples.length; i++) {
            quantizedSamples[i] = samples[i] / maxShort;
        }
    }

    private void generateAutoCorrelation() {
        double sum = 0, mean = 0;

        // Generate Mean of samples
        for (int i = 0; i < quantizedSamples.length; i++) {
            sum += quantizedSamples[i];
        }
        mean = sum / quantizedSamples.length;

        // Generate reflection coefficients
        for (int i = 0; i <= maxLpcOrder; i++) {
            autocorrelationFactors[i] = 0.0;
            for (int j = i; j < quantizedSamples.length; j++) {
                autocorrelationFactors[i] += (quantizedSamples[j] - mean) * (quantizedSamples[j - i] - mean);
            }
        }
    }

    private void generateReflectionCoefficients() {
        double error;
        double[][] gen = new double[2][maxLpcOrder];

        for (int i = 0; i < maxLpcOrder; i++) {
            gen[0][i] = gen[1][i] = autocorrelationFactors[i];
        }

        error = autocorrelationFactors[0];
        reflectionCoefficients[0] = -gen[1][0] / error;
        error += gen[1][0] * reflectionCoefficients[0];

        for (int i = 1; i < maxLpcOrder; i++) {
            for (int j = 0; j < maxLpcOrder - i; j++) {
                gen[1][j] = gen[1][j + 1] + reflectionCoefficients[i - 1] * gen[0][j];
                gen[0][j] = gen[1][j + 1] * reflectionCoefficients[i - 1] + gen[0][j];
            }
            reflectionCoefficients[i] = -gen[1][0] / error;
            error += gen[1][0] * reflectionCoefficients[i];
        }
    }

    private void generateOptimalOrder() {
        for (int i = maxLpcOrder - 1; i >= 0; i--) {
            if (Math.abs(reflectionCoefficients[i]) > 0.05) {
                optimalOrder = (byte) (i + 1);
                break;
            }
        }
    }

    private void quantizeReflectionCoefficients() {
        quantizedReflectionCoefficients = new int[optimalOrder];

        quantizedReflectionCoefficients[0] = (int) Math
                .floor(64 * (-1 + (sqrt2 * Math.sqrt(reflectionCoefficients[0] + 1))));
        quantizedReflectionCoefficients[1] = (int) Math
                .floor(64 * (-1 + (sqrt2 * Math.sqrt(-reflectionCoefficients[1] + 1))));
        for (int i = 2; i < optimalOrder; i++) {
            quantizedReflectionCoefficients[i] = (int) Math.floor(64 * reflectionCoefficients[i]);
        }
    }

    private void dequantizeReflectionCoefficients() {
        if (optimalOrder <= 1) {
            reflectionCoefficients[0] = 0;
            return;
        }

        reflectionCoefficients[0] = LookupTables.firstOrderCoefficients[quantizedReflectionCoefficients[0] + 64];
        reflectionCoefficients[1] = LookupTables.secondOrderCoefficients[quantizedReflectionCoefficients[1] + 64];
        for (int i = 2; i < optimalOrder; i++) {
            reflectionCoefficients[i] = LookupTables.higherOrderCoefficients[quantizedReflectionCoefficients[i] + 64];
        }
    }

    private void generatelinearPredictionCoefficients() {
        linearPredictionCoefficients = new long[optimalOrder + 1];
        double[][] linearPredictionCoefficientMatrix = new double[optimalOrder][optimalOrder];
        double[] lpcTmp = new double[optimalOrder];
        long correction = (long) 1 << correctionFactor;

        // Generate LPC matrix
        for (int i = 0; i < optimalOrder; i++) {
            lpcTmp[i] = reflectionCoefficients[i];
            int i2 = i >> 1;
            int j = 0;
            for (j = 0; j < i2; j++) {
                double tmp = lpcTmp[j];
                lpcTmp[j] += reflectionCoefficients[i] * lpcTmp[i - 1 - j];
                lpcTmp[i - 1 - j] += reflectionCoefficients[i] * tmp;
            }
            if (i > 1) {
                lpcTmp[j] += lpcTmp[j] * reflectionCoefficients[i];
            }

            for (j = 0; j <= i; j++) {
                linearPredictionCoefficientMatrix[i][j] = -lpcTmp[j];
            }
        }

        // Select optimum order row from matrix
        for (int i = 0; i < optimalOrder; i++) {
            linearPredictionCoefficients[i + 1] = (long) (correction * linearPredictionCoefficientMatrix[optimalOrder - 1][i]);
        }
    }

    private void generateResidues() {
        long correction = (long) 1 << correctionFactor;
        
        residues[0] = samples[0];
        
        for (int i = 1; i <= optimalOrder; i++) {
            long temp = correction;
            for (int j = 1; j <= i; j++) {
                temp += linearPredictionCoefficients[j] * samples[i - j];
            }
            residues[i] = samples[i] - (int) (temp >> correctionFactor);
        }

        for (int i = optimalOrder + 1; i < samples.length; i++) {
            long temp = correction;
            for (int j = 0; j <= optimalOrder; j++)
                temp += (linearPredictionCoefficients[j] * samples[i - j]);
            residues[i] = samples[i] - (int) (temp >> correctionFactor);
        }
    }

    public int[] process() {
        quantizeSamples();
        generateAutoCorrelation();
        generateReflectionCoefficients();
        generateOptimalOrder();
        quantizeReflectionCoefficients();
        dequantizeReflectionCoefficients();
        generatelinearPredictionCoefficients();
        generateResidues();

        return residues;
    }
}