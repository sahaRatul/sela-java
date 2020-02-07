package org.sela.data;

import java.util.concurrent.atomic.AtomicInteger;

// Data Class for keeping track of progress. Will be shared between audio thread
// and print thread
public class Progress {
    private AtomicInteger current;
    public final int total;

    public Progress(final int total) {
        current = new AtomicInteger(0);
        this.total = total;
    }

    public int getCurrent() {
        return current.get();
    }

    public void setCurrent(int value) {
        current.set(value);
    }
}