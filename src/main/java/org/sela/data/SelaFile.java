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

    public SelaFile(int sampleRate, short bitsPerSample, byte channels, List<Frame> frames, FileOutputStream fos) {
        this.sampleRate = sampleRate;
        this.bitsPerSample = bitsPerSample;
        this.channels = channels;
        this.numFrames = frames.size();
        this.frames = frames;
        this.outputStream = new DataOutputStream(new BufferedOutputStream(fos));
    }

    public SelaFile(FileInputStream fis) {
        this.inputStream = new DataInputStream(new BufferedInputStream(fis));
    }

    private void write(ByteBuffer buffer) {
        buffer.put(magicNumber);
        buffer.putInt(sampleRate);
        buffer.putShort(bitsPerSample);
        buffer.put(channels);
        buffer.putInt(numFrames);

        for (Frame frame : frames) {
            frame.write(buffer);
        }
    }

    private void read(ByteBuffer buffer) throws FileException {
        byte[] magicNumber = new byte[4];
        for (int i = 0; i < magicNumber.length; i++) {
            magicNumber[i] = buffer.get();
        }
        if(!(new String(magicNumber).equals("SeLa"))) {
            throw new FileException("Not a sela file");
        }
        sampleRate = buffer.getInt();
        bitsPerSample = buffer.getShort();
        channels = buffer.get();
        numFrames = buffer.getInt();
        frames = new ArrayList<>(numFrames);

        //Read frames
        for (int i = 0; i < numFrames; i++) {
            //Read SyncWord
            int sync = buffer.getInt();
            if(sync != 0xAA55FF00) {
                break;
            }
            Frame frame = new Frame(i);
            frame.subFrames = new ArrayList<>(2);

            //Read subframes
            for(int j = 0; j < channels; j++) {
                //Get channel
                byte subFrameChannel = buffer.get();

                //Get Reflection data
                byte reflectionCoefficientRiceParam = buffer.get();
                short reflectionCoefficientRequiredInts = buffer.getShort();
                byte optimumLpcOrder = buffer.get();
                int[] encodedReflectionCoefficients = new int[reflectionCoefficientRequiredInts];
                for(int k = 0; k < encodedReflectionCoefficients.length; k++) {
                    encodedReflectionCoefficients[i] = buffer.getInt();
                }

                //Get Residue data
                byte residueRiceParam = buffer.get();
                short residueRequiredInts = buffer.getShort();
                short samplesPerChannel = buffer.getShort();
                int[] encodedResidues = new int[residueRequiredInts];
                for (int k = 0; k < encodedResidues.length; k++) {
                    encodedResidues[i] = buffer.getInt();
                }

                //Generate objects
                RiceEncodedData reflectionData = new RiceEncodedData(reflectionCoefficientRiceParam, optimumLpcOrder, encodedReflectionCoefficients);
                RiceEncodedData residueData = new RiceEncodedData(residueRiceParam, samplesPerChannel, encodedResidues);
                SubFrame subFrame = new SubFrame(subFrameChannel, reflectionData, residueData);
                frame.subFrames.add(subFrame);
            }
        }
    }

    public void readFromStream() throws IOException, FileException {
        if (inputStream == null) {
            throw new FileException("inputStream is undefined");
        }

        byte[] inputBytes = new byte[inputStream.available()];
        inputStream.read(inputBytes);

        ByteBuffer buffer = ByteBuffer.wrap(inputBytes);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        read(buffer);
    }

    public void writeToStream() throws IOException, FileException {
        if (outputStream == null) {
            throw new FileException("outputStream is undefined");
        }
        int byteCount = 15;
        for (Frame frame : frames) {
            byteCount += frame.getByteCount();
        }

        ByteBuffer buffer = ByteBuffer.allocate(byteCount);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        write(buffer);

        outputStream.write(buffer.array());
        outputStream.close();
    }
}