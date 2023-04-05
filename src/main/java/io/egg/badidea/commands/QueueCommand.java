package io.egg.badidea.commands;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import io.egg.badidea.BaseCommand;
import io.egg.badidea.mixing.AudioMixer;
import io.egg.badidea.tts.*;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class QueueCommand extends BaseCommand {
    public QueueCommand() {
        super("queue");
    }

    @Override
    public boolean requiresVoiceConnection() {
        return true;
    }
    @Override
    protected ArrayList<OptionData> createSlashOptions() {
        var things = new ArrayList<OptionData>();
        return things;
    }
    
    @Override
    public boolean shouldHandleVoiceCommand(String in, Member from) {
        return in.startsWith("what's up next") || in.startsWith("what's the next song") || (in.startsWith("what's") && in.endsWith("in the queue") && in.contains("next"));
    }
    
    @Override
    public void handleVoiceCommand(String in, Member from) {
        // get next track
        var c = AudioMixer.trackScheduler.queue.peek();
        String speech;
        if (c == null) {
            speech = "sorry, but nothing is queued up next.";
        } else {
            var name = c.getInfo().title;
            var author = c.getInfo().author;
            speech = random("the next song is", "up next is", "the next song in the queue is") + " " + name + ", by " + author + ".";
        }
        TtsThread.submitJob(new TtsJob(speech, new TtsPlayerHandler()));
    }
    @Override
    public void handleSlashCommand(SlashCommandInteractionEvent event) {
        var msgStart = "```\n";
        msgStart = msgStart + getTrackInfo(AudioMixer.audioPlayer.getPlayingTrack(), true);
        for (var t : AudioMixer.trackScheduler.queue) {
            msgStart += "\n" + getTrackInfo(t, false);
        }
        msgStart += "```";
        event.reply(msgStart).queue();
        event.getHook().deleteOriginal().queueAfter(10, TimeUnit.SECONDS);

    }
    private String getTrackInfo(AudioTrack t, boolean current) {
        if (t == null) {
            return "  [unavailable]";
        }
        var name = t.getInfo().title;
        var author = t.getInfo().author;
        return (current ? "->" : "  ") + name + " by " + author + " " + (String) t.getUserData();
    }
}
