package org.sela;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import org.sela.data.WavFrame;
import org.sela.exception.FileException;

public class Player {
    private List<WavFrame> wavFrames;
    private Decoder decoder;

    public Player(final File inputFile) throws IOException, FileException {
        decoder = new Decoder(inputFile, null);
        wavFrames = decoder.processFrames();
    }

    public void play() throws LineUnavailableException, InterruptedException {
        // Select audio format parameters
        final AudioFormat af = new AudioFormat(decoder.selaFile.getSampleRate(), decoder.selaFile.getBitsPerSample(),
                decoder.selaFile.getChannels(), true, false);
        final DataLine.Info info = new DataLine.Info(SourceDataLine.class, af);
        final SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);

        // Prepare audio output
        line.open(af, 2048 * decoder.selaFile.getChannels());
        line.start();

        //Prepare print thread
        PlayProgress progress = new PlayProgress(wavFrames.size());
        Thread printThread = new Thread(new ProgressPrinter(progress));
        printThread.start();

        // Output wave form repeatedly
        for (int i = 0; i < wavFrames.size(); i++) {
            final byte[] bytes = wavFrames.get(i).getDemuxedShortSamplesInByteArray();
            line.write(bytes, 0, bytes.length);
            progress.currentFrameNumber++;
        }
        printThread.join();
        line.drain();
        line.stop();
        line.close();
    }
}

// A separate thread for printing is required since audio lags when we print as well as play audio on single thread on Windows. 
class ProgressPrinter implements Runnable {
    private PlayProgress progress;
    public ProgressPrinter(PlayProgress progress) {
        this.progress = progress;
    }

    public void run() {
        while (progress.currentFrameNumber < progress.totalFrameCount) {
            printProgress(progress.currentFrameNumber, progress.totalFrameCount);
        }
        printProgress(progress.currentFrameNumber, progress.totalFrameCount); //Print one last time to make it 100%
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

// Data Class for keeping track of progress. Will be shared between audio thread and print thread
class PlayProgress {
    public volatile int currentFrameNumber;
    public final int totalFrameCount;

    public PlayProgress(int totalFrameCount) {
        currentFrameNumber = 0;
        this.totalFrameCount = totalFrameCount;
    }
}