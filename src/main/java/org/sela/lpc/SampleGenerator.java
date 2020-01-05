package org.sela.lpc;

public final class SampleGenerator extends LinearPredictionBase {
    private int[] residues;
    private int[] samples;
    
    public SampleGenerator(int[] residues, int[] quantizedReflectionCoefficients, byte optimalOrder) {
        super(quantizedReflectionCoefficients, optimalOrder);
        this.residues = residues;
        this.samples = new int[residues.length];
    }

    private void generateSamples() {
        long correction = (long) 1 << (super.correctionFactor - 1);
        
        samples[0] = residues[0];
        
        for (int i = 1; i <= super.optimalOrder; i++) {
            long temp = correction;
            for (int j = 1; j <= i; j++) {
                temp -= super.linearPredictionCoefficients[j] * samples[i - j];
            }
            samples[i] = residues[i] - (int) (temp >> super.correctionFactor);
        }

        for (int i = super.optimalOrder + 1; i < residues.length; i++) {
            long temp = correction;
            for (int j = 0; j <= super.optimalOrder; j++)
                temp -= (super.linearPredictionCoefficients[j] * samples[i - j]);
            samples[i] = residues[i] - (int) (temp >> super.correctionFactor);
        }
    } 

    public int[] process() {
        super.dequantizeReflectionCoefficients();
        super.generatelinearPredictionCoefficients();
        generateSamples();
        return samples;
    }
}