package org.sela;

import java.io.File;

import org.sela.data.SelaFile;

public final class App {
    public static void main(String[] args) {
        try {
            File inputFile;
            File outputFile;
            if (args.length > 1) {
                inputFile = new File(args[0]);
                outputFile = new File(args[1]);
            } else {
                inputFile = new File("/Users/ratul.s/Source/sela/sat2.wav");
                outputFile = new File("/Users/ratul.s/encoded.sela");
            }
            Encoder selaEncoder = new Encoder(inputFile, outputFile);
            SelaFile selaFile = selaEncoder.process();
            System.out.println("Encoded, writing to output file now");
            selaFile.writeToStream();

            // Decoder selaDecoder = new Decoder();
            // List<int[][]> samples = selaDecoder.process(frames);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
