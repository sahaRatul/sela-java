package org.sela.data;

import java.nio.ByteBuffer;

public class SubChunk {
    protected String subChunkId;
    protected int subChunkSize;
    protected byte[] subChunkData;

    public void write(final ByteBuffer buffer) {
        final char[] chars = new char[4];
        subChunkId.getChars(0, 4, chars, 0);
        for (final char c : chars) {
            buffer.put((byte)c);
        }
        buffer.putInt(subChunkSize);
        buffer.put(subChunkData);
    }
}