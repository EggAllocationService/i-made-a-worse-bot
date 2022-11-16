package io.egg.badidea.commands;

import java.util.ArrayList;

import io.egg.badidea.BaseCommand;
import io.egg.badidea.tts.TtsJob;
import io.egg.badidea.tts.TtsThread;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class SynthCommand extends BaseCommand{

    public SynthCommand() {
        super("tts");
    }
    @Override
    protected ArrayList<OptionData> createSlashOptions() {
        var things = new ArrayList<OptionData>();
        things.add(new OptionData(OptionType.STRING, "speech", "Speech to synthesise").setRequired(true));

        return things;
    }
    @Override
    public boolean requiresVoiceConnection() {
        return false;
    }
    @Override
    public void handleSlashCommand(SlashCommandInteractionEvent e) {
        e.deferReply().complete();
        var speech = e.getOption("speech").getAsString();
        TtsThread.submitJob(new TtsJob(speech, bytes -> {
           e.getHook().editOriginal(bytes, "generated.wav").queue();
        }));
    }
    
}
