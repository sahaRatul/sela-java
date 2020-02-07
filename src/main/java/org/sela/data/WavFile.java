//Note: This entire file needs to rewritten

package org.sela.data;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import org.sela.exception.*;

public class WavFile {
    private Chunk chunk;
    private File inputFile;
    private ByteBuffer buffer;
    private int readOffset;
    private int[][] demuxedSamples; // Used for reading
    private List<WavFrame> frames; // Used for writing
    private short bitsPerSample;
    private final String fmtNotFound = "fmt subchunk not found in wav";
    private final String dataNotFound = "data subchunk not found in wav";

    private DataOutputStream outputStream;

    public WavFile(final File inputFile) throws IOException, FileException {
        this.inputFile = inputFile;
        this.readOffset = 0;
        this.read();
    }

    public WavFile(final int sampleRate, final short bitsPerSample, final byte channels, final List<WavFrame> frames,
            final FileOutputStream fos) {
        this.outputStream = new DataOutputStream(new BufferedOutputStream(fos));
        this.frames = frames;
        this.bitsPerSample = bitsPerSample;
        createChunk();
        createFormatSubChunk(sampleRate, channels, bitsPerSample);
        createDataChunk();
    }

    private void allocateBuffer() throws IOException {
        final DataInputStream inputStream = new DataInputStream(
                new BufferedInputStream(new FileInputStream(inputFile)));

        final byte[] bytes = new byte[(int) inputFile.length()];
        inputStream.readFully(bytes);

        buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        inputStream.close();
    }

    private void readChunk() throws FileException {
        chunk = new Chunk();
        final byte[] chunkId = new byte[4];
        final byte[] format = new byte[4];
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

    private void createChunk() {
        chunk = new Chunk();
        chunk.chunkId = "RIFF";
        chunk.chunkSize = 36;
        chunk.format = "WAVE";
        chunk.subChunks = new ArrayList<>(2);
    }

    private void readSubChunks() throws FileException {
        while (buffer.hasRemaining()) {
            final SubChunk subChunk = new SubChunk();
            final byte[] subChunkId = new byte[4];
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

    private void generateFormatSubChunk() throws FileException {
        final SubChunk subChunk = chunk.subChunks.stream().filter(x -> x.subChunkId.equals("fmt ")).findFirst()
                .orElse(null);
        if (subChunk == null) {
            throw new FileException(fmtNotFound);
        }
        final int subChunkIndex = chunk.subChunks.indexOf(subChunk);

        final ByteBuffer buffer = ByteBuffer.wrap(subChunk.subChunkData);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        final FormatSubChunk formatSubChunk = new FormatSubChunk();
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

    private void createFormatSubChunk(final int sampleRate, final byte channels, final short bitsPerSample) {
        final SubChunk subChunk = new SubChunk();
        subChunk.subChunkId = "fmt ";
        subChunk.subChunkSize = 16;
        final ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putShort((short) 1); // AudioFormat
        buffer.putShort((short) channels); // Channels
        buffer.putInt(sampleRate); // SampleRate
        buffer.putInt((sampleRate * channels * bitsPerSample) / 8); // ByteRate
        buffer.putShort((short) ((channels * bitsPerSample) / 8)); // BlockAlign
        buffer.putShort(bitsPerSample); // BitsPerSample
        subChunk.subChunkData = buffer.array();
        chunk.subChunks.add(subChunk);
    }

    private void generateDataChunk() throws FileException {
        // Get Data subChunk
        final SubChunk subChunk = chunk.subChunks.stream().filter(x -> x.subChunkId.equals("data")).findFirst()
                .orElse(null);
        if (subChunk == null) {
            throw new FileException(dataNotFound);
        }
        final int subChunkIndex = chunk.subChunks.indexOf(subChunk);

        // Get fmt subChunk
        final FormatSubChunk formatSubChunk = (FormatSubChunk) chunk.subChunks.stream()
                .filter(x -> x.subChunkId.equals("fmt ")).findFirst().orElse(null);
        if (formatSubChunk == null) {
            throw new FileException(fmtNotFound);
        }

        final DataSubChunk dataSubChunk = new DataSubChunk((byte) formatSubChunk.bitsPerSample);

        final ByteBuffer buffer = ByteBuffer.wrap(subChunk.subChunkData);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        dataSubChunk.subChunkId = subChunk.subChunkId;
        dataSubChunk.subChunkSize = subChunk.subChunkSize;
        dataSubChunk.subChunkData = subChunk.subChunkData;

        final int bytesPerSample = formatSubChunk.bitsPerSample / 8;
        final int sampleCount = dataSubChunk.subChunkSize / bytesPerSample;
        dataSubChunk.samples = new int[sampleCount];

        if (formatSubChunk.bitsPerSample == 16) {
            for (int i = 0; i < sampleCount; i++) {
                dataSubChunk.samples[i] = buffer.getShort();
            }
        } else {
            for (int i = 0; i < sampleCount; i++) {
                dataSubChunk.samples[i] = (buffer.get()) << 24 | (buffer.get() & 0xFF) << 16
                        | (buffer.get() & 0xFF) << 8;
            }
        }
        dataSubChunk.validate();
        chunk.subChunks.set(subChunkIndex, dataSubChunk);
    }

    private void createDataChunk() {
        final SubChunk subChunk = new SubChunk();
        subChunk.subChunkId = "data";
        subChunk.subChunkSize = 0;
        for (final WavFrame frame : frames) {
            subChunk.subChunkSize += frame.getSizeInBytes();
        }
        subChunk.subChunkData = new byte[0];
        chunk.subChunks.add(subChunk);
    }

    private void demuxSamples() throws FileException {
        final FormatSubChunk formatSubChunk = (FormatSubChunk) chunk.subChunks.stream()
                .filter(x -> x.subChunkId.equals("fmt ")).findFirst().orElse(null);
        final DataSubChunk dataSubChunk = (DataSubChunk) chunk.subChunks.stream()
                .filter(x -> x.subChunkId.equals("data")).findFirst().orElse(null);

        if (formatSubChunk == null) {
            throw new FileException(fmtNotFound);
        }
        if (dataSubChunk == null) {
            throw new FileException(dataNotFound);
        }

        final int[][] demuxedSamples = new int[formatSubChunk.numChannels][dataSubChunk.samples.length
                / formatSubChunk.numChannels];
        for (int i = 0; i < demuxedSamples.length; i++) {
            for (int j = 0; j < demuxedSamples[i].length; j++) {
                demuxedSamples[i][j] = dataSubChunk.samples[demuxedSamples.length * j + i];
            }
        }

        this.frames = new ArrayList<>(1);
        this.demuxedSamples = demuxedSamples;
        this.frames.add(new WavFrame(0, demuxedSamples, (byte) getBitsPerSample())); // Just for reference
    }

    public short getNumChannels() {
        final FormatSubChunk formatSubChunk = (FormatSubChunk) chunk.subChunks.get(0);
        return formatSubChunk.numChannels;
    }

    public int getSampleRate() {
        final FormatSubChunk formatSubChunk = (FormatSubChunk) chunk.subChunks.get(0);
        return formatSubChunk.sampleRate;
    }

    public short getBitsPerSample() {
        final FormatSubChunk formatSubChunk = (FormatSubChunk) chunk.subChunks.get(0);
        return formatSubChunk.bitsPerSample;
    }

    public int getSampleCount() {
        return demuxedSamples.length * demuxedSamples[0].length;
    }

    public List<WavFrame> getFrames() {
        return frames;
    }

    private void read() throws IOException, FileException {
        allocateBuffer();
        readChunk();
        readSubChunks();
        generateFormatSubChunk();
        generateDataChunk();
        demuxSamples();
        chunk.subChunks.trimToSize();
    }

    public void writeToStream() throws FileException, IOException {
        if (outputStream == null) {
            throw new FileException("outputStream is null");
        }
        int byteCount = 44;
        for (final WavFrame frame : frames) {
            byteCount += frame.getSizeInBytes();
        }

        final ByteBuffer buffer = ByteBuffer.allocate(byteCount);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        // Write chunk and subchunks
        chunk.chunkSize += chunk.subChunks.get(1).subChunkSize;
        chunk.write(buffer);

        // Write samples
        byte bytesPerSample = (byte) ((byte) bitsPerSample / 8);
        for (int i = 0; i < frames.size(); i++) {
            buffer.put(frames.get(i).getDemuxedSamplesInByteArray(bytesPerSample));
        }

        outputStream.write(buffer.array());
        outputStream.close();
    }

    public void readFrames(final int[][] output, final int samplesPerChannel) {
        final int readLimit = (demuxedSamples[0].length - readOffset) > samplesPerChannel ? samplesPerChannel
                : (demuxedSamples[0].length - readOffset);
        for (int i = 0; i < demuxedSamples.length; i++) {
            for (int j = 0; j < readLimit; j++) {
                output[i][j] = demuxedSamples[i][readOffset + j];
            }
        }
        readOffset += samplesPerChannel;
    }
}
