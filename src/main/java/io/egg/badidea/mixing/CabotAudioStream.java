package io.egg.badidea.mixing;

import java.nio.ByteBuffer;

public class CabotAudioStream {
    short[] buffer = new short[48000 * 2 * 5]; // 48khz * 2 channels * 5 seconds
    int writePos = 0;
    boolean priority = false;
    boolean open = false;
    int volume = 100;
    static final int SHORT_20MS_AUDIO_LEN = 1920; //20 milliseconds of audio at stereo 48khz

    public CabotAudioStream() {}

    public CabotAudioStream(int bufSize) {
        buffer = new short[bufSize];
    }

    public void push(short[] audio) {
        System.arraycopy(audio, 0, buffer, writePos, audio.length);
        writePos += audio.length;
    }
    public void push(byte[] audio) {
        var wrapped = new short[audio.length / 2];
        ByteBuffer.wrap(audio).asShortBuffer().get(wrapped);
        push(wrapped);
    }
    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean val) {
        open = val;
    }
    public void setPriority(boolean pri) {
        priority = pri;
    }
    public boolean isPriority() {return priority;}
    public boolean canProvideFrame() {
        return writePos != 0 && open;
    }
    public short[] provide() {
        if (!canProvideFrame()) return null; 
        short[] res = new short[SHORT_20MS_AUDIO_LEN];
        if (writePos < SHORT_20MS_AUDIO_LEN) {
            System.arraycopy(buffer, 0, res, 0, writePos);
            writePos = 0;
            return res;
        } else {
            System.arraycopy(buffer, 0, res, 0, SHORT_20MS_AUDIO_LEN);
            System.arraycopy(buffer, SHORT_20MS_AUDIO_LEN, buffer, 0, writePos - SHORT_20MS_AUDIO_LEN);
            writePos -= SHORT_20MS_AUDIO_LEN;
            
        }
        if (writePos < 0) writePos = 0;
        mul(res, volume * 0.01f);
        return res;

    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    static void mul(short[] a, float b) {
        for (int i = 0; i < a.length; i++) {
            a[i] = (short) (a[i] * b);
        }
    }
}
