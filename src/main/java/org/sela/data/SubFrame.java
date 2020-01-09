package org.sela.data;

import java.nio.ByteBuffer;

public final class SubFrame {
    // Audio Channel number - 1, 2, etc
    public byte channel;
    // 0 - Subframe is independent, 1 - SubFrame uses difference coding and is
    // dependent on another channel
    public byte subFrameType;
    // Incase subFrame uses difference coding, the channel from which difference is
    // generated will be stored here
    public byte parentChannelNumber;

    // Reflection coefficient data
    public byte reflectionCoefficientRiceParam;
    public short reflectionCoefficientRequiredInts;
    public byte optimumLpcOrder;
    public int[] encodedReflectionCoefficients;

    // Residue data
    public byte residueRiceParam;
    public short residueRequiredInts;
    public short samplesPerChannel;
    public int[] encodedResidues;

    public SubFrame(final byte channel, final byte subFrameType, final byte parentChannelNumber,
            final RiceEncodedData reflectionData, final RiceEncodedData residueData) {
        this.channel = channel;
        this.subFrameType = subFrameType;
        this.parentChannelNumber = parentChannelNumber;
        
        this.reflectionCoefficientRiceParam = (byte) reflectionData.optimumRiceParam;
        this.reflectionCoefficientRequiredInts = (short) reflectionData.encodedData.length;
        this.optimumLpcOrder = (byte) reflectionData.dataCount;
        this.encodedReflectionCoefficients = reflectionData.encodedData;

        this.residueRiceParam = (byte) residueData.optimumRiceParam;
        this.residueRequiredInts = (short) residueData.encodedData.length;
        this.samplesPerChannel = (short) residueData.dataCount;
        this.encodedResidues = residueData.encodedData;
    }

    public int getByteCount() {
        return 10 + (4 * (encodedReflectionCoefficients.length + encodedResidues.length));
    }

    public void write(final ByteBuffer buffer) {
        buffer.put(channel);
        buffer.put(subFrameType);
        buffer.put(parentChannelNumber);

        buffer.put(reflectionCoefficientRiceParam);
        buffer.putShort(reflectionCoefficientRequiredInts);
        buffer.put(optimumLpcOrder);
        for (final int i : encodedReflectionCoefficients) {
            buffer.putInt(i);
        }

        buffer.put(residueRiceParam);
        buffer.putShort(residueRequiredInts);
        buffer.putShort(samplesPerChannel);
        for (final int i : encodedResidues) {
            buffer.putInt(i);
        }
    }
}