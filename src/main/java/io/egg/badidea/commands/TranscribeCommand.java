package io.egg.badidea.commands;


import java.time.Instant;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import io.egg.badidea.BaseCommand;
import io.egg.badidea.mixing.AudioMixer;
import io.egg.badidea.transcribe.TranscriptionThread;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;

public class TranscribeCommand extends BaseCommand {

    
    public TranscribeCommand() {
        super("transcribe");
    }

    @Override
    public boolean requiresVoiceConnection() {
        return true;
    }

    @Override
    public boolean shouldHandleVoiceCommand(String in, Member from) {
        return in.startsWith("transcribe");
    }
    
    @Override
    public void handleVoiceCommand(String in, Member from) {
        var text = Arrays.asList(in.split(" ")).stream().skip(1).collect(Collectors.joining(" "));

        var emb = new EmbedBuilder();
        emb.setFooter(from.getUser().getAsTag(), from.getAvatarUrl());
        emb.addField("Transcription Result", "\"" + text + "\"", false);
        emb.setTimestamp(Instant.now());
        from.getUser().openPrivateChannel().complete().sendMessageEmbeds(emb.build()).complete();

        AudioMixer.notificationSink.push(TranscriptionThread.successNoise);
    }
    
}
