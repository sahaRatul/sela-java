package org.sela;

import org.junit.Test;
import org.sela.data.*;
import org.sela.rice.*;

import static org.junit.Assert.*;

import java.util.Random;

public class RiceTest {
    @Test
    public void testRiceEncoderDecoder() {
        // Generate an array with random values
        Random rd = new Random();
        int[] input = new int[2048];
        for (int i = 0; i < input.length; i++) {
            input[i] = rd.nextInt(400) - 200;
        }

        // Encode
        RiceDecodedData inputData = new RiceDecodedData(input);
        RiceEncoder riceEncoder = new RiceEncoder(inputData);
        RiceEncodedData encodedData = riceEncoder.process();

        // Decode
        RiceDecoder riceDecoder = new RiceDecoder(encodedData);
        RiceDecodedData decodedData = riceDecoder.process();
        assertArrayEquals(inputData.decodedData, decodedData.decodedData);
    }
}