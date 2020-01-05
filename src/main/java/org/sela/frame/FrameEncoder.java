package org.sela.frame;

import java.util.ArrayList;

import org.sela.data.*;
import org.sela.lpc.*;
import org.sela.rice.*;

public final class FrameEncoder {
    private int[][] samples;
    private int index;

    public FrameEncoder(int[][] samples) {
        this.samples = samples;
        this.index = 0;
    }

    public FrameEncoder(int[][] samples, int index) {
        this.samples = samples;
        this.index = index; //Useful when parallel processing
    }

    public Frame process() {
        ArrayList<SubFrame> subFrames = new ArrayList<>(samples.length);

        // Foreach channel
        for (byte i = 0; i < samples.length; i++) {
            // Stage 1 - Generate residues and reflection coefficients
            ResidueGenerator residueGenerator = new ResidueGenerator(new LpcDecodedData(samples[i]));
            LpcEncodedData residues = residueGenerator.process();

            // Stage 2 - Compress residues and reflection coefficients
            RiceEncoder reflectionRiceEncoder = new RiceEncoder(
                    new RiceDecodedData(residues.quantizedReflectionCoefficients));
            RiceEncoder residueRiceEncoder = new RiceEncoder(new RiceDecodedData(residues.residues));
            RiceEncodedData reflectionData = reflectionRiceEncoder.process();
            RiceEncodedData residueData = residueRiceEncoder.process();

            // Stage 3 - Generate Subframes
            SubFrame subFrame = new SubFrame(i, reflectionData, residueData);
            subFrames.add(subFrame);
        }

        return new Frame(subFrames, index);
    }
}
