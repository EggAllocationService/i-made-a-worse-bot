package io.egg.badidea.speakerHandler;

import java.nio.ByteBuffer;

import io.egg.badidea.mixing.AudioMixer;
import net.dv8tion.jda.api.audio.AudioSendHandler;

public class SpeakerSendHandler implements AudioSendHandler {

    @Override
    public boolean canProvide() {

       return AudioMixer.canProvide();
    }

    @Override
    public ByteBuffer provide20MsAudio() {
    
        return ByteBuffer.wrap(AudioMixer.provideFrame());
    }

    @Override
    public boolean isOpus() {
        return false;
    }
}
