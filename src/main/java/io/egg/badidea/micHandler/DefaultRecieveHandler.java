package io.egg.badidea.micHandler;


import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import javax.sound.sampled.AudioInputStream;

import io.egg.badidea.protocol.MicInputStream;
import io.egg.badidea.speakerHandler.SpeakerThread;
import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.UserAudio;
import net.dv8tion.jda.api.entities.User;

public class DefaultRecieveHandler implements AudioReceiveHandler {
    public static final byte[] SILENCE = new byte[SpeakerThread.LEN_AUDIO_BYTES];
    public static HashMap<User, MicInputStream> audioStreams = new HashMap<>();
    public static ArrayList<LockWait> lockWaits = new ArrayList<>();
    static boolean locked = false;
    public DefaultRecieveHandler() {

    }

    @Override
    public boolean canReceiveCombined() {
        return false;
    }
    @Override
    public void handleUserAudio(UserAudio a) {
        MicInputStream m;
        if (a.getUser() == null)
            return;
        User u = a.getUser();
        byte[] o = a.getAudioData(0.8f);
        byte[] resampled = shittyResample(o);
        if (!audioStreams.containsKey(u)) {
            m = new MicInputStream();
            if (!locked) {
                audioStreams.put(u, m);
            } else {
                lockWaits.add(new LockWait(u, m)); 
            }

        } else {
            m = audioStreams.get(u);
        }

        m.write(resampled);

    }
    public static void lock() {
        locked = true;
    }
    public static void unlock() {
        locked = false;
        for (LockWait u : lockWaits) {
            if (!u.remove) {
                audioStreams.put(u.user, u.stream);
            } else {
                audioStreams.remove(u.user);
            }
        }
        lockWaits.clear();
    }
    public static void remove(User u) {
        if (locked) {
            lockWaits.add(new LockWait(u));
        } else {
            audioStreams.remove(u);
        }
    }

    @Override
    public boolean canReceiveUser() {
        return true;
    }
    static class LockWait {
        public User user;
        public MicInputStream stream;
        public boolean remove = false;
        public LockWait(User u, MicInputStream s) {
            user = u;
            stream = s;
        }
        public LockWait(User u) {
            remove = true;
            user = u;
            stream = null;
        }
    }
    public byte[] shittyResample(byte[] data) {
        ByteBuffer in = ByteBuffer.wrap(data);
        ByteBuffer out = ByteBuffer.allocate((int) Math.ceil((data.length / 2) / 3));
        while (out.position() < 640) {
            short a1 = in.getShort();
            short b1 = in.getShort();
            int c1 = (a1 + b1) / 2;
            short a2 = in.getShort();
            short b2 = in.getShort();
            int c2 = (a2 + b2) / 2;
            short a3 = in.getShort();
            short b3 = in.getShort();
            int c3 = (a3 + b3) / 2;
            short c = (short) ((c1 + c2 + c3) / 3);
            out.putShort(c);
        }
        return out.array();
    }
}
