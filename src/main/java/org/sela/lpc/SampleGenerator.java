package org.sela.lpc;

import org.sela.data.*;

public final class SampleGenerator extends LinearPredictionBase {
    private int[] residues;
    private int[] samples;
    
    public SampleGenerator(LpcEncodedData encodedData) {
        super(encodedData.quantizedReflectionCoefficients, encodedData.optimalLpcOrder);
        this.residues = encodedData.residues;
        this.samples = new int[residues.length];
    }

    private void generateSamples() {
        long correction = (long) 1 << (super.correctionFactor - 1);
        
        samples[0] = residues[0];
        
        for (int i = 1; i <= super.optimalLpcOrder; i++) {
            long temp = correction;
            for (int j = 1; j <= i; j++) {
                temp -= super.linearPredictionCoefficients[j] * samples[i - j];
            }
            samples[i] = residues[i] - (int) (temp >> super.correctionFactor);
        }

        for (int i = super.optimalLpcOrder + 1; i < residues.length; i++) {
            long temp = correction;
            for (int j = 0; j <= super.optimalLpcOrder; j++)
                temp -= (super.linearPredictionCoefficients[j] * samples[i - j]);
            samples[i] = residues[i] - (int) (temp >> super.correctionFactor);
        }
    } 

    public LpcDecodedData process() {
        super.dequantizeReflectionCoefficients();
        super.generatelinearPredictionCoefficients();
        generateSamples();
        return new LpcDecodedData(samples);
    }
}