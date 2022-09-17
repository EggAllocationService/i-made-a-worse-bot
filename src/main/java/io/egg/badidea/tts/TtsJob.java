package io.egg.badidea.tts;

import java.util.function.Consumer;

public class TtsJob {
    // Will run on TTS thread
    public Consumer<byte[]> completionJob;
    public String toGenerate;
    public TtsJob(String text, Consumer<byte[]> callback) {
        toGenerate = text;
        completionJob = callback;
    }
}
