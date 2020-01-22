package org.sela.data;

import org.sela.exception.*;

public class FormatSubChunk extends SubChunk {
    public short audioFormat;
    public short numChannels;
    public int sampleRate;
    public int byteRate;
    public short blockAlign;
    public short bitsPerSample;

    public void validate() throws FileException {
        if(!super.subChunkId.equals("fmt ")) {
            throw new FileException("Invalid subChunkId for fmt");
        }
        else if(!(bitsPerSample == 16 || bitsPerSample == 24)) {
            throw new FileException("Only 16/24bit LE PCM supported");
        }
    }
}