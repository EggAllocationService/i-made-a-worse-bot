package io.egg.badidea.micHandler;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import io.egg.badidea.speakerHandler.SpeakerThread;

public class MicThread extends Thread {
    public static FileChannel micPipe;
    private static FileOutputStream x;
    public static byte[] buffer = new byte[SpeakerThread.LEN_AUDIO_BYTES];

    public MicThread() throws FileNotFoundException {
        x = new FileOutputStream("/tmp/mic1");
        micPipe = x.getChannel();

    }

    @Override
    public void run() {
        while (true) {
            try {
                //micPipe.write(ByteBuffer.wrap(buffer));
                x.write(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {

                e.printStackTrace();
            }
        }
    }
}
