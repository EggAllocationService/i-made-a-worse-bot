package io.egg.badidea.commands;

import io.egg.badidea.BaseCommand;
import io.egg.badidea.mixing.AudioMixer;
import io.egg.badidea.tts.TtsJob;
import io.egg.badidea.tts.TtsPlayerHandler;
import io.egg.badidea.tts.TtsThread;
import net.dv8tion.jda.api.entities.Member;

public class PlayingCommand extends BaseCommand {

    public PlayingCommand() {
        super("playing");
    }

    @Override
    public boolean requiresVoiceConnection() {
        return true;
    }

    @Override
    public boolean shouldHandleVoiceCommand(String in, Member from) {
        return in.startsWith("what song is this") || in.startsWith("what is this") || in.startsWith("what's playing") || in.startsWith("what is playing");
    }

    @Override
    public void handleVoiceCommand(String in, Member from) {
        var c = AudioMixer.audioPlayer.getPlayingTrack();
        String speech;
        if (c == null) {
            speech = "sorry, but nothing is playing right now.";
        } else {
            var name = c.getInfo().title;
            var author = c.getInfo().author;
            speech = random("the current song is", "this song is called", "this is", "this is called") + " " + name + ", by " + author + ".";
        }
        

        TtsThread.submitJob(new TtsJob(speech, new TtsPlayerHandler()));

        // TtsThread.submitJob(new TtsJob("my speech subsystem is fully functional,
        // hello " + from.getEffectiveName().toLowerCase(), new TtsPlayerHandler()));
    }
}