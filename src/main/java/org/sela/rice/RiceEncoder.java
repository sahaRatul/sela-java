package org.sela.rice;

import java.util.ArrayList;
import java.util.Collections;

public class RiceEncoder {
    private int[] input;
    private long[] unsignedInput;
    private int[] output;
    private int[] bitOutput;
    private ArrayList<Integer> bitSizes;
    private int optimumParam;
    private int requiredBits;
    private final int maxRiceParam = 20;

    public RiceEncoder(int[] input) {
        this.input = input;
        this.unsignedInput = new long[input.length];
        this.bitSizes = new ArrayList<Integer>(maxRiceParam);
        optimumParam = 0;
        requiredBits = 0;
    }

    private void convertSignedToUnsigned() {
        for (int i = 0; i < input.length; i++) {
            unsignedInput[i] = input[i] < 0 ? (-(input[i] << 1)) - 1 : (input[i] << 1);
        }
    }

    private void calculateOptimumRiceParam() {
        for (int i = 0; i < maxRiceParam; i++) {
            int temp = 0;
            for (int j = 0; j < unsignedInput.length; j++) {
                temp += unsignedInput[j] >> i;
                temp += 1;
                temp += i;
            }
            bitSizes.add(temp);
            requiredBits = Collections.min(bitSizes);
            optimumParam = bitSizes.indexOf(requiredBits);
        }
    }

    private void generateEncodedBits() {
        bitOutput = new int[(int)(Math.ceil((float)requiredBits/32) * 32)];
        int temp = 0, bits = 0;
        
        for (int i = 0; i < unsignedInput.length; i++) {
            temp = (int)(unsignedInput[i] >> optimumParam);
            // Write out 'temp' number of ones
            for (int j = 0; j < temp; j++) {
                bitOutput[bits++] = 0b1;
            }

            // Write out a zero
            bits++;

            // Write out last param bits of input
            for (int j = (optimumParam - 1); j >= 0; j--) {
                bitOutput[bits++] = (int)((unsignedInput[i] >> j) & 0b1);
            }
        }
    }

    private void writeInts() {
        output = new int[((int) Math.ceil(((float) requiredBits) / 32))];
        for (int i = 0; i < output.length; i++) {
            for(int j = 0; j < 32; j++) {
                output[i] |= ((long)bitOutput[i * 32 + j]) << j;;
            }
        }
    }

    public int getOptimumParam() {
        return optimumParam;
    }

    public int[] encode() {
        convertSignedToUnsigned();
        calculateOptimumRiceParam();
        generateEncodedBits();
        writeInts();
        return output;
    }
}
