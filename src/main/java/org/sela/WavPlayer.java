package org.sela;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import org.sela.data.Progress;
import org.sela.data.WavFile;
import org.sela.data.WavFrame;
import org.sela.exception.FileException;
import org.sela.utils.ProgressPrinter;

public class WavPlayer {
    private final WavFile wavFile;
    private List<WavFrame> wavFrames;
    private int frameCount;
    private final int samplePerSubFrame = 2048;

    public WavPlayer(final File inputFile) throws IOException, FileException {
        wavFile = new WavFile(inputFile);
    }

    private void readSamples() {
        final long sampleCount = wavFile.getSampleCount();
        frameCount = (int) Math.ceil((double) sampleCount / (samplePerSubFrame * wavFile.getNumChannels()));
        wavFrames = new ArrayList<>(frameCount);

        for (int i = 0; i < frameCount; i++) {
            final int[][] samples = new int[wavFile.getNumChannels()][samplePerSubFrame];
            wavFile.readFrames(samples, samplePerSubFrame);
            wavFrames.add(new WavFrame(i, samples, (byte) wavFile.getBitsPerSample()));
        }
    }

    public void play() throws LineUnavailableException, InterruptedException, FileException, IOException {
        readSamples();

        // Select audio format parameters
        final AudioFormat af = new AudioFormat(wavFile.getSampleRate(), wavFile.getBitsPerSample(),
                wavFile.getNumChannels(), true, false);
        final DataLine.Info info = new DataLine.Info(SourceDataLine.class, af);
        final SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);

        // Prepare audio output
        line.open(af, 2048 * wavFile.getNumChannels());
        line.start();

        // Prepare print thread
        final Progress progress = new Progress(wavFrames.size());
        final Thread printThread = new Thread(new ProgressPrinter(progress));
        printThread.start();

        // Output wave form repeatedly
        byte bytesPerSample = (byte) ((byte) wavFile.getBitsPerSample() / 8);
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