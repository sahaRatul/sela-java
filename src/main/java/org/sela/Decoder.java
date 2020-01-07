package org.sela;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
    File outputFile;
    protected List<WavFrame> wavFrames;

    public Decoder(File inputFile, File outputFile) throws FileNotFoundException {
        this.selaFile = new SelaFile(new FileInputStream(inputFile));
        this.outputFile = outputFile;
    }

    private void readFrames() throws IOException, FileException {
        selaFile.readFromStream();
    }

    public List<WavFrame> process() throws IOException, FileException {
        readFrames();
        
        // Decode frames in parallel
        List<WavFrame> wavFrames = selaFile.getFrames().parallelStream().map(x -> new FrameDecoder(x).process())
                .collect(Collectors.toList());
        
        // Sort decoded samples
        Collections.sort(wavFrames);
        this.wavFrames = wavFrames;

        return wavFrames;
    }
}