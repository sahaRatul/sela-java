package org.sela.data;

public final class LpcEncodedData {
    public byte optimalLpcOrder;
    public int[] quantizedReflectionCoefficients;
    public int[] residues;

    public LpcEncodedData(byte optimalLpcOrder, int[] quantizedReflectionCoefficients, int[] residues) {
        this.optimalLpcOrder = optimalLpcOrder;
        this.quantizedReflectionCoefficients = quantizedReflectionCoefficients;
        this.residues = residues;
    }
}