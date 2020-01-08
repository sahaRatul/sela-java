package org.sela.frame;

import org.sela.data.*;
import org.sela.lpc.SampleGenerator;
import org.sela.rice.RiceDecoder;

public final class FrameDecoder {
    private final Frame frame;

    public FrameDecoder(final Frame frame) {
        this.frame = frame;
    }

    public WavFrame process() {
        final int[][] samples = new int[frame.subFrames.size()][];

        // Foreach subFrame
        for (final SubFrame subFrame : frame.subFrames) {
            // Stage 1 - Extract data from subFrame
            final byte channel = subFrame.channel;
            final byte optimumLpcOrder = subFrame.optimumLpcOrder;
            final RiceEncodedData reflectionData = new RiceEncodedData(subFrame.reflectionCoefficientRiceParam,
                    subFrame.optimumLpcOrder, subFrame.encodedReflectionCoefficients);
            final RiceEncodedData residueData = new RiceEncodedData(subFrame.residueRiceParam,
                    subFrame.samplesPerChannel, subFrame.encodedResidues);

            // Stage 2 - Decompress data
            final RiceDecodedData decodedReflectionData = (new RiceDecoder(reflectionData)).process();
            final RiceDecodedData decodedResidueData = (new RiceDecoder(residueData)).process();

            // Stage 3 - Generate Samples
            final LpcEncodedData encodedData = new LpcEncodedData(optimumLpcOrder, decodedReflectionData.decodedData,
                    decodedResidueData.decodedData);
            final LpcDecodedData decoded = (new SampleGenerator(encodedData)).process();
            samples[channel] = decoded.samples;
        }
        return new WavFrame(frame.getIndex(), samples);
    }
}