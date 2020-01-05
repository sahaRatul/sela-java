package org.sela.data;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public final class Frame implements Comparable<Frame> {
    public final int syncWord = 0xAA55FF00;
    public ArrayList<SubFrame> subFrames;
    private int index; // For internal sorting, not to be written to output

    public Frame(ArrayList<SubFrame> subFrames, int index) {
        this.subFrames = subFrames;
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public int compareTo(Frame frame) {
        return this.index - frame.index;
    }

    public int getByteCount() {
        int count = 4;
        for (SubFrame subFrame : subFrames) {
            count += subFrame.getByteCount();
        }
        return count;
    }

    public void write(ByteBuffer buffer) {
        buffer.putInt(syncWord);
        for (SubFrame subFrame : subFrames) {
            subFrame.write(buffer);
        }
    }
}