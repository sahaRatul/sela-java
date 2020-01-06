package org.sela.data;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import org.sela.exception.*;

public class WavFile {
    public Chunk chunk;
    private File inputFile;
    private ByteBuffer buffer;
    private int readOffset;
    private int[][] demuxedSamples;

    public WavFile(File inputFile) throws IOException, WavFileException {
        this.inputFile = inputFile;
        this.readOffset = 0;
        this.read();
    }

    private void allocateBuffer() throws IOException {
        DataInputStream inputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(inputFile)));

        byte[] bytes = new byte[(int) inputFile.length()];
        inputStream.readFully(bytes);

        buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        inputStream.close();
    }

    private void readChunk() throws WavFileException {
        chunk = new Chunk();
        byte[] chunkId = new byte[4];
        byte[] format = new byte[4];
        buffer.get(chunkId, 0, 4);

        chunk.chunkId = new String(chunkId);
        chunk.chunkSize = buffer.getInt();

        for (int i = 0; i < format.length; i++) {
            format[i] = buffer.get();
        }
        chunk.format = new String(format);
        chunk.subChunks = new ArrayList<>(100);

        chunk.validate();
    }

    private void readSubChunks() throws WavFileException {
        while (buffer.hasRemaining()) {
            SubChunk subChunk = new SubChunk();
            byte[] subChunkId = new byte[4];
            for (int i = 0; i < subChunkId.length; i++) {
                subChunkId[i] = buffer.get();
            }
            subChunk.subChunkId = new String(subChunkId);
            subChunk.subChunkSize = buffer.getInt();
            subChunk.subChunkData = new byte[subChunk.subChunkSize];
            for (int i = 0; i < subChunk.subChunkData.length; i++) {
                subChunk.subChunkData[i] = buffer.get();
            }
            chunk.subChunks.add(subChunk);
        }
    }

    private void generateFormatSubChunk() throws WavFileException {
        SubChunk subChunk = chunk.subChunks.stream().filter(x -> x.subChunkId.equals("fmt ")).findFirst().get();
        if (subChunk == null) {
            throw new WavFileException("fmt subchunk not found in wav");
        }
        int subChunkIndex = chunk.subChunks.indexOf(subChunk);

        ByteBuffer buffer = ByteBuffer.wrap(subChunk.subChunkData);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        FormatSubChunk formatSubChunk = new FormatSubChunk();
        formatSubChunk.subChunkId = subChunk.subChunkId;
        formatSubChunk.subChunkSize = subChunk.subChunkSize;
        formatSubChunk.subChunkData = subChunk.subChunkData;
        formatSubChunk.audioFormat = buffer.getShort();
        formatSubChunk.numChannels = buffer.getShort();
        formatSubChunk.sampleRate = buffer.getInt();
        formatSubChunk.byteRate = buffer.getInt();
        formatSubChunk.blockAlign = buffer.getShort();
        formatSubChunk.bitsPerSample = buffer.getShort();

        formatSubChunk.validate();

        chunk.subChunks.set(subChunkIndex, formatSubChunk);
    }

    private void generateDataChunk() throws WavFileException {
        // Get Data subChunk
        SubChunk subChunk = chunk.subChunks.stream().filter(x -> x.subChunkId.equals("data")).findFirst().get();
        if (subChunk == null) {
            throw new WavFileException("data subchunk not found in wav");
        }
        int subChunkIndex = chunk.subChunks.indexOf(subChunk);

        // Get fmt subChunk
        FormatSubChunk formatSubChunk = (FormatSubChunk) chunk.subChunks.stream()
                .filter(x -> x.subChunkId.equals("fmt ")).findFirst().get();

        DataSubChunk dataSubChunk = new DataSubChunk();

        ByteBuffer buffer = ByteBuffer.wrap(subChunk.subChunkData);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        dataSubChunk.subChunkId = subChunk.subChunkId;
        dataSubChunk.subChunkSize = subChunk.subChunkSize;
        dataSubChunk.subChunkData = subChunk.subChunkData;

        int bytesPerSample = formatSubChunk.bitsPerSample / 8;
        int sampleCount = dataSubChunk.subChunkSize / bytesPerSample;
        dataSubChunk.samples = new int[sampleCount];
        for (int i = 0; i < sampleCount; i++) {
            dataSubChunk.samples[i] = buffer.getShort();
        }
        dataSubChunk.validate();
        chunk.subChunks.set(subChunkIndex, dataSubChunk);
    }

    private void demuxSamples() {
        FormatSubChunk formatSubChunk = (FormatSubChunk) chunk.subChunks.stream()
                .filter(x -> x.subChunkId.equals("fmt ")).findFirst().get();
        DataSubChunk dataSubChunk = (DataSubChunk) chunk.subChunks.stream().filter(x -> x.subChunkId.equals("data"))
                .findFirst().get();

        int[][] demuxedSamples = new int[formatSubChunk.numChannels][dataSubChunk.samples.length
                / formatSubChunk.numChannels];
        for (int i = 0; i < demuxedSamples.length; i++) {
            for (int j = 0; j < demuxedSamples[i].length; j++) {
                demuxedSamples[i][j] = dataSubChunk.samples[demuxedSamples.length * j + i];
            }
        }

        this.demuxedSamples = demuxedSamples;
    }

    private void read() throws IOException, WavFileException {
        allocateBuffer();
        readChunk();
        readSubChunks();
        generateFormatSubChunk();
        generateDataChunk();
        demuxSamples();
        chunk.subChunks.trimToSize();
    }

    public short getNumChannels() {
        FormatSubChunk formatSubChunk = (FormatSubChunk) chunk.subChunks.get(0);
        return formatSubChunk.numChannels;
    }

    public int getSampleRate() {
        FormatSubChunk formatSubChunk = (FormatSubChunk) chunk.subChunks.get(0);
        return formatSubChunk.sampleRate;
    }

    public short getBitsPerSample() {
        FormatSubChunk formatSubChunk = (FormatSubChunk) chunk.subChunks.get(0);
        return formatSubChunk.bitsPerSample;
    }

    public int getSampleCount() {
        return demuxedSamples.length * demuxedSamples[0].length;
    }

    public void readFrames(int[][] output, int samplesPerChannel) {
        int readLimit = (demuxedSamples[0].length - readOffset) > samplesPerChannel ? samplesPerChannel
                : (demuxedSamples[0].length - readOffset);
        for (int i = 0; i < demuxedSamples.length; i++) {
            for (int j = 0; j < readLimit; j++) {
                output[i][j] = demuxedSamples[i][readOffset + j];
            }
        }
        readOffset += samplesPerChannel;
    }
}
