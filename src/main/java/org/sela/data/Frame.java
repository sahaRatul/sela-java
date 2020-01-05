package org.sela.data;

import java.util.ArrayList;

public final class Frame implements Comparable<Frame> {
    public final int syncWord = 0xAA55FF00;
    public ArrayList<SubFrame> subFrames;
    private int index; //For internal sorting, not to be written to output

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
}