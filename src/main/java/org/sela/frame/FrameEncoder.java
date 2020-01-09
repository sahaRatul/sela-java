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
            if (i == 1 && ((i + 1) == wavFrame.samples.length)) {
                // Stage 1 - Generate difference signal
                int[] differenceSignal = new int[wavFrame.samples[i].length];
                for (int j = 0; j < differenceSignal.length; j++) {
                    differenceSignal[j] = wavFrame.samples[i][j - 1] - wavFrame.samples[i][j];
                }

                // Stage 2 - Generate residues and reflection coefficients for difference as
                // well as actual signal
                final ResidueGenerator residueGeneratorDifference = new ResidueGenerator(
                        new LpcDecodedData(differenceSignal));
                final LpcEncodedData residuesDifference = residueGeneratorDifference.process();

                final ResidueGenerator residueGeneratorActual = new ResidueGenerator(
                        new LpcDecodedData(wavFrame.samples[i]));
                final LpcEncodedData residuesActual = residueGeneratorActual.process();

                // Stage 3 - Compress residues and reflection coefficients for difference and
                // actual signal
                final RiceEncoder reflectionRiceEncoderDifference = new RiceEncoder(
                        new RiceDecodedData(residuesDifference.quantizedReflectionCoefficients));
                final RiceEncoder residueRiceEncoderDifference = new RiceEncoder(
                        new RiceDecodedData(residuesDifference.residues));
                final RiceEncodedData reflectionDataDifference = reflectionRiceEncoderDifference.process();
                final RiceEncodedData residueDataDifference = residueRiceEncoderDifference.process();

                final RiceEncoder reflectionRiceEncoderActual = new RiceEncoder(
                        new RiceDecodedData(residuesActual.quantizedReflectionCoefficients));
                final RiceEncoder residueRiceEncoderActual = new RiceEncoder(
                        new RiceDecodedData(residuesActual.residues));
                final RiceEncodedData reflectionDataActual = reflectionRiceEncoderActual.process();
                final RiceEncodedData residueDataActual = residueRiceEncoderActual.process();

                // Stage 4 - Compare sizes of both types and generate subFrame
                int differenceDataSize = reflectionDataDifference.encodedData.length
                        + residueDataDifference.encodedData.length;
                int actualDataSize = reflectionDataActual.encodedData.length + residueDataActual.encodedData.length;
                if (differenceDataSize < actualDataSize) {
                    final SubFrame subFrame = new SubFrame(i, (byte) 1, (byte) (i - 1), reflectionDataDifference,
                            residueDataDifference);
                    subFrames.add(subFrame);
                } else {
                    final SubFrame subFrame = new SubFrame(i, (byte) 0, i, reflectionDataActual, residueDataActual);
                    subFrames.add(subFrame);
                }
            } else {
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
                final SubFrame subFrame = new SubFrame(i, (byte) 0, i, reflectionData, residueData);
                subFrames.add(subFrame);
            }
        }

        return new Frame(subFrames, wavFrame.getIndex());
    }
}
