package io.egg.badidea.mixing;

import java.nio.ByteBuffer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;

public class WrappedAudioPlayerStream extends CabotAudioStream {
    AudioPlayer p;
    AudioFrame last;
    
    public WrappedAudioPlayerStream(AudioPlayer player) {
        p = player;
    }

    @Override
    public short[] provide() {
        if (last == null) return null;
        var data = last.getData();
        last = null;
        var wrapped = new short[data.length / 2];
        ByteBuffer.wrap(data).asShortBuffer().get(wrapped);
        mul(wrapped, (float) (volume * 0.01));
        return wrapped;
    }

    @Override
    public boolean canProvideFrame() {
        if (last != null) return true;
        var x = p.provide();
        if (x == null) {
            return false;
        } else {
            last = x;
            return true;
        }
    }
    
}
