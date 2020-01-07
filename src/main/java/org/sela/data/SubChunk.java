package org.sela.data;

import java.nio.ByteBuffer;

public class SubChunk {
    protected String subChunkId;
    protected int subChunkSize;
    protected byte[] subChunkData;

    public void write(ByteBuffer buffer) {
        char[] chars = new char[4];
        subChunkId.getChars(0, 4, chars, 0);
        for (char c : chars) {
            buffer.put((byte)c);
        }
        buffer.putInt(subChunkSize);
        buffer.put(subChunkData);
    }
}