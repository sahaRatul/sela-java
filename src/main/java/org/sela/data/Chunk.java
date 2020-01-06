package org.sela.data;

import java.util.ArrayList;

import org.sela.exception.*;

class Chunk {
    public String chunkId;
    public int chunkSize;
    public String format;
    public ArrayList<SubChunk> subChunks;

    public void validate() throws FileException {
        if(!chunkId.equals("RIFF")) {
            throw new FileException("Invalid ChunkId");
        }
        else if(!format.equals("WAVE")) {
            throw new FileException("Invalid format");
        }
    }
}