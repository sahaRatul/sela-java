package org.sela.utils;

import java.util.Collections;

import org.sela.data.Progress;

// A separate thread for printing is required since audio lags when we print as
// well as play audio on single thread on Windows.
public class ProgressPrinter implements Runnable {
    private final Progress progress;

    public ProgressPrinter(final Progress progress) {
        this.progress = progress;
    }

    public void run() {
        while (progress.current < progress.total) {
            printProgress(progress.current, progress.total);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        printProgress(progress.current, progress.total); // Print one last time to make it 100%
    }

    private void printProgress(final long current, final long total) {
        final StringBuilder string = new StringBuilder(140);
        final int percent = (int) (current * 100 / total);
        string.append('\r').append(String.format("%d%% [", percent))
                .append(String.join("", Collections.nCopies(percent / 2, "="))).append(">")
                .append(String.join("", Collections.nCopies(50 - (percent / 2), " "))).append(']').append(" (")
                .append(current).append('/').append(total).append(')');
        System.out.print(string);
    }
}