package org.sela.data;

public final class LpcEncodedData {
    public final byte optimalLpcOrder;
    public final byte bitsPerSample;
    public final int[] quantizedReflectionCoefficients;
    public final int[] residues;

    public LpcEncodedData(final byte optimalLpcOrder, final byte bitsPerSample, final int[] quantizedReflectionCoefficients,
            final int[] residues) {
        this.optimalLpcOrder = optimalLpcOrder;
        this.bitsPerSample = bitsPerSample;
        this.quantizedReflectionCoefficients = quantizedReflectionCoefficients;
        this.residues = residues;
    }
}