package org.sela.frame;

import java.util.ArrayList;

import org.sela.data.*;
import org.sela.lpc.*;
import org.sela.rice.*;

public final class FrameEncoder {
    private WavFrame wavFrame;

    public FrameEncoder(WavFrame wavFrame) {
        this.wavFrame = wavFrame;
    }

    public Frame process() {
        ArrayList<SubFrame> subFrames = new ArrayList<>(wavFrame.samples.length);

        // Foreach channel
        for (byte i = 0; i < wavFrame.samples.length; i++) {
            // Stage 1 - Generate residues and reflection coefficients
            ResidueGenerator residueGenerator = new ResidueGenerator(new LpcDecodedData(wavFrame.samples[i]));
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

        return new Frame(subFrames, wavFrame.getIndex());
    }
}
