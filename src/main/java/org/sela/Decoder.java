package org.sela;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.sela.data.*;
import org.sela.exception.FileException;
import org.sela.frame.FrameDecoder;

public class Decoder {
    protected WavFile wavFile;
    protected SelaFile selaFile;
    private final File outputFile;

    public Decoder(final File inputFile, final File outputFile) throws FileNotFoundException {
        this.selaFile = new SelaFile(new FileInputStream(inputFile));
        this.outputFile = outputFile;
    }

    private void readFrames() throws IOException, FileException {
        selaFile.readFromStream();
    }

    protected List<WavFrame> processFrames() throws IOException, FileException {
        readFrames();

        // Decode frames in parallel
        final List<WavFrame> wavFrames = selaFile.getFrames().parallelStream().map(x -> new FrameDecoder(x).process())
                .collect(Collectors.toList());

        // Sort decoded samples
        Collections.sort(wavFrames);

        return wavFrames;
    }

    public WavFile process() throws IOException, FileException {
        final List<WavFrame> wavFrames = processFrames();

        final WavFile wavFile = new WavFile(selaFile.getSampleRate(), selaFile.getBitsPerSample(),
                selaFile.getChannels(), wavFrames, new FileOutputStream(outputFile));
        this.wavFile = wavFile;
        return wavFile;
    }
}