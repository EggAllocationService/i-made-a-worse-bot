package io.egg.badidea.commands;



import io.egg.badidea.BaseCommand;
import io.egg.badidea.tts.TtsJob;
import io.egg.badidea.tts.TtsPlayerHandler;
import io.egg.badidea.tts.TtsThread;
import net.dv8tion.jda.api.entities.Member;

public class SpeechTestCommand extends BaseCommand {
        
    public SpeechTestCommand() {
        super("speechtest");
    }

    @Override
    public boolean requiresVoiceConnection() {
        return true;
    }

    @Override
    public boolean shouldHandleVoiceCommand(String in, Member from) {
        return in.startsWith("say something") || in.startsWith("speech test");
    }
    
    @Override
    public void handleVoiceCommand(String in, Member from) {
        TtsThread.submitJob(new TtsJob("my speech subsystem is fully functional, hello " + from.getEffectiveName().toLowerCase(), new TtsPlayerHandler()));
    }    
}
