package org.sela.data;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public final class Frame implements Comparable<Frame> {
    public final int syncWord = 0xAA55FF00;
    public ArrayList<SubFrame> subFrames;
    private final byte bitsPerSample; //For internal reference, not to be written on output
    private final int index; // For internal sorting, not to be written to output

    public Frame(final ArrayList<SubFrame> subFrames, final byte bitsPerSample, final int index) {
        this.subFrames = subFrames;
        this.bitsPerSample = bitsPerSample;
        this.index = index;
    }

    public Frame(final int index, final byte bitsPerSample) {
        this.index = index;
        this.bitsPerSample = bitsPerSample;
    }

    public int getIndex() {
        return index;
    }

    public byte getBitsPerSample() {
        return bitsPerSample;
    }

    @Override
    public int compareTo(final Frame frame) {
        return this.index - frame.index;
    }

    public int getByteCount() {
        int count = 4;
        for (final SubFrame subFrame : subFrames) {
            count += subFrame.getByteCount();
        }
        return count;
    }

    public void write(final ByteBuffer buffer) {
        buffer.putInt(syncWord);
        for (final SubFrame subFrame : subFrames) {
            subFrame.write(buffer);
        }
    }
}