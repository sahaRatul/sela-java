package org.sela.data;

import java.nio.ByteBuffer;
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

    public void write(ByteBuffer buffer) {
        buffer.put((byte)'R');
        buffer.put((byte)'I');
        buffer.put((byte)'F');
        buffer.put((byte)'F');
        buffer.putInt(chunkSize);
        buffer.put((byte)'W');
        buffer.put((byte)'A');
        buffer.put((byte)'V');
        buffer.put((byte)'E');
        for (SubChunk subChunk : subChunks) {
            subChunk.write(buffer);
        }
    }
}