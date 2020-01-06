package org.sela.data;

import org.sela.wav.WavFileException;

public class FormatSubChunk extends SubChunk {
    public short audioFormat;
    public short numChannels;
    public int sampleRate;
    public int byteRate;
    public short blockAlign;
    public short bitsPerSample;

    public void validate() throws WavFileException {
        if(!super.subChunkId.equals("fmt ")) {
            throw new WavFileException("Invalid subChunkId for fmt");
        }
        else if(super.subChunkSize != 16) {
            throw new WavFileException("Invalid subChunk size");
        }
        else if(audioFormat != 1) {
            throw new WavFileException("Invalid audioFormat");
        }
        else if(bitsPerSample != 16) {
            throw new WavFileException("Only 16bit PCM supported");
        }
    }
}