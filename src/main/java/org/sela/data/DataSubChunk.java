package org.sela.data;

import org.sela.exception.*;

public class DataSubChunk extends SubChunk {
    public int[] samples;

    public void validate() throws FileException {
        if(!super.subChunkId.equals("data")) {
            throw new FileException("Invalid subChunkId for data");
        }
        if(super.subChunkSize != (samples.length * 2)) {
            throw new FileException("Invalid subChunkSize for data");
        }
    }
}