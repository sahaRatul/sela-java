package org.sela.data;

// Data Class for keeping track of progress. Will be shared between audio thread
// and print thread
public class Progress {
    public volatile int current;
    public final int total;

    public Progress(final int total) {
        current = 0;
        this.total = total;
    }
}