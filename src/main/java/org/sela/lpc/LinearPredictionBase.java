package org.sela.lpc;

public class LinearPredictionBase {
    protected double[] reflectionCoefficients;
    protected int[] quantizedReflectionCoefficients;
    protected long[] linearPredictionCoefficients;

    protected byte optimalOrder;

    protected final int maxLpcOrder = 100;
    protected final int correctionFactor = 35;

    public LinearPredictionBase() {
        this.reflectionCoefficients = new double[maxLpcOrder];
        optimalOrder = 1;
    }

    public LinearPredictionBase(int[] quantizedReflectionCoefficients, byte optimalOrder) {
        this.reflectionCoefficients = new double[maxLpcOrder];
        this.quantizedReflectionCoefficients = quantizedReflectionCoefficients;
        this.optimalOrder = optimalOrder;
    }

    protected void dequantizeReflectionCoefficients() {
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

    protected void generatelinearPredictionCoefficients() {
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
            if (i % 2 == 1) {
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
}