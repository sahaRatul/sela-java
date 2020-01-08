package org.sela.frame;

import java.util.ArrayList;

import org.sela.data.*;
import org.sela.lpc.*;
import org.sela.rice.*;

public final class FrameEncoder {
    private final WavFrame wavFrame;

    public FrameEncoder(final WavFrame wavFrame) {
        this.wavFrame = wavFrame;
    }

    public Frame process() {
        final ArrayList<SubFrame> subFrames = new ArrayList<>(wavFrame.samples.length);

        // Foreach channel
        for (byte i = 0; i < wavFrame.samples.length; i++) {
            // Stage 1 - Generate residues and reflection coefficients
            final ResidueGenerator residueGenerator = new ResidueGenerator(new LpcDecodedData(wavFrame.samples[i]));
            final LpcEncodedData residues = residueGenerator.process();

            // Stage 2 - Compress residues and reflection coefficients
            final RiceEncoder reflectionRiceEncoder = new RiceEncoder(
                    new RiceDecodedData(residues.quantizedReflectionCoefficients));
            final RiceEncoder residueRiceEncoder = new RiceEncoder(new RiceDecodedData(residues.residues));
            final RiceEncodedData reflectionData = reflectionRiceEncoder.process();
            final RiceEncodedData residueData = residueRiceEncoder.process();

            // Stage 3 - Generate Subframes
            final SubFrame subFrame = new SubFrame(i, reflectionData, residueData);
            subFrames.add(subFrame);
        }

        return new Frame(subFrames, wavFrame.getIndex());
    }
}
