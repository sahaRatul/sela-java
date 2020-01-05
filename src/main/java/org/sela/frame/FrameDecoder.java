package org.sela.frame;

import org.sela.data.*;
import org.sela.lpc.SampleGenerator;
import org.sela.rice.RiceDecoder;

public class FrameDecoder {
    private Frame frame;

    public FrameDecoder(Frame frame) {
        this.frame = frame;
    }

    public int[][] process() {
        int[][] samples = new int[frame.subFrames.size()][];
        
        // Foreach subFrame
        for (SubFrame subFrame : frame.subFrames) {
            // Stage 1 - Extract data from subFrame
            byte channel = subFrame.channel;
            byte optimumLpcOrder = subFrame.optimumLpcOrder;
            RiceEncodedData reflectionData = new RiceEncodedData(subFrame.reflectionCoefficientRiceParam, subFrame.optimumLpcOrder, subFrame.encodedReflectionCoefficients);
            RiceEncodedData residueData = new RiceEncodedData(subFrame.residueRiceParam, subFrame.samplesPerChannel, subFrame.encodedResidues);

            //Stage 2 - Decompress data
            RiceDecodedData decodedReflectionData = (new RiceDecoder(reflectionData)).process();
            RiceDecodedData decodedResidueData = (new RiceDecoder(residueData)).process();

            // Stage 3 - Generate Samples
            LpcEncodedData encodedData = new LpcEncodedData(optimumLpcOrder, decodedReflectionData.decodedData, decodedResidueData.decodedData);
            LpcDecodedData decoded = (new SampleGenerator(encodedData)).process();
            samples[channel] = decoded.samples;
        }
        return samples;
    }
}