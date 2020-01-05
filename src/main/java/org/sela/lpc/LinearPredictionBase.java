package org.sela.lpc;

public class LinearPredictionBase {
    protected double[] reflectionCoefficients;
    protected int[] quantizedReflectionCoefficients;
    protected long[] linearPredictionCoefficients;

    protected byte optimalLpcOrder;

    protected final int maxLpcOrder = 100;
    protected final int correctionFactor = 35;

    public LinearPredictionBase() {
        this.reflectionCoefficients = new double[maxLpcOrder];
        optimalLpcOrder = 1;
    }

    public LinearPredictionBase(int[] quantizedReflectionCoefficients, byte optimalLpcOrder) {
        this.reflectionCoefficients = new double[maxLpcOrder];
        this.quantizedReflectionCoefficients = quantizedReflectionCoefficients;
        this.optimalLpcOrder = optimalLpcOrder;
    }

    protected void dequantizeReflectionCoefficients() {
        if (optimalLpcOrder <= 1) {
            reflectionCoefficients[0] = 0;
            return;
        }

        reflectionCoefficients[0] = LookupTables.firstOrderCoefficients[quantizedReflectionCoefficients[0] + 64];
        reflectionCoefficients[1] = LookupTables.secondOrderCoefficients[quantizedReflectionCoefficients[1] + 64];
        for (int i = 2; i < optimalLpcOrder; i++) {
            reflectionCoefficients[i] = LookupTables.higherOrderCoefficients[quantizedReflectionCoefficients[i] + 64];
        }
    }

    protected void generatelinearPredictionCoefficients() {
        linearPredictionCoefficients = new long[optimalLpcOrder + 1];
        double[][] linearPredictionCoefficientMatrix = new double[optimalLpcOrder][optimalLpcOrder];
        double[] lpcTmp = new double[optimalLpcOrder];
        long correction = (long) 1 << correctionFactor;

        // Generate LPC matrix
        for (int i = 0; i < optimalLpcOrder; i++) {
            lpcTmp[i] = reflectionCoefficients[i];
            int i2 = i >> 1;
            int j = 0;
            for (j = 0; j < i2; j++) {
                double tmp = lpcTmp[j];
                lpcTmp[j] += reflectionCoefficients[i] * lpcTmp[i - 1 - j];
                lpcTmp[i - 1 - j] += reflectionCoefficients[i] * tmp;
            }
            if (i % 2 == 1) {
                lpcTmp[j] += lpcTmp[j] * reflectionCoefficients[i];
            }

            for (j = 0; j <= i; j++) {
                linearPredictionCoefficientMatrix[i][j] = -lpcTmp[j];
            }
        }

        // Select optimum order row from matrix
        for (int i = 0; i < optimalLpcOrder; i++) {
            linearPredictionCoefficients[i
                    + 1] = (long) (correction * linearPredictionCoefficientMatrix[optimalLpcOrder - 1][i]);
        }
    }
}