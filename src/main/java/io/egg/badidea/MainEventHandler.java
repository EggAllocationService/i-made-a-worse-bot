package io.egg.badidea;

import io.egg.badidea.micHandler.DefaultRecieveHandler;
import io.egg.badidea.mixing.AudioMixer;
import io.egg.badidea.speakerHandler.SpeakerSendHandler;
import io.egg.badidea.wakeWordHandler.WakeWordThread;
import net.dv8tion.jda.api.audio.hooks.ConnectionStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Activity.ActivityType;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.user.UserActivityEndEvent;
import net.dv8tion.jda.api.events.user.UserActivityStartEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;

public class MainEventHandler extends ListenerAdapter {

    @Override
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent e) {
        if (e.getMember().getUser().isBot()) return;
        if (!e.getGuild().getId().equals(Main.config.guildId)) return;
        if (e.getChannelJoined() != null) {
            System.out.println("User " + e.getMember().getEffectiveName() +" joined channel: " + e.getChannelJoined().getName());
            AudioManager a = e.getGuild().getAudioManager();
            if (e.getGuild().getAudioManager().getConnectionStatus() == ConnectionStatus.CONNECTED) return;
            if (a.getSendingHandler() == null) a.setSendingHandler(new SpeakerSendHandler());
            if (a.getReceivingHandler() == null) a.setReceivingHandler(new DefaultRecieveHandler());
            a.openAudioConnection(e.getChannelJoined());
            Main.bot.getPresence().setActivity(Activity.listening(e.getChannelJoined().getName()));
        } 
        if (e.getChannelLeft() != null) {
            System.out.println("User " + e.getMember().getEffectiveName() +" left channel: " + e.getChannelLeft().getName());
            WakeWordThread.userDisconnect(e.getMember().getUser());
            AudioManager a = e.getGuild().getAudioManager();
            if (a.isConnected() && a.getConnectedChannel() == e.getChannelLeft()) {
                if (e.getChannelLeft().getMembers().size() == 1) {
                    Main.bot.getPresence().setActivity(null);
                    if (AudioMixer.audioPlayer.getPlayingTrack() != null) {
                        AudioMixer.trackScheduler.stop();
                    }
                    a.closeAudioConnection();
                }
            }
        }
    }
    
    @Override
    public void onReady(ReadyEvent e) {
        System.out.println("Bot is ready!");
        CommandManager.build();
        new Thread(MainEventHandler::recalculatePlayingDestinyRoles).start();
    }
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent e) {
        CommandManager.handleSlashCommand(e);
    }
    @Override

    public void onUserActivityStart(UserActivityStartEvent event) {
        if (!event.getGuild().getId().equals("840384900347854888")) return;
        if (event.getMember().getRoles().size() == 0) return;
        if (event.getNewActivity().getType() == ActivityType.PLAYING && event.getNewActivity().getName().equalsIgnoreCase("Destiny 2")) {
            // player started playing destiny 2
            event.getGuild().addRoleToMember(event.getMember(), event.getGuild().getRoleById("1076974452484620298")).queue();
        }
    }  

    @Override
    public void onUserActivityEnd(UserActivityEndEvent event) {
        if (!event.getGuild().getId().equals("840384900347854888")) return;
        if (event.getMember().getRoles().size() == 0) return;
        if (event.getOldActivity().getType() == ActivityType.PLAYING && event.getOldActivity().getName().equalsIgnoreCase("Destiny 2")) {
            // player stopped playing destiny 2
            event.getGuild().removeRoleFromMember(event.getMember(), event.getGuild().getRoleById("1076974452484620298")).queue();
        }
    }
    public static void recalculatePlayingDestinyRoles() {
        var g = Main.bot.getGuildById("840384900347854888");
        var members = g.findMembers(item -> true).get();
        for (var m : members) {
            if (m.getRoles().size() == 0) continue;
            var hasRole = m.getRoles().stream().map(r -> r.getId()).anyMatch(s -> s.matches("1076974452484620298"));
            var isPlayingDestiny = m.getActivities().stream().filter(a -> a.getType() == ActivityType.PLAYING).map(a -> a.getName()).anyMatch(name -> name.equalsIgnoreCase("Destiny 2"));
            if (hasRole) { // checks if the person has the playing destiny role
                if (!isPlayingDestiny) {
                    // is playing destiny
                    m.getGuild().removeRoleFromMember(m, m.getGuild().getRoleById("1076974452484620298")).queue();
                    System.out.println("Removed playing role from " + m.getEffectiveName());
                }
            } else {
                if (isPlayingDestiny) {
                    m.getGuild().addRoleToMember(m, m.getGuild().getRoleById("1076974452484620298")).queue();
                    System.out.println("Added playing role to " + m.getEffectiveName());
                }
            }
        }
    }
}
