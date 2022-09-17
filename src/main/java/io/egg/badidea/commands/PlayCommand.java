package io.egg.badidea.commands;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import io.egg.badidea.BaseCommand;
import io.egg.badidea.Main;
import io.egg.badidea.mixing.AudioMixer;
import io.egg.badidea.transcribe.TranscriptionThread;
import io.egg.badidea.tts.TtsJob;
import io.egg.badidea.tts.TtsPlayerHandler;
import io.egg.badidea.tts.TtsThread;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class PlayCommand extends BaseCommand {

    public PlayCommand() {
        super("play");

    }

    @Override
    public boolean requiresVoiceConnection() {
        return true;
    }
    @Override
    protected ArrayList<OptionData> createSlashOptions() {
        var things = new ArrayList<OptionData>();
        things.add(new OptionData(OptionType.STRING, "search", "Song to search for").setRequired(true));
        things.add(new OptionData(OptionType.BOOLEAN, "youtube", "If true, search on YouTube instead of YouTube Music").setRequired(false));
        return things;
    }
    @Override
    public boolean shouldHandleVoiceCommand(String in, Member from) {
        if (in.startsWith("play") || in.startsWith("lay") || in.startsWith("clay") || in.startsWith("queue") || in.startsWith("place") || in.startsWith("plate") || in.startsWith("plain") || in.startsWith("flay")) {
            return true;
        } else if (in.equals("who was in paris") || in.equals("who is in paris") || in.equals("who's in paris")) {
            return true;
        }

        return false;
    }
    
    @Override
    public void handleVoiceCommand(String in, Member from) {
        AudioMixer.notificationSink.push(TranscriptionThread.ackNoise);
        if (in.equals("who was in paris") || in.equals("who is in paris") || in.equals("who's in paris")) {
            System.out.println("finding people in paris");
            Main.audioManager.loadItem("ytmsearch: Ni**as In Paris", new LoadHandler(c -> AudioMixer.notificationSink.push(TranscriptionThread.successNoise)));
            return;
        }
        var tmp = in.split(" ");

        tmp[0] = "";
        var start = "ytmsearch: ";
        var text = String.join(" ", tmp);
        if (text.endsWith("on youtube")) {
            start = "ytsearch: ";
            text = text.replace("on youtube", "");
        }
        System.out.println("searching youtube for " + text);
        Main.audioManager.loadItem(start + text , new LoadHandler(c -> {
            var name = c.getInfo().title;
            var author = c.getInfo().author;
            AudioMixer.notificationSink.push(TranscriptionThread.successNoise);
           /*  if (AudioMixer.trackScheduler.queue.size() == 0) {
                // meaning nothing was added to queue
               String speech = random("alright", "okay", "sure") + ", playing " + name + " by " + author + ".";
                TtsThread.submitJob(new TtsJob(speech, new TtsPlayerHandler()));
            } else {
                AudioMixer.notificationSink.push(TranscriptionThread.successNoise)
            }*/
        }));
    }
    @Override
    public void handleSlashCommand(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        var start = "ytmsearch: ";
        var text = event.getOption("search").getAsString();
        if (event.getOption("youtube") != null && event.getOption("youtube").getAsBoolean()) {
            start = "ytsearch: ";
        }
        System.out.println("searching youtube for " + text);
        Main.audioManager.loadItem(start + text , new LoadHandler(c -> {
            if (c == null) {
                event.getHook().sendMessage(":x: There was an error queueing that track.").complete();
            } else {
                event.getHook().sendMessage(":white_check_mark: Queued **" + c.getInfo().title + "**").complete();
            }
            
            event.getHook().deleteOriginal().queueAfter(4, TimeUnit.SECONDS);
        }));

    }
}
