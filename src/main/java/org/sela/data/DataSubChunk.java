package org.sela.data;

import org.sela.exception.*;

public class DataSubChunk extends SubChunk {
    public int[] samples;

    public void validate() throws WavFileException {
        if(!super.subChunkId.equals("data")) {
            throw new WavFileException("Invalid subChunkId for data");
        }
        if(super.subChunkSize != (samples.length * 2)) {
            throw new WavFileException("Invalid subChunkSize for data");
        }
    }
}