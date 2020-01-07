package org.sela;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.LineUnavailableException;

import org.sela.data.SelaFile;
import org.sela.data.WavFile;
import org.sela.exception.FileException;

public final class App {
    public static void main(String[] args) {
        System.out.println("\u001B[1mSimplE Lossless Audio. Released under MIT license\u001B[0m");
        if (args.length < 2) {
            printUsage();
        } else {
            try {
                parseCommandLineArgs(args);
            } catch (Exception e) {
                System.err.println("\u001B[1m" + e.getMessage() + ". Aborting...\u001B[0m");
                e.printStackTrace();
            }
        }
    }

    private static void parseCommandLineArgs(String[] args) throws IOException, FileException, LineUnavailableException {
        if (args[0].equals("-e") && args.length == 3) {
            File inputFile = new File(args[1]);
            File outputFile = new File(args[2]);
            System.out.println("\u001B[1mEncoding: \u001B[0m" + inputFile.getAbsolutePath());
            encodeFile(inputFile, outputFile);
        } else if (args[0].equals("-d") && args.length == 3) {
            File inputFile = new File(args[1]);
            File outputFile = new File(args[2]);
            System.out.println("\u001B[1mDecoding: \u001B[0m" + inputFile.getAbsolutePath());
            decodeFile(inputFile, outputFile);
        } else if (args[0].equals("-p") && args.length == 2) {
            File inputFile = new File(args[1]);
            System.out.println("\u001B[1mPlaying: \u001B[0m" + inputFile.getAbsolutePath());
            playFile(inputFile);
            System.out.println("");
        } else {
            System.out.println("Invalid arguments..");
            printUsage();
            return;
        }
        System.out.println("\u001B[1mDone\u001B[0m");
    }

    private static void encodeFile(File inputFile, File outputFile) throws IOException, FileException {
        Encoder selaEncoder = new Encoder(inputFile, outputFile);
        SelaFile selaFile = selaEncoder.process();
        selaFile.writeToStream();
    }

    private static void decodeFile(File inputFile, File outputFile) throws IOException, FileException {
        Decoder selaDecoder = new Decoder(inputFile, outputFile);
        WavFile wavFile = selaDecoder.process();
        wavFile.writeToStream();
    }

    private static void playFile(File inputFile) throws IOException, FileException, LineUnavailableException {
        Player selaPlayer = new Player(inputFile);
        selaPlayer.play();
    }

    private static void printUsage() {
        System.out.println("");
        System.out.println("\u001B[1mUsage:\u001B[0m");
        System.out.println("");
        System.out.println("\u001B[1mEncoding a file:\u001B[0m");
        System.out.println("java -jar sela.jar -e path/to/input.wav path/to/output.sela");
        System.out.println("");
        System.out.println("\u001B[1mDecoding a file:\u001B[0m");
        System.out.println("java -jar sela.jar -d path/to/input.sela path/to/output.wav");
        System.out.println("");
        System.out.println("\u001B[1mPlaying a file:\u001B[0m");
        System.out.println("java -jar sela.jar -p path/to/input.sela");
        System.out.println("");
    }
}
