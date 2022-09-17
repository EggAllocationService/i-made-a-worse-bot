package io.egg.badidea.tts;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.function.Consumer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioFormat.Encoding;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;

import io.egg.badidea.mixing.AudioMixer;

public class TtsPlayerHandler implements Consumer<byte[]> {
    public static AudioFormat DISCORD_FORMAT = new AudioFormat(Encoding.PCM_SIGNED, 48000, 16, 2, 4, 48000, true);
    @Override
    public void accept(byte[] t) {
        InputStream wrapped = new ByteArrayInputStream(t);
        try {
            AudioInputStream in_raw = AudioSystem.getAudioInputStream(wrapped);
            var out = AudioSystem.getAudioInputStream(DISCORD_FORMAT, in_raw);
            ByteArrayOutputStream os = new ByteArrayOutputStream((int) Math.floor(t.length * 2.2 * 2));
            IOUtils.copy(out, os);
            os.flush();
            os.close();
            AudioMixer.notificationSink.push(os.toByteArray());

        } catch (Exception e) {
            e.printStackTrace();
        }
        
        
    }
    
}
