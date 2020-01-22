package org.sela;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.LineUnavailableException;

import org.sela.data.SelaFile;
import org.sela.data.WavFile;
import org.sela.exception.FileException;

public final class App {
    public static void main(final String[] args) {
        System.out.println("SimplE Lossless Audio. Released under MIT license");
        if (args.length < 2) {
            printUsage();
        } else {
            try {
                parseCommandLineArgs(args);
            } catch (final Exception e) {
                System.err.println(e.getMessage() + ". Aborting...");
                e.printStackTrace();
            }
        }
    }

    private static void parseCommandLineArgs(final String[] args)
            throws IOException, FileException, LineUnavailableException, InterruptedException {
        if (args[0].equals("-e") && args.length == 3) {
            final File inputFile = new File(args[1]);
            final File outputFile = new File(args[2]);
            System.out.println("Encoding: " + inputFile.getAbsolutePath());
            encodeFile(inputFile, outputFile);
        } else if (args[0].equals("-d") && args.length == 3) {
            final File inputFile = new File(args[1]);
            final File outputFile = new File(args[2]);
            System.out.println("Decoding: " + inputFile.getAbsolutePath());
            decodeFile(inputFile, outputFile);
        } else if (args[0].equals("-p") && args.length == 2) {
            final File inputFile = new File(args[1]);
            System.out.println("Playing: " + inputFile.getAbsolutePath());
            playSelaFile(inputFile);
            System.out.println("");
        } else if(args[0].equals("-w") && args.length == 2) {
            final File inputFile = new File(args[1]);
            System.out.println("Playing: " + inputFile.getAbsolutePath());
            playWavFile(inputFile);
            System.out.println("");
        } else {
            System.out.println("Invalid arguments...");
            printUsage();
            return;
        }
        System.out.println("Done");
    }

    private static void encodeFile(final File inputFile, final File outputFile) throws IOException, FileException {
        final Encoder selaEncoder = new Encoder(inputFile, outputFile);
        final SelaFile selaFile = selaEncoder.process();
        selaFile.writeToStream();
    }

    private static void decodeFile(final File inputFile, final File outputFile) throws IOException, FileException {
        final Decoder selaDecoder = new Decoder(inputFile, outputFile);
        final WavFile wavFile = selaDecoder.process();
        wavFile.writeToStream();
    }

    private static void playSelaFile(final File inputFile) throws IOException, FileException, LineUnavailableException, InterruptedException {
        final Player selaPlayer = new Player(inputFile);
        selaPlayer.play();
    }

    private static void playWavFile(final File inputFile) throws IOException, FileException, LineUnavailableException, InterruptedException {
        final WavPlayer wavPlayer = new WavPlayer(inputFile);
        wavPlayer.play();
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
        System.out.println("Playing a sela file:");
        System.out.println("java -jar sela.jar -w path/to/input.wav");
        System.out.println("");
    }
}
