package io.egg.badidea.speakerHandler;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class SpeakerThread extends Thread {
    public static final int LEN_AUDIO_BYTES = 3840;
    public static BufferedInputStream speakerPipe;
    public static boolean freshData = false;
    public static byte[] buffer = new byte[LEN_AUDIO_BYTES];

    public SpeakerThread() throws FileNotFoundException {
        speakerPipe = new BufferedInputStream(new FileInputStream("/tmp/speaker1")); 
    }

    @Override
    public void run() {
       try {
            while (true) {
               var available = speakerPipe.available();

               if (available >= LEN_AUDIO_BYTES) {
                   speakerPipe.read(buffer);
                   freshData = true;
                   Thread.sleep(20);
               } else {
                    Thread.sleep(1);
                   continue;
                   /*buffer = new byte[LEN_AUDIO_BYTES];
                   speakerPipe.read(buffer, 0, available);*/
               }
               
               //Thread.sleep(20);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
