package org.sela.data;

public final class LpcDecodedData {
    public final int[] samples;
    public final byte bitsPerSample;

    public LpcDecodedData(final int[] samples, final byte bitsPerSample) {
        this.samples = samples;
        this.bitsPerSample = bitsPerSample;
    }
}