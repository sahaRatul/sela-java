package org.sela;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.sela.data.Frame;
import org.sela.data.SelaFile;
import org.sela.data.WavFile;
import org.sela.frame.FrameEncoder;
import org.sela.exception.*;

public class Encoder {
    WavFile wavFile;
    File outputFile;
    int[][][] samples;
    private final int samplePerSubFrame = 2048;

    public Encoder(File inputFile, File outputFile) throws WavFileException, IOException {
        wavFile = new WavFile(inputFile);
        this.outputFile = outputFile;
    }

    private void readSamples() {
        long sampleCount = wavFile.getSampleCount();
        int selaFrameCount = (int) Math.ceil((double) sampleCount / (samplePerSubFrame));

        samples = new int[selaFrameCount][wavFile.getNumChannels()][samplePerSubFrame];
        for (int i = 0; i < selaFrameCount; i++) {
            wavFile.readFrames(samples[i], 2048);
        }
    }

    public SelaFile process() throws IOException {
        readSamples();
        List<int[][]> listSamples = Arrays.asList(samples);
        List<Frame> frames = listSamples.parallelStream()
                .map(x -> (new FrameEncoder(x, listSamples.indexOf(x))).process()).collect(Collectors.toList());
        Collections.sort(frames);
        return new SelaFile(wavFile.getSampleRate(), wavFile.getBitsPerSample(), (byte) wavFile.getNumChannels(),
                frames, new FileOutputStream(outputFile));
    }
}