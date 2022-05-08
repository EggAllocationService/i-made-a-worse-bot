package io.egg.badidea.mixing;

import java.nio.ByteBuffer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;

import io.egg.badidea.protocol.TrackScheduler;
import io.egg.badidea.speakerHandler.SpeakerThread;

public class AudioMixer {
    public static AudioPlayer audioPlayer;
    public static TrackScheduler trackScheduler;
    public static AudioSink notificationSink = new AudioSink();
    static boolean isMuted = false;
    static byte[] lastFrame = null;
    public static byte[] provideFrame() {
        if (notificationSink.canProvideFrame() && lastFrame == null ) {
            return notificationSink.provide();
        }
        if (!notificationSink.canProvideFrame() && lastFrame != null && !isMuted) {
            var c = lastFrame;
            lastFrame = null;
            return c;
        }

        var factor = isMuted ? 0.1 : 1;
        short[] buffer = new short[SpeakerThread.LEN_AUDIO_BYTES / 2];
        ByteBuffer.wrap(lastFrame).asShortBuffer().get(buffer);
        short[] notification = new short[SpeakerThread.LEN_AUDIO_BYTES / 2];
        if (notificationSink.canProvideFrame()) {
            ByteBuffer.wrap(notificationSink.provide()).asShortBuffer().get(notification);
        }
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = (short) (((short) (buffer[i] * factor)) + notification[i]);

        }
        ByteBuffer output = ByteBuffer.allocate(SpeakerThread.LEN_AUDIO_BYTES);
        output.asShortBuffer().put(buffer);
        lastFrame = null;
        return output.array();
    }
    public static boolean canProvide() {
        if (notificationSink.canProvideFrame()) return true;
        if (audioPlayer == null) return false;
        var x = audioPlayer.provide();
        if (x != null) {
            lastFrame = x.getData();
            return true;
        }
        return false;

    }

    public static void nowListening() {
        isMuted = true;
    }
    public static void stopListening() {
        isMuted = false;
    }
    
}
