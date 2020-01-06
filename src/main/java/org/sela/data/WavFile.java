package org.sela.data;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import org.sela.wav.WavFileException;

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
        chunk.subChunks = new ArrayList<>(2);

        chunk.validate();
    }

    private void readFormatSubChunk() throws WavFileException {
        FormatSubChunk formatSubChunk = new FormatSubChunk();
        byte[] subChunkId = new byte[4];

        for (int i = 0; i < subChunkId.length; i++) {
            subChunkId[i] = buffer.get();
        }

        formatSubChunk.subChunkId = new String(subChunkId);
        formatSubChunk.subChunkSize = buffer.getInt();
        formatSubChunk.audioFormat = buffer.getShort();
        formatSubChunk.numChannels = buffer.getShort();
        formatSubChunk.sampleRate = buffer.getInt();
        formatSubChunk.byteRate = buffer.getInt();
        formatSubChunk.blockAlign = buffer.getShort();
        formatSubChunk.bitsPerSample = buffer.getShort();

        formatSubChunk.validate();

        chunk.subChunks.add(formatSubChunk);
    }

    private void readDataChunk() throws WavFileException {
        FormatSubChunk formatSubChunk = (FormatSubChunk) chunk.subChunks.stream()
                .filter(x -> x.subChunkId.equals("fmt ")).findFirst().get();
        DataSubChunk dataSubChunk = new DataSubChunk();
        byte[] subChunkId = new byte[4];

        for (int i = 0; i < subChunkId.length; i++) {
            subChunkId[i] = buffer.get();
        }
        dataSubChunk.subChunkId = new String(subChunkId);
        dataSubChunk.subChunkSize = buffer.getInt();

        int bytesPerSample = formatSubChunk.bitsPerSample / 8;
        int sampleCount = dataSubChunk.subChunkSize / bytesPerSample;
        dataSubChunk.samples = new int[sampleCount];
        for(int i = 0; i < sampleCount; i++) {
            dataSubChunk.samples[i] = buffer.getShort();
        }

        dataSubChunk.validate();

        chunk.subChunks.add(dataSubChunk);
    }

    private void demuxSamples() {
        FormatSubChunk formatSubChunk = (FormatSubChunk) chunk.subChunks.get(0);
        DataSubChunk dataSubChunk = (DataSubChunk) chunk.subChunks.get(1);
        
        int[][] demuxedSamples = new int[formatSubChunk.numChannels][dataSubChunk.samples.length / formatSubChunk.numChannels];
        for(int i = 0; i < demuxedSamples.length; i++) {
            for (int j = 0; j < demuxedSamples[i].length; j++) {
                demuxedSamples[i][j] = dataSubChunk.samples[demuxedSamples.length * j + i];
            }
        }
        
        this.demuxedSamples = demuxedSamples;
    }

    private void read() throws IOException, WavFileException {
        allocateBuffer();
        readChunk();
        readFormatSubChunk();
        readDataChunk();
        demuxSamples();
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
        int readLimit = (demuxedSamples[0].length - readOffset) > samplesPerChannel ? samplesPerChannel : (demuxedSamples[0].length - readOffset);
        for(int i = 0; i < demuxedSamples.length; i++) {
            for(int j = 0; j < readLimit; j++) {
                output[i][j] = demuxedSamples[i][readOffset + j];
            }
        }
        readOffset += samplesPerChannel;
    }
}
