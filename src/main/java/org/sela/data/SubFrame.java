package org.sela.data;

import java.nio.ByteBuffer;

public final class SubFrame {
    // Audio Channel number - 1, 2, etc
    public byte channel;

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

    public SubFrame(byte channel, RiceEncodedData reflectionData, RiceEncodedData residueData) {
        this.channel = channel;

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

    public void write(ByteBuffer buffer) {
        buffer.put(channel);
        
        buffer.put(reflectionCoefficientRiceParam);
        buffer.putShort(reflectionCoefficientRequiredInts);
        buffer.put(optimumLpcOrder);
        for (int i : encodedReflectionCoefficients) {
            buffer.putInt(i);
        }

        buffer.put(residueRiceParam);
        buffer.putShort(residueRequiredInts);
        buffer.putShort(samplesPerChannel);
        for (int i : encodedResidues) {
            buffer.putInt(i);
        }
    }
}