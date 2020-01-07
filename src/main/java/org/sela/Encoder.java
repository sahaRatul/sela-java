package org.sela;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.sela.data.*;
import org.sela.frame.FrameEncoder;
import org.sela.exception.*;

public class Encoder {
    private WavFile wavFile;
    private File outputFile;
    private List<WavFrame> wavFrames;
    private int frameCount;
    private final int samplePerSubFrame = 2048;

    public Encoder(File inputFile, File outputFile) throws FileException, IOException {
        wavFile = new WavFile(inputFile);
        this.outputFile = outputFile;
    }

    private void readSamples() {
        long sampleCount = wavFile.getSampleCount();
        frameCount = (int) Math.ceil((double) sampleCount / (samplePerSubFrame * wavFile.getNumChannels()));
        wavFrames = new ArrayList<>(frameCount);

        int [][][] samples = new int[frameCount][wavFile.getNumChannels()][samplePerSubFrame];
        for (int i = 0; i < frameCount; i++) {
            wavFile.readFrames(samples[i], samplePerSubFrame);
            wavFrames.add(new WavFrame(i, samples[i]));
        }
    }

    public SelaFile process() throws IOException {
        readSamples();

        // Encode samples2 in parallel
        List<Frame> frames = wavFrames.parallelStream()
                .map(x -> (new FrameEncoder(x)).process()).collect(Collectors.toList());

        // Sort encoded frames
        Collections.sort(frames);

        return new SelaFile(wavFile.getSampleRate(), wavFile.getBitsPerSample(), (byte) wavFile.getNumChannels(),
                frames, new FileOutputStream(outputFile));
    }
}