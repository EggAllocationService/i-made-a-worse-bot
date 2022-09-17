package io.egg.badidea.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioFormat.Encoding;

import io.egg.badidea.speakerHandler.SpeakerThread;

public class MicInputStream extends InputStream {
    // public ByteBuffer buffer = ByteBuffer.allocate(SpeakerThread.LEN_AUDIO_BYTES
    // * 100);
    public short[] buffer = new short[SpeakerThread.LEN_AUDIO_BYTES * 3];
    public int writePos = 0;
    public int expectedLen = 512;
    

    @Override
    public int read() throws IOException {
        return (int) buffer[0];
        
    }
    public MicInputStream() {
        super();
    }
    public MicInputStream(int bufferSize) {
        super();
        buffer = new short[bufferSize];
    }

    public void write(byte[] b) {
        if (writePos + b.length > buffer.length) {
            short[] tmp = new short[writePos + (b.length * 2)];
            System.arraycopy(buffer, 0, tmp, 0, writePos);
            System.out.println("WARN: Had to extend buffer from " + buffer.length + " to " + tmp.length);
            System.out.println("WARN: Maybe a consumer thread is hung?");
            buffer = tmp;
            
        }
        short[] array = new short[b.length / 2];
        ByteBuffer.wrap(b).asShortBuffer().get(array);
        System.arraycopy(array, 0, buffer, writePos, array.length);
        writePos += array.length;
    }

    public boolean canProvideLenBytes(int bytes) {
        // System.out.println("Asking for " + bytes + " shorts at pos " + writePos);
        return writePos >= bytes;
        // return writePos != 0;
    }
    public int available() {
        return writePos;
    }
    public short[] getAll() {
        short[] b = new short[writePos];

        System.arraycopy(buffer, 0, b, 0, writePos);
        writePos = 0;
        return b;
    }
    public void read(short[] s) {

        System.arraycopy(buffer, 0, s, 0, s.length);
        System.arraycopy(buffer, s.length, buffer, 0, buffer.length -  s.length);
        writePos -=  s.length;

    }
}
