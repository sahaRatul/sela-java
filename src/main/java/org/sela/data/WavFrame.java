package org.sela.data;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class WavFrame implements Comparable<WavFrame> {
    private final int index;
    private final byte bitsPerSample;
    public int[][] samples;

    public WavFrame(final int index, final int[][] samples, final byte bitsPerSample) {
        this.index = index;
        this.samples = samples;
        this.bitsPerSample = bitsPerSample;
    }

    public int getIndex() {
        return index;
    }

    public byte getBitsPerSample() {
        return bitsPerSample;
    }

    @Override
    public int compareTo(final WavFrame frame) {
        return this.index - frame.index;
    }

    public int getSizeInBytes() {
        return samples.length * samples[0].length * (bitsPerSample / 8);
    }

    public byte[] getDemuxedSamplesInByteArray(final byte bytesPerSample) {
        // Demux
        final int[] demuxed = new int[samples.length * samples[0].length];
        for (int i = 0; i < samples.length; i++) {
            for (int j = 0; j < samples[i].length; j++) {
                demuxed[j * samples.length + i] = samples[i][j];
            }
        }

        // Write to buffer
        final byte[] bytes = new byte[demuxed.length * bytesPerSample];
        final ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        if (bytesPerSample == 2) {
            for (int i = 0; i < demuxed.length; i++) {
                buffer.putShort((short)demuxed[i]);
            }
        }
        if (bytesPerSample == 3) {
            for (int i = 0; i < demuxed.length; i++) {
                for (int j = bytesPerSample; j >= 1; j--) {
                    buffer.put((byte)(demuxed[i] >>> (j * 8)));
                }
            }
        }
        return buffer.array();
    }
}