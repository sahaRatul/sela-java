package org.sela.data;

import java.util.ArrayList;

public final class Frame {
    public final int syncword = 0xAA55FF00;
    public ArrayList<SubFrame> subFrames;

    public Frame(ArrayList<SubFrame> subFrames) {
        this.subFrames = subFrames;
    }
}