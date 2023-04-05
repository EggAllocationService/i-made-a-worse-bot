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
        notificationSink.setVolume(40);
    }

    public static byte[] provideFrame() {
        var mixed = DynamicAudioMixer.mixAudio();
        var w = ByteBuffer.wrap(buffer).asShortBuffer().put(0, mixed);
        return buffer;
        
    }
    public static boolean canProvide() {
        return DynamicAudioMixer.canProvideFrame();
    }
}
