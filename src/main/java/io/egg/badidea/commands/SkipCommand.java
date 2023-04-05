package io.egg.badidea.commands;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import io.egg.badidea.BaseCommand;
import io.egg.badidea.mixing.AudioMixer;
import io.egg.badidea.transcribe.TranscriptionThread;
import io.egg.badidea.tts.TtsJob;
import io.egg.badidea.tts.TtsPlayerHandler;
import io.egg.badidea.tts.TtsThread;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class SkipCommand extends BaseCommand{
    public SkipCommand() {
        super("skip");
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
        if (in.startsWith("skip") || in.startsWith("next song") || in.startsWith("next some")) {
         return true;
        } 

        return false;
    }
    
    @Override
    public void handleVoiceCommand(String in, Member from) {
        AudioMixer.trackScheduler.nextTrack();
        AudioMixer.notificationSink.push(TranscriptionThread.successNoise);
    }
    @Override
    public void handleSlashCommand(SlashCommandInteractionEvent event) {
        AudioMixer.trackScheduler.nextTrack();
        event.reply("<a:check2:1020363266930790400> Skipped").complete();
        event.getHook().deleteOriginal().queueAfter(4, TimeUnit.SECONDS);

    }
}
