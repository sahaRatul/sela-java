package org.sela;

import org.junit.Test;
import org.sela.rice.*;

import static org.junit.Assert.*;

import java.util.Random;

public class RiceTest {
    @Test
    public void testRiceEncoderDecoder() {
        Random rd = new Random();
        int[] input = new int[2048];
        for (int i = 0; i < input.length; i++) {
            input[i] = rd.nextInt(400) - 200;
        }

        // Encode
        RiceEncoder riceEnc = new RiceEncoder(input);
        int[] encoded = riceEnc.process();
        int optRiceParam = riceEnc.getOptimumParam();

        // Decode
        RiceDecoder riceDec = new RiceDecoder(encoded, input.length, optRiceParam);
        int[] decoded = riceDec.process();
        assertArrayEquals(input, decoded);
    }
}