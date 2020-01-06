package org.sela.data;

import java.util.ArrayList;

import org.sela.wav.WavFileException;

class Chunk {
    public String chunkId;
    public int chunkSize;
    public String format;
    public ArrayList<SubChunk> subChunks;

    public void validate() throws WavFileException {
        if(!chunkId.equals("RIFF")) {
            throw new WavFileException("Invalid ChunkId");
        }
        else if(!format.equals("WAVE")) {
            throw new WavFileException("Invalid format");
        }
    }
}