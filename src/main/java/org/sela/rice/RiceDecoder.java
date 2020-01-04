package org.sela.rice;

public final class RiceDecoder {
    private int[] input;
    private int[] bitInput;
    private int sampleCount;
    private int optimumParam;
    private long[] unsignedOutput;
    private int[] output;

    public RiceDecoder(int[] input, int sampleCount, int optimumParam) {
        this.input = input;
        this.sampleCount = sampleCount;
        this.optimumParam = optimumParam;
        this.unsignedOutput = new long[sampleCount];
        this.output = new int[sampleCount];
    }

    private void convertUnsignedToSigned() {
        for (int i = 0; i < output.length; i++) {
            output[i] = (int)(((unsignedOutput[i] & 0x01) == 0x01) ? -((unsignedOutput[i] + 1) >> 1) : (unsignedOutput[i] >> 1));
        }
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
        while (count < sampleCount) {
            // Count 1s until a zero is encountered
            temp = 0;
            while (bitInput[bitReadCounter++] == 1) {
                temp++;
            }
            unsignedOutput[count] = temp << optimumParam;
            // Read the last 'optimumParam' number of bits and add them to output
            for (i = 1; i < (optimumParam + 1); i++) {
                unsignedOutput[count] = unsignedOutput[count] | ((long)bitInput[bitReadCounter++] << (optimumParam - i));
            }
            count++;
        }
    }

    public int[] decode() {
        generateEncodedBits();
        generateDecodedUnsignedInts();
        convertUnsignedToSigned();
        return output;
    }
}
