package io.egg.badidea.wakeWordHandler;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import ai.picovoice.porcupine.Porcupine;
import ai.picovoice.porcupine.PorcupineException;
import io.egg.badidea.Main;
import io.egg.badidea.micHandler.DefaultRecieveHandler;
import io.egg.badidea.mixing.AudioMixer;
import io.egg.badidea.mixing.DynamicAudioMixer;
import io.egg.badidea.protocol.MicInputStream;
import io.egg.badidea.transcribe.TranscriptionThread;
import net.dv8tion.jda.api.entities.User;

public class WakeWordThread extends Thread {

    public static HashMap<User, Porcupine> wakeWordMap = new HashMap<User, Porcupine>();
    boolean running = true;
    public ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public WakeWordThread() throws PorcupineException {
        super("Wake Word Thread");
    }

    @Override
    public void run() {
        while (running) {
            try {
                DefaultRecieveHandler.lock();
                var start = Instant.now();
                var futures = new ArrayList<Future<?>>();
                ConcurrentLinkedQueue<User> results = new ConcurrentLinkedQueue<>();
                for (User u : DefaultRecieveHandler.audioStreams.keySet()) {
                    if (Main.transcriptionThread.transcribeFromUser != null
                            && Main.transcriptionThread.transcribeFromUser.equals(u)) {
                        continue;
                    }
                    if (!wakeWordMap.containsKey(u) || wakeWordMap.get(u) == null) {
                        wakeWordMap.put(u, createPorcupine());
                        System.out.println("Created a Porcqupine listener for " + u.getAsTag());

                    }
                    futures.add(pool.submit(() -> {

                        
                        Porcupine userPq = wakeWordMap.get(u);
                        MicInputStream stream = DefaultRecieveHandler.audioStreams.get(u);
                        if (userPq == null) return;
                        if (!stream.canProvideLenBytes(userPq.getFrameLength())) {
                            return;
                        }
                        short[] s = new short[userPq.getFrameLength()];
                        stream.read(s);
                        int result;
                        try {
                            result = userPq.process(s);
                        } catch (PorcupineException e) {
                            e.printStackTrace();
                            return;
                        }
                        if (result == -1)
                            return;
                        results.add(u);
                    }));

                  
                }
                for (var u : futures) {
                    u.get();
                }
                User found = results.poll();
                if(found != null) {
                    MicInputStream stream = DefaultRecieveHandler.audioStreams.get(found);
                    if (stream.canProvideLenBytes(320)) {
                        TranscriptionThread.offerPrerec(stream.getAll());
                    }
                    System.out.println("Wake word detected from user " + found.getAsTag());
                    if (Main.transcriptionThread.transcribeFromUser == null) {
                        AudioMixer.musicStream.setVolume(10);
                     Main.transcriptionThread.beginTranscription(found);
                    }
                    
                }
                futures.clear();
                results.clear();
                DefaultRecieveHandler.unlock();
                Instant finish = Instant.now();
                long diff = ChronoUnit.MILLIS.between(start, finish);
                try {
                    var tdelta = (long) (19 - Math.floor(diff));
                    if (tdelta < 0) {
                        System.out.println("WARN: Can't keep up! Running " + (tdelta * -1) + "ms behind" );
                    } else {
                        Thread.sleep(tdelta);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("FATAL: Wake trigger thread is exiting");
                Runtime.getRuntime().halt(-1);
            }

        }

    }

    public static void userDisconnect(User u) {
        if (!wakeWordMap.containsKey(u))
            return;
        while (DefaultRecieveHandler.locked) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        wakeWordMap.get(u).delete();
        wakeWordMap.remove(u);
        DefaultRecieveHandler.remove(u);
    }

    public static void reset() {
        for (User u : wakeWordMap.keySet()) {
            userDisconnect(u);
        }
    }

    public static Porcupine createPorcupine() {
        try {
            return new Porcupine.Builder()
                    .setLibraryPath(Main.config.libporcupinePath)
                    // .setBuiltInKeyword(BuiltInKeyword.HEY_GOOGLE)
                    .setKeywordPath(Main.config.wakeWord)
                    .setAccessKey(Main.config.picoToken)
                    .build();
        } catch (PorcupineException e) {
            e.printStackTrace();
            return null;
        }
    }
}
/*
 * 
 * if (!wakeWordMap.containsKey(u) || wakeWordMap.get(u) == null) {
 * wakeWordMap.put(u, createPorcupine());
 * System.out.println("Created a Porcqupine listener for " + u.getAsTag());
 * 
 * }
 * Porcupine userPq = wakeWordMap.get(u);
 * MicInputStream stream = DefaultRecieveHandler.audioStreams.get(u);
 * if (!stream.canProvideLenBytes(userPq.getFrameLength())) {
 * continue;
 * }
 * short[] s = new short[userPq.getFrameLength()];
 * stream.read(s);
 * int result;
 * try {
 * result = userPq.process(s);
 * } catch (PorcupineException e) {
 * e.printStackTrace();
 * continue;
 * }
 * if (result == -1)
 * continue;
 */