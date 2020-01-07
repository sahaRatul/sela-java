package org.sela;

import java.io.File;
import java.io.IOException;

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

    public void play() {
        try {
            // select audio format parameters
            AudioFormat af = new AudioFormat(super.selaFile.getSampleRate(), super.selaFile.getBitsPerSample(),
                    super.selaFile.getChannels(), true, false);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, af);
            SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);

            // prepare audio output
            line.open(af, 4096);
            line.start();

            // output wave form repeatedly
            for (int i = 0; i < wavFrames.size(); i++) {
                // line.write(buffer, 0, buffer.length);
                byte[] bytes = wavFrames.get(i).getDemuxedShortSamplesInByteArray();
                line.write(bytes, 0, bytes.length);
            }

            line.drain();
            line.stop();
            line.close();

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}