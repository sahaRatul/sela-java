package org.sela;

import org.sela.lpc.ResidueGenerator;

/**
 * Hello world!
 */
public final class App {
    private App() {
    }

    /**
     * Says hello to the world.
     * 
     * @param args The arguments of the program.
     */
    public static void main(String[] args) {
        int[] samples = new int[2048];
        for(int i = 0; i < samples.length; i++) {
            samples[i] = (int)(32767 * Math.sin(Math.toRadians(i)));
        }

        ResidueGenerator resGen = new ResidueGenerator(samples);
        resGen.process();
    }
}
