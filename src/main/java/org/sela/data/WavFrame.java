package org.sela.data;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class WavFrame implements Comparable<WavFrame> {
    private final int index;
    public int[][] samples;

    public WavFrame(final int index, final int[][] samples) {
        this.index = index;
        this.samples = samples;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public int compareTo(final WavFrame frame) {
        return this.index - frame.index;
    }

    public int getSizeInBytes() {
        return samples.length * samples[0].length * 2; // Assuming 16 bit samples
    }

    public byte[] getDemuxedShortSamplesInByteArray() {
        // Demux
        final short[] demuxed = new short[samples.length * samples[0].length];
        for (int i = 0; i < samples.length; i++) {
            for (int j = 0; j < samples[i].length; j++) {
                demuxed[j * samples.length + i] = (short) samples[i][j];
            }
        }

        // Write to buffer
        final byte[] bytes = new byte[demuxed.length * 2];
        final ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < demuxed.length; i++) {
            buffer.putShort(demuxed[i]);
        }
        return buffer.array();
    }
}