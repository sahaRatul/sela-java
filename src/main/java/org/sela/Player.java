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

public class Player extends Decoder {
    private List<WavFrame> wavFrames;

    public Player(final File inputFile) throws IOException, FileException {
        super(inputFile, null);
        wavFrames = super.processFrames();
    }

    private static void printProgress(final long current, final long total) {
        final StringBuilder string = new StringBuilder(140);
        final int percent = (int) (current * 100 / total);
        string.append('\r').append(String.format("%d%% [", percent))
                .append(String.join("", Collections.nCopies(percent / 2, "="))).append("\u001B[1m>\u001B[0m")
                .append(String.join("", Collections.nCopies(50 - (percent / 2), " "))).append(']').append(" (")
                .append(current).append('/').append(total).append(')');
        System.out.print(string);
    }

    public void play() throws LineUnavailableException {
        // Select audio format parameters
        final AudioFormat af = new AudioFormat(super.selaFile.getSampleRate(), super.selaFile.getBitsPerSample(),
                super.selaFile.getChannels(), true, false);
        final DataLine.Info info = new DataLine.Info(SourceDataLine.class, af);
        final SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);

        // Prepare audio output
        line.open(af, 2048 * super.selaFile.getChannels());
        line.start();

        // Output wave form repeatedly
        for (int i = 0; i < wavFrames.size(); i++) {
            final byte[] bytes = wavFrames.get(i).getDemuxedShortSamplesInByteArray();
            line.write(bytes, 0, bytes.length);
            Player.printProgress((i + 1), wavFrames.size());
        }
        line.drain();
        line.stop();
        line.close();
    }
}