package org.sela.data;

public class WavFrame implements Comparable<WavFrame> {
    private int index;
    public int[][] samples;

    public WavFrame(int index, int[][] samples) {
        this.index = index;
        this.samples = samples;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public int compareTo(WavFrame frame) {
        return this.index - frame.index;
    }
}