package org.sela;

import java.io.File;

public final class App {
    private App() {

    }

    public static void main(String[] args) {
        try {
            File file;
            if (args.length > 0) {
                file = new File(args[0]);
            }
            else {
                file = new File("/home/ratul/Downloads/HANA - HANADRIEL/Satellite.wav");
            }
            Encoder selaEncoder = new Encoder(file);
            selaEncoder.process();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
