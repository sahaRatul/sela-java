package org.sela.data;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public final class Frame implements Comparable<Frame> {
    public final int syncWord = 0xAA55FF00;
    public ArrayList<SubFrame> subFrames;
    private final int index; // For internal sorting, not to be written to output

    public Frame(final ArrayList<SubFrame> subFrames, final int index) {
        this.subFrames = subFrames;
        this.index = index;
    }

    public Frame(final int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
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