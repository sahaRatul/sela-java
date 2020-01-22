package org.sela;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import org.sela.data.Progress;
import org.sela.data.WavFrame;
import org.sela.exception.FileException;
import org.sela.utils.ProgressPrinter;

public class Player {
    private final List<WavFrame> wavFrames;
    private final Decoder decoder;

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

        // Prepare print thread
        final Progress progress = new Progress(wavFrames.size());
        final Thread printThread = new Thread(new ProgressPrinter(progress));
        printThread.start();

        // Output wave form repeatedly
        byte bytesPerSample = (byte) ((byte) decoder.selaFile.getBitsPerSample() / 8);
        for (int i = 0; i < wavFrames.size(); i++) {
            final byte[] bytes = wavFrames.get(i).getDemuxedSamplesInByteArray(bytesPerSample);
            line.write(bytes, 0, bytes.length);
            progress.current++;
        }
        printThread.join();
        line.drain();
        line.stop();
        line.close();
    }
}
