package org.sela;

import org.junit.Test;
import org.sela.data.*;
import org.sela.frame.*;

import static org.junit.Assert.*;

public class FrameTest {
    @Test
    public void testFrame() {
        int[][] samples = new int[2][2048];
        
        // Generate a sine wave for channel 1
        for(int i = 0; i < samples[0].length; i++) {
            samples[0][i] = (int)(32767 * Math.sin(Math.toRadians(i)));
        }

        //Generate a cosine wave for channel 2
        for(int i = 0; i < samples[1].length; i++) {
            samples[1][i] = (int)(32767 * Math.cos(Math.toRadians(i)));
        }

        FrameEncoder encoder = new FrameEncoder(samples);
        Frame frame = encoder.process();

        FrameDecoder decoder = new FrameDecoder(frame);
        int[][] output = decoder.process();

        assertEquals(samples.length, output.length);
        for (int i = 0; i < samples.length; i++) {
            assertArrayEquals(samples[i], output[i]);
        }
    }    
}