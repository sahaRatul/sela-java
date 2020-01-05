package org.sela.data;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

public final class SelaFile {
    private final byte[] magicNumber = {0x53, 0x65, 0x4c, 0x61}; //SeLa in ASCII
    private int sampleRate;
    private short bitsPerSample;
    private byte channels;
    private int numFrames;
    private List<Frame> frames;
    DataOutputStream outPutStream;

    public SelaFile(int sampleRate, short bitsPerSample, byte channels, List<Frame> frames, FileOutputStream fos) {
        this.sampleRate = sampleRate;
        this.bitsPerSample = bitsPerSample;
        this.channels = channels;
        this.numFrames = frames.size();
        this.frames = frames;
        outPutStream = new DataOutputStream(new BufferedOutputStream(fos));
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

    public void writeToStream() throws IOException {
        int byteCount = 15;
        for (Frame frame : frames) {
            byteCount += frame.getByteCount();
        }

        ByteBuffer buffer = ByteBuffer.allocate(byteCount);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        write(buffer);

        outPutStream.write(buffer.array());
        outPutStream.close();
    }
}