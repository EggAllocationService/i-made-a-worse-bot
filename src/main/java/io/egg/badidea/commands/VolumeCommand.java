package io.egg.badidea.commands;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.checkerframework.checker.units.qual.s;

import io.egg.badidea.BaseCommand;
import io.egg.badidea.mixing.AudioMixer;
import io.egg.badidea.transcribe.TranscriptionThread;
import io.egg.badidea.tts.TtsJob;
import io.egg.badidea.tts.TtsPlayerHandler;
import io.egg.badidea.tts.TtsThread;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class VolumeCommand extends BaseCommand {

    public VolumeCommand() {
        super("volume");
    }
    @Override
    public boolean requiresVoiceConnection() {
        return true;
    }
    @Override
    protected ArrayList<OptionData> createSlashOptions() {
        var things = new ArrayList<OptionData>();
        things.add(new OptionData(OptionType.INTEGER, "volume", "New volume for the bot").setRequired(true).setRequiredRange(1, 100));

        return things;
    }
    @Override
    public boolean shouldHandleVoiceCommand(String txt, Member from) {
        return txt.contains("volume") && (txt.contains("turn down") || (txt.endsWith("half") && txt.contains("set")) || txt.contains("turn up"));
    }
    @Override
    public void handleSlashCommand(SlashCommandInteractionEvent e) {
        AudioMixer.audioPlayer.setVolume(e.getOption("volume").getAsInt());
        e.reply("<a:check2:1020363266930790400> Set the player volume to **" + AudioMixer.audioPlayer.getVolume() + "**%").queue();
        e.getHook().deleteOriginal().queueAfter(4, TimeUnit.SECONDS);
    }
    @Override
    public void handleVoiceCommand(String txt, Member from) {
        int current = AudioMixer.audioPlayer.getVolume();
        if (txt.endsWith("half")) {
            AudioMixer.audioPlayer.setVolume(50);
        } else if (txt.contains("turn down")) {
            AudioMixer.audioPlayer.setVolume(Math.max(current - 10, 10));
        } else if (txt.contains("turn up")) {
            AudioMixer.audioPlayer.setVolume(Math.min(current + 10, 100));
        }
        if (AudioMixer.audioPlayer.getVolume() != current) {
            String speech = random("alright", "okay", "sure") + ", set the volume to " + AudioMixer.audioPlayer.getVolume()  + " percent.";
            TtsThread.submitJob(new TtsJob(speech, new TtsPlayerHandler()));
        } else {
            // nothing changed
            AudioMixer.notificationSink.push(TranscriptionThread.failNoise);
        }
    }
}
