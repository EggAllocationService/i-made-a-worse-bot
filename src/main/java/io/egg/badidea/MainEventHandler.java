package io.egg.badidea;

import io.egg.badidea.micHandler.DefaultRecieveHandler;
import io.egg.badidea.speakerHandler.SpeakerSendHandler;
import io.egg.badidea.wakeWordHandler.WakeWordThread;
import net.dv8tion.jda.api.audio.hooks.ConnectionStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;

public class MainEventHandler extends ListenerAdapter {

    @Override
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent e) {
        if (e.getMember().getUser().isBot()) return;
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
                    Main.bot.getPresence().setActivity(Activity.playing("League of Legends"));
                    a.closeAudioConnection();
                }
            }
        }
    }
    
    @Override
    public void onReady(ReadyEvent e) {
        System.out.println("Bot is ready!");
    }
}
