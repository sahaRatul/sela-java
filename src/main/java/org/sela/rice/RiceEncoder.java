package org.sela.rice;

import java.util.ArrayList;
import java.util.Collections;

import org.sela.data.*;

public final class RiceEncoder {
    private final int[] input;
    private final long[] unsignedInput;
    private int[] output;
    private int[] bitOutput;
    private final ArrayList<Long> bitSizes;
    private int optimumRiceParam;
    private long requiredBits;
    private final int maxRiceParam = 20;

    public RiceEncoder(final RiceDecodedData data) {
        this.input = data.decodedData;
        this.unsignedInput = new long[input.length];
        this.bitSizes = new ArrayList<Long>(maxRiceParam);
        optimumRiceParam = 0;
        requiredBits = 0;
    }

    private void convertSignedToUnsigned() {
        for (int i = 0; i < input.length; i++) {
            unsignedInput[i] = input[i] < 0 ? (-(input[i] << 1)) - 1 : (input[i] << 1);
        }
    }

    private void calculateOptimumRiceParam() {
        for (int i = 0; i < maxRiceParam; i++) {
            long temp = 0;
            for (int j = 0; j < unsignedInput.length; j++) {
                temp += (unsignedInput[j] >>> i);
                temp += 1;
                temp += i;
            }
            bitSizes.add(temp < 0 ? Long.MAX_VALUE : temp);
        }
        requiredBits = Collections.min(bitSizes);
        optimumRiceParam = bitSizes.indexOf(requiredBits);
    }

    private void generateEncodedBits() {
        bitOutput = new int[(int) (Math.ceil((double) requiredBits / 32) * 32)];
        long temp = 0;
        int bits = 0;

        for (int i = 0; i < unsignedInput.length; i++) {
            temp = (unsignedInput[i] >>> optimumRiceParam);
            // Write out 'temp' number of ones
            for (int j = 0; j < temp; j++) {
                bitOutput[bits++] = 0b1;
            }

            // Write out a zero
            bits++;

            // Write out last param bits of input
            for (int j = (optimumRiceParam - 1); j >= 0; j--) {
                bitOutput[bits++] = (int) ((unsignedInput[i] >> j) & 0b1);
            }
        }
    }

    private void writeInts() {
        output = new int[((int) Math.ceil(((float) requiredBits) / 32))];
        for (int i = 0; i < output.length; i++) {
            for (int j = 0; j < 32; j++) {
                output[i] |= ((long) bitOutput[i * 32 + j]) << j;
                ;
            }
        }
    }

    public RiceEncodedData process() {
        convertSignedToUnsigned();
        calculateOptimumRiceParam();
        generateEncodedBits();
        writeInts();
        return new RiceEncodedData(optimumRiceParam, input.length, output);
    }
}
