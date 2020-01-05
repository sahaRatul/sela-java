package org.sela;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import org.sela.data.Frame;
import org.sela.frame.FrameDecoder;
import org.sela.wav.WavFile;

public class Decoder {
    WavFile wavFile;
    File outputFile;
    
    public Decoder(File outputFile) {
        this.outputFile = outputFile;
    }

    public List<int[][]> process(List<Frame> frames) {
        List<int[][]> samples = frames.stream().map(x -> new FrameDecoder(x).process()).collect(Collectors.toList());
        System.out.println("Decoded");
        return samples;
    }
}