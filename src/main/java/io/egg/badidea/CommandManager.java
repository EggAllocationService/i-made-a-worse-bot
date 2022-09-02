package io.egg.badidea;

import java.util.ArrayList;
import java.util.HashMap;

import io.egg.badidea.mixing.AudioMixer;
import io.egg.badidea.transcribe.TranscriptionThread;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class CommandManager {
    static HashMap<String, BaseCommand> commands = new HashMap<>(); 
    static boolean hasInit = false;

    public static void registerCommand(BaseCommand command) {
        if (commands.containsKey(command.getName())) {
            System.out.println("WARN: Tried to register command " + command.getName() + " twice!");
            return;
        }
        if (hasInit) {
            System.out.println("WARN: Tried to register command after slash command init!");
            return;
        }
        commands.put(command.getName(), command);
    }


    public static void build() {
        if (hasInit) {
            System.out.println("WARN: Trying to build commands twice!");
        }
        var guild = Main.bot.getGuildById(Main.config.guildId);
       
        var currentCommands = guild.retrieveCommands().complete();
        HashMap<String, String> currentNameIdMap = new HashMap<>();
        for (Command c : currentCommands) {
            currentNameIdMap.put(c.getName(), c.getId());
        }
        ArrayList<SlashCommandData> localCommands = new ArrayList<>();
        for (BaseCommand b : commands.values()) {
            var name = b.getName();
            var data = b.createSlashCommand();
            if (data == null && currentNameIdMap.containsKey(name)) {
                // delete command that no loger has a slash command component
                guild.deleteCommandById(currentNameIdMap.get(name));
                continue;
            }
            if (data == null) continue;
           localCommands.add(data);
        }
        guild.updateCommands().addCommands(localCommands).complete();
        System.out.println("Successfully synced " + localCommands.size() + " commands to Discord");
    }

    public static void handleSlashCommand(SlashCommandInteractionEvent event) {
        var name = event.getName();
        if (!commands.containsKey(name)) {
            event.reply(":x: This command does not have an attached handler, this is a serious bug!").setEphemeral(true).queue();
            return;
        }
        var local = commands.get(name);
        if (local.requiresVoiceConnection() && (
            !event.getGuild().getAudioManager().isConnected() ||
            !event.getMember().getVoiceState().inAudioChannel() ||
            !event.getMember().getVoiceState().getChannel().equals(event.getGuild().getAudioManager().getConnectedChannel())
        )) {
            // not in same vc
            event.reply(":x: You must be in the same voice channel as the bot to use this command!").setEphemeral(true).queue();
            return;
        }
        local.handleSlashCommand(event);
    }


    public static void handleVoiceCommand(String text, Member from) {
        BaseCommand found = null;
        for (var c : commands.values()) {
            if (c.shouldHandleVoiceCommand(text, from)) {
                found = c;
                break;
            }
        }
        if (found == null) {
            AudioMixer.notificationSink.push(TranscriptionThread.failNoise);
        } else {
            System.out.println("INFO: Matched voice command " + found.getName() + " for user " + from.getNickname());
            found.handleVoiceCommand(text, from);
        }
    }

}
