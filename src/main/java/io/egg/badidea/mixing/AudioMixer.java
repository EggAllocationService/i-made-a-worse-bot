package io.egg.badidea.mixing;

import java.nio.ByteBuffer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;

import io.egg.badidea.protocol.TrackScheduler;

public class AudioMixer {
    public static AudioPlayer audioPlayer;
    public static TrackScheduler trackScheduler;
    public static WrappedAudioPlayerStream musicStream;
    public static CabotAudioStream notificationSink = new CabotAudioStream(48000 * 2 * 10);
    public static AutoClosingAudioStream speechSink = new AutoClosingAudioStream(48000 * 2 * 10);
    static boolean isMuted = false;
    static byte[] lastFrame = null;
    static byte[] buffer = new byte[1920 * 2];

    static {
        speechSink.alwaysPriority = true;
        DynamicAudioMixer.channels.put("speech", speechSink);
        DynamicAudioMixer.channels.put("notifications", notificationSink);
        speechSink.setOpen(isMuted);
        notificationSink.setOpen(true);
        speechSink.setVolume(80);
        notificationSink.setVolume(60);
    }

    public static byte[] provideFrame() {
        var mixed = DynamicAudioMixer.mixAudio();
        var w = ByteBuffer.wrap(buffer).asShortBuffer().put(0, mixed);
        return buffer;
        
    }
    public static boolean canProvide() {
        return DynamicAudioMixer.canProvideFrame();
    }
    

    /*public static byte[] provideFrame() {
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
    }*/
    
}
