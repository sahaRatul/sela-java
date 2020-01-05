package org.sela.data;

public final class RiceEncodedData {
    public int optimumRiceParam;
    public int dataCount;
    public int[] encodedData;

    public RiceEncodedData(int optimumRiceParam, int dataCount, int[] encodedData) {
        this.optimumRiceParam = optimumRiceParam;
        this.dataCount = dataCount;
        this.encodedData = encodedData;
    }
}