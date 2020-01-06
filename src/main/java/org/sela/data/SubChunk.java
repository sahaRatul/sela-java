package org.sela.data;

import org.sela.exception.*;

public abstract class SubChunk {
    protected String subChunkId;
    protected int subChunkSize;

    public abstract void validate() throws WavFileException;
}