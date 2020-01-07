package org.sela;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

import org.sela.exception.FileException;

public class Player extends Decoder {
    public Player(File inputFile) throws IOException, FileException {
        super(inputFile, null);
        super.process();
    }

    private static void printProgress(long current, long total) {
        StringBuilder string = new StringBuilder(140);
        int percent = (int) (current * 100 / total);
        string.append('\r')
                .append(String.join("", Collections.nCopies(percent == 0 ? 2 : 2 - (int) (Math.log10(percent)), " ")))
                .append(String.format(" %d%% [", percent)).append(String.join("", Collections.nCopies(percent, "=")))
                .append('>').append(String.join("", Collections.nCopies(100 - percent, " "))).append(']')
                .append(String.join("",
                        Collections.nCopies(current == 0 ? (int) (Math.log10(total))
                                : (int) (Math.log10(total)) - (int) (Math.log10(current)), " ")));

        System.out.print(string);
    }

    public void play() {
        try {
            // Select audio format parameters
            AudioFormat af = new AudioFormat(super.selaFile.getSampleRate(), super.selaFile.getBitsPerSample(),
                    super.selaFile.getChannels(), true, false);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, af);
            SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);

            // Prepare audio output
            line.open(af, 4096);
            line.start();

            // Output wave form repeatedly
            for (int i = 0; i < wavFrames.size(); i++) {
                byte[] bytes = wavFrames.get(i).getDemuxedShortSamplesInByteArray();
                line.write(bytes, 0, bytes.length);
                Player.printProgress(i, wavFrames.size());
            }

            line.drain();
            line.stop();
            line.close();

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}