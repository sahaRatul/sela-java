package org.sela.rice;

import org.sela.data.*;

public final class RiceDecoder {
    private final int[] input;
    private int[] bitInput;
    private final int dataCount;
    private final int optimumRiceParam;
    private final long[] unsignedOutput;
    private final int[] output;

    public RiceDecoder(final RiceEncodedData encodedData) {
        this.input = encodedData.encodedData;
        this.dataCount = encodedData.dataCount;
        this.optimumRiceParam = encodedData.optimumRiceParam;
        this.unsignedOutput = new long[dataCount];
        this.output = new int[dataCount];
    }

    private void generateEncodedBits() {
        bitInput = new int[input.length * 32];
        for (int i = 0; i < input.length; i++) {
            for (int j = 0; j < 32; j++) {
                bitInput[i * 32 + j] = input[i] >> j & 0b1;
            }
        }
    }

    private void generateDecodedUnsignedInts() {
        int count = 0, temp = 0, i = 0;
        int bitReadCounter = 0;
        while (count < dataCount) {
            // Count 1s until a zero is encountered
            temp = 0;
            while (bitInput[bitReadCounter++] == 1) {
                temp++;
            }
            unsignedOutput[count] = temp << optimumRiceParam;
            // Read the last 'optimumRiceParam' number of bits and add them to output
            for (i = 1; i < (optimumRiceParam + 1); i++) {
                unsignedOutput[count] = unsignedOutput[count]
                        | ((long) bitInput[bitReadCounter++] << (optimumRiceParam - i));
            }
            count++;
        }
    }

    private void convertUnsignedToSigned() {
        for (int i = 0; i < output.length; i++) {
            output[i] = (int) (((unsignedOutput[i] & 0x01) == 0x01) ? -((unsignedOutput[i] + 1) >> 1)
                    : (unsignedOutput[i] >> 1));
        }
    }

    public RiceDecodedData process() {
        generateEncodedBits();
        generateDecodedUnsignedInts();
        convertUnsignedToSigned();
        return new RiceDecodedData(output);
    }
}
