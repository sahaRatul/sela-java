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

        // Foreach independent subFrame
        for (final SubFrame subFrame : frame.subFrames) {
            if (subFrame.subFrameType == 0) {
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
                final LpcEncodedData encodedData = new LpcEncodedData(optimumLpcOrder,
                        decodedReflectionData.decodedData, decodedResidueData.decodedData);
                final LpcDecodedData decoded = (new SampleGenerator(encodedData)).process();
                samples[channel] = decoded.samples;
            }
        }

        // Foreach dependent subFrame
        for (final SubFrame subFrame : frame.subFrames) {
            if (subFrame.subFrameType == 1) {
                // Stage 1 - Extract data from subFrame
                final byte channel = subFrame.channel;
                final byte parentChannelNumber = subFrame.parentChannelNumber;
                final byte optimumLpcOrder = subFrame.optimumLpcOrder;
                final RiceEncodedData reflectionData = new RiceEncodedData(subFrame.reflectionCoefficientRiceParam,
                        subFrame.optimumLpcOrder, subFrame.encodedReflectionCoefficients);
                final RiceEncodedData residueData = new RiceEncodedData(subFrame.residueRiceParam,
                        subFrame.samplesPerChannel, subFrame.encodedResidues);

                // Stage 2 - Decompress data
                final RiceDecodedData decodedReflectionData = (new RiceDecoder(reflectionData)).process();
                final RiceDecodedData decodedResidueData = (new RiceDecoder(residueData)).process();

                // Stage 3 - Generate Difference signal
                final LpcEncodedData encodedData = new LpcEncodedData(optimumLpcOrder,
                        decodedReflectionData.decodedData, decodedResidueData.decodedData);
                final LpcDecodedData difference = (new SampleGenerator(encodedData)).process();

                int[] decoded = new int[difference.samples.length];
                
                //Stage 4 Generate samples
                for (int i = 0; i < decoded.length; i++) {
                    decoded[i] = samples[parentChannelNumber][i] - difference.samples[i];
                }
                samples[channel] = decoded;
            }
        }
        return new WavFrame(frame.getIndex(), samples);
    }
}