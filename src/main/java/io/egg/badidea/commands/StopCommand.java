package io.egg.badidea.commands;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import io.egg.badidea.BaseCommand;
import io.egg.badidea.mixing.AudioMixer;
import io.egg.badidea.transcribe.TranscriptionThread;
import io.egg.badidea.tts.*;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class StopCommand extends BaseCommand{
    public StopCommand() {
        super("stop");

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
        if (in.startsWith("stop")  || in.equals("shut up") || in.equals("shut the fuck up")) {
         return true;
        } 

        return false;
    }
    
    @Override
    public void handleVoiceCommand(String in, Member from) {
        AudioMixer.trackScheduler.stop();
        //AudioMixer.notificationSink.push(TranscriptionThread.successNoise);
        /*String speech = random("alright", "okay", "sure") + ", stopping " + random("the song", "playback") + ".";
        TtsThread.submitJob(new TtsJob(speech, new TtsPlayerHandler()));*/
        AudioMixer.notificationSink.push(TranscriptionThread.successNoise);
    }
    @Override
    public void handleSlashCommand(SlashCommandInteractionEvent event) {
        AudioMixer.trackScheduler.stop();
        event.reply("<a:check2:1020363266930790400> Stopped playing!").complete();
        event.getHook().deleteOriginal().queueAfter(4, TimeUnit.SECONDS);

    }
}
