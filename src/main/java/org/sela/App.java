package org.sela;

import java.io.File;
import java.io.IOException;

import org.sela.data.SelaFile;
import org.sela.exception.FileException;

public final class App {
    public static void main(String[] args) {
        System.out.println("SimplE Lossless Audio. Released under MIT license");
        if (args.length < 2) {
            printUsage();
            System.out.println("Done");
        } else {
            try {
                parseCommandLineArgs(args);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void parseCommandLineArgs(String[] args) throws IOException, FileException {
        if (args[0].equals("-e") && args.length == 3) {
            File inputFile = new File(args[1]);
            File outputFile = new File(args[2]);
            System.out.println("Encoding: " + args[1]);
            encodeFile(inputFile, outputFile);
        } else if (args[0].equals("-d") && args.length == 3) {
            File inputFile = new File(args[1]);
            File outputFile = new File(args[2]);
            System.out.println("Decoding: " + args[1]);
            decodeFile(inputFile, outputFile);
        } else if (args[0].equals("-p") && args.length == 2) {
            File inputFile = new File(args[1]);
            System.out.println("Playing: " + args[1]);
            playFile(inputFile);
        } else {
            System.out.println("Invalid arguments..");
            printUsage();
        }
    }

    private static void encodeFile(File inputFile, File outputFile) throws IOException, FileException {
        Encoder selaEncoder = new Encoder(inputFile, outputFile);
        SelaFile selaFile = selaEncoder.process();
        selaFile.writeToStream();
    }

    private static void decodeFile(File inputFile, File outputFile) throws IOException, FileException {
        Decoder selaDecoder = new Decoder(inputFile, outputFile);
        selaDecoder.process();
    }

    private static void playFile(File inputFile) throws IOException, FileException {
        Player selaPlayer = new Player(inputFile);
        selaPlayer.play();
    }

    private static void printUsage() {
        System.out.println("");
        System.out.println("Usage:");
        System.out.println("");
        System.out.println("Encoding a file:");
        System.out.println("java -jar sela.jar -e path/to/input.wav path/to/output.sela");
        System.out.println("");
        System.out.println("Decoding a file:");
        System.out.println("java -jar sela.jar -d path/to/input.sela path/to/output.wav");
        System.out.println("");
        System.out.println("Playing a sela file:");
        System.out.println("java -jar sela.jar -p path/to/input.sela");
        System.out.println("");
    }
}
