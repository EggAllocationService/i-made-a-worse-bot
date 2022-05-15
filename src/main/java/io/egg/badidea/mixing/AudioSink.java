package io.egg.badidea.mixing;

import io.egg.badidea.speakerHandler.SpeakerThread;

public class AudioSink {
    byte[] buffer = new byte[48000 * 2 * 2 * 10]; // 48khz ( 2 bytes per short) * 2 channels * 10 seconds
    int writePos = 0;

    public void push(byte[] audio) {
        System.arraycopy(audio, 0, buffer, writePos, audio.length);
        writePos += audio.length;
    }

    public boolean canProvideFrame() {
        return writePos != 0;
    }
    public byte[] provide() {
        byte[] res = new byte[SpeakerThread.LEN_AUDIO_BYTES];
        if (writePos < SpeakerThread.LEN_AUDIO_BYTES) {
            System.arraycopy(buffer, 0, res, 0, writePos);
            writePos = 0;
            return res;
        } else {
            System.arraycopy(buffer, 0, res, 0, SpeakerThread.LEN_AUDIO_BYTES);
            System.arraycopy(buffer, SpeakerThread.LEN_AUDIO_BYTES, buffer, 0, writePos - SpeakerThread.LEN_AUDIO_BYTES);
            writePos -= SpeakerThread.LEN_AUDIO_BYTES;
            
        }
        if (writePos < 0) writePos = 0;
        return res;
    }
}
