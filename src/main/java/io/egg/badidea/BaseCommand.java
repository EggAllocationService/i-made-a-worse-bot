package io.egg.badidea;

import java.util.ArrayList;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public abstract class BaseCommand {
    String name;

    public BaseCommand(String name) {
        this.name = name;
    }

    public abstract boolean requiresVoiceConnection();
    public String getName() {
        return name;
    }
    protected String getDescription() {
        return "No description provided.";
    }

    public boolean shouldHandleVoiceCommand(String text, Member from) {
        return false;
    }

    protected ArrayList<OptionData> createSlashOptions() {
        return null;
    }

    public SlashCommandData createSlashCommand() {
        var opts = createSlashOptions();
        if (opts == null) return null;
        var c = Commands.slash(name, getDescription());
        c.addOptions(opts);
        return c;
    }

    public void handleVoiceCommand(String text, Member from) {
        // to implement in subclasses
    }

    public void handleSlashCommand(SlashCommandInteractionEvent event) {
        // to implement in subclasses
    }

}
