package io.egg.badidea.wakeWordHandler;

import java.util.HashMap;

import ai.picovoice.porcupine.Porcupine;
import ai.picovoice.porcupine.PorcupineException;
import io.egg.badidea.Main;
import io.egg.badidea.micHandler.DefaultRecieveHandler;
import io.egg.badidea.mixing.AudioMixer;
import io.egg.badidea.protocol.MicInputStream;
import net.dv8tion.jda.api.entities.User;

public class WakeWordThread extends Thread {

    public static HashMap<User, Porcupine> wakeWordMap = new HashMap<User, Porcupine>();
    boolean running = true;
    public WakeWordThread() throws PorcupineException {
        super("Wake Word Thread");
    }
    @Override
    public void run() {
        while (running) {
            for (User u : DefaultRecieveHandler.audioStreams.keySet()) {
                if (Main.transcriptionThread.transcribeFromUser != null && Main.transcriptionThread.transcribeFromUser.equals(u)) {
                    continue;
                }
                if (!wakeWordMap.containsKey(u) || wakeWordMap.get(u) == null) {
                    wakeWordMap.put(u, createPorcupine());
                    System.out.println("Created a Porcqupine listener for " + u.getAsTag());
                    
                }
                Porcupine userPq = wakeWordMap.get(u);
                MicInputStream stream = DefaultRecieveHandler.audioStreams.get(u);
                if (!stream.canProvideLenBytes(userPq.getFrameLength())) {
                    continue;
                }
                short[] s = new short[userPq.getFrameLength()];
                stream.read(s);
                int result;
                try {
                    result = userPq.process(s);
                } catch (PorcupineException e) {
                    e.printStackTrace();
                    continue;
                }
                //System.out.println("Portcquipe result: " + result + " With first short: " + s[1] + " At time " + System.currentTimeMillis() );
                if(result == -1) continue;
                System.out.println("Wake word detected from user " + u.getAsTag() + " with result " + result);
                if (Main.transcriptionThread.transcribeFromUser != null) {
                    continue;
                }
                AudioMixer.nowListening();
                Main.transcriptionThread.beginTranscription(u);
            }
            try {
                Thread.sleep(18);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void userDisconnect(User u) {
        if (!wakeWordMap.containsKey(u)) return;
        wakeWordMap.get(u).delete();
        wakeWordMap.remove(u);
        DefaultRecieveHandler.audioStreams.remove(u);
    }
    public static void reset() {
        for (User u : wakeWordMap.keySet()) {
            userDisconnect(u);
        }
    }
    private static Porcupine createPorcupine() {
        try {
            return new Porcupine.Builder()
                    .setLibraryPath(Main.config.libporcupinePath)
                    //.setBuiltInKeyword(BuiltInKeyword.HEY_GOOGLE)
                    .setKeywordPath(Main.config.wakeWord)
                    .setAccessKey(Main.config.picoToken)
                    .build();
        } catch (PorcupineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }
}
