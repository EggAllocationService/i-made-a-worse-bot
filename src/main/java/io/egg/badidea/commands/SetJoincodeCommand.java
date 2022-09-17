package io.egg.badidea.commands;

import java.time.Instant;
import java.util.ArrayList;

import io.egg.badidea.BaseCommand;
import io.egg.badidea.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class SetJoincodeCommand extends BaseCommand {

    public SetJoincodeCommand() {
        super("joincode");
    }

    @Override
    protected ArrayList<OptionData> createSlashOptions() {
        var things = new ArrayList<OptionData>();
        things.add(new OptionData(OptionType.STRING, "set", "Your bungie ID").setRequired(false));
        return things;
    }

    @Override
    public boolean requiresVoiceConnection() {
        return false;
    }

    @Override
    public void handleSlashCommand(SlashCommandInteractionEvent e) {
        e.deferReply(true).complete();
        if (e.getOption("set") == null) {
            // just reply with joincode
            var emb = new EmbedBuilder();
            emb.setFooter(e.getUser().getAsTag(), e.getUser().getAvatarUrl());
            emb.addField("Join Code",  Main.codes.codes.get(e.getUser().getId()), false);
            emb.setTimestamp(Instant.now());
            e.getHook().sendMessageEmbeds(emb.build()).complete();
        } else {
            // setting join code
            var newCode = e.getOption("set").getAsString();
            if (newCode.startsWith("/") || !newCode.contains("#")) {
                e.getHook().sendMessage(":x: That is not a valid join code!").setEphemeral(true).complete();
                return;
            }
            Main.codes.codes.put(e.getUser().getId(), newCode);
            Main.codes.save();
            e.getHook().sendMessage(":white_check_mark: Updated your join code").setEphemeral(true).complete();
        }
    }
    
}
