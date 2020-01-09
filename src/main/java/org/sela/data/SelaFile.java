package org.sela.data;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import org.sela.exception.FileException;

public final class SelaFile {
    private final byte[] magicNumber = { 0x53, 0x65, 0x4c, 0x61 }; // SeLa in ASCII
    private int sampleRate;
    private short bitsPerSample;
    private byte channels;
    private int numFrames;
    private List<Frame> frames;
    DataOutputStream outputStream;
    DataInputStream inputStream;

    public SelaFile(final int sampleRate, final short bitsPerSample, final byte channels, final List<Frame> frames,
            final FileOutputStream fos) {
        this.sampleRate = sampleRate;
        this.bitsPerSample = bitsPerSample;
        this.channels = channels;
        this.numFrames = frames.size();
        this.frames = frames;
        this.outputStream = new DataOutputStream(new BufferedOutputStream(fos));
    }

    public SelaFile(final FileInputStream fis) {
        this.inputStream = new DataInputStream(new BufferedInputStream(fis));
    }

    private void write(final ByteBuffer buffer) {
        buffer.put(magicNumber);
        buffer.putInt(sampleRate);
        buffer.putShort(bitsPerSample);
        buffer.put(channels);
        buffer.putInt(numFrames);

        for (final Frame frame : frames) {
            frame.write(buffer);
        }
    }

    private void read(final ByteBuffer buffer) throws FileException {
        final byte[] magicNumber = new byte[4];
        for (int i = 0; i < magicNumber.length; i++) {
            magicNumber[i] = buffer.get();
        }
        if (!(new String(magicNumber).equals("SeLa"))) {
            throw new FileException("Not a sela file");
        }
        sampleRate = buffer.getInt();
        bitsPerSample = buffer.getShort();
        channels = buffer.get();
        numFrames = buffer.getInt();
        frames = new ArrayList<>(numFrames);

        // Read frames
        for (int i = 0; i < numFrames; i++) {
            // Read SyncWord
            final int sync = buffer.getInt();
            if (sync != 0xAA55FF00) {
                break;
            }
            final Frame frame = new Frame(i);
            frame.subFrames = new ArrayList<>(2);

            // Read subframes
            for (int j = 0; j < channels; j++) {
                // Get channel
                final byte subFrameChannel = buffer.get();
                final byte subFrameType = buffer.get();
                final byte parentChannelNumber = buffer.get();

                // Get Reflection data
                final byte reflectionCoefficientRiceParam = buffer.get();
                final short reflectionCoefficientRequiredInts = buffer.getShort();
                final byte optimumLpcOrder = buffer.get();
                final int[] encodedReflectionCoefficients = new int[reflectionCoefficientRequiredInts];

                for (int k = 0; k < encodedReflectionCoefficients.length; k++) {
                    encodedReflectionCoefficients[k] = buffer.getInt();
                }

                // Get Residue data
                final byte residueRiceParam = buffer.get();
                final short residueRequiredInts = buffer.getShort();
                final short samplesPerChannel = buffer.getShort();
                final int[] encodedResidues = new int[residueRequiredInts];
                for (int k = 0; k < encodedResidues.length; k++) {
                    encodedResidues[k] = buffer.getInt();
                }

                // Generate subframes
                final RiceEncodedData reflectionData = new RiceEncodedData(reflectionCoefficientRiceParam,
                        optimumLpcOrder, encodedReflectionCoefficients);
                final RiceEncodedData residueData = new RiceEncodedData(residueRiceParam, samplesPerChannel,
                        encodedResidues);
                final SubFrame subFrame = new SubFrame(subFrameChannel, subFrameType, parentChannelNumber,
                        reflectionData, residueData);
                frame.subFrames.add(subFrame);
            }
            frames.add(frame);
        }
    }

    public void readFromStream() throws IOException, FileException {
        if (inputStream == null) {
            throw new FileException("inputStream is null");
        }

        final byte[] inputBytes = new byte[inputStream.available()];
        inputStream.read(inputBytes);

        final ByteBuffer buffer = ByteBuffer.wrap(inputBytes);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        read(buffer);
        inputStream.close();
    }

    public void writeToStream() throws IOException, FileException {
        if (outputStream == null) {
            throw new FileException("outputStream is null");
        }
        int byteCount = 15;
        for (final Frame frame : frames) {
            byteCount += frame.getByteCount();
        }

        final ByteBuffer buffer = ByteBuffer.allocate(byteCount);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        write(buffer);

        outputStream.write(buffer.array());
        outputStream.close();
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public short getBitsPerSample() {
        return bitsPerSample;
    }

    public byte getChannels() {
        return channels;
    }

    public List<Frame> getFrames() {
        return frames;
    }
}