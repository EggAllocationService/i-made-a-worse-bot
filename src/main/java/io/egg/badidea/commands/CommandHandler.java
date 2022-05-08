package io.egg.badidea.commands;


import io.egg.badidea.Main;
import io.egg.badidea.mixing.AudioMixer;
import io.egg.badidea.transcribe.TranscriptionThread;
import io.egg.badidea.wakeWordHandler.WakeWordThread;
import net.dv8tion.jda.api.entities.Member;

public class CommandHandler {
    public static void handleCommand(String in, Member from) {
        if (in.startsWith("play") || in.startsWith("plane") || in.startsWith("place") || in.startsWith("queue") || in.startsWith("clay") || in.startsWith("plate")) {
            AudioMixer.notificationSink.push(TranscriptionThread.ackNoise);
            var tmp = in.split(" ");
            tmp[0] = "";
            var text = String.join(" ", tmp);
            System.out.println("searching youtube for " + text);
            Main.audioManager.loadItem("ytmsearch: " + text , new LoadHandler());
        } else if (in.startsWith("stop") || in == "pop" || in == "thought" || in.equals("shut up")) {
            AudioMixer.trackScheduler.stop();
            AudioMixer.notificationSink.push(TranscriptionThread.successNoise);
        } else if (in.startsWith("skip") || in.startsWith("next song") || in.startsWith("next some") || in == "next") {
            AudioMixer.trackScheduler.nextTrack();
            AudioMixer.notificationSink.push(TranscriptionThread.successNoise);
        } else if (in.equals("who was in paris") || in.equals("who is in paris") || in.equals("who's in paris")) {
             AudioMixer.notificationSink.push(TranscriptionThread.ackNoise);
            System.out.println("finding people in paris");
              Main.audioManager.loadItem("ytmsearch: Ni**as In Paris", new LoadHandler());
        }  else {
            AudioMixer.notificationSink.push(TranscriptionThread.failNoise);
        }
    }
}
 