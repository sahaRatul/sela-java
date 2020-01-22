package org.sela.data;

import org.sela.exception.*;

public class DataSubChunk extends SubChunk {
    public int[] samples;
    private final byte bitsPerSample;

    public DataSubChunk(final byte bitsPerSample) {
        this.bitsPerSample = bitsPerSample;
    }

    public void validate() throws FileException {
        if(!super.subChunkId.equals("data")) {
            throw new FileException("Invalid subChunkId for data");
        }
        if(super.subChunkSize != (samples.length * (bitsPerSample / 8))) {
            throw new FileException("Invalid subChunkSize for data");
        }
    }
}