package io.egg.badidea;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;

import javax.security.auth.login.LoginException;

import com.google.gson.Gson;
import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeHttpContextFilter;

import ai.picovoice.porcupine.PorcupineException;
import io.egg.badidea.commands.*;
import io.egg.badidea.micHandler.MicThread;
import io.egg.badidea.mixing.AudioMixer;
import io.egg.badidea.mixing.DynamicAudioMixer;
import io.egg.badidea.mixing.WrappedAudioPlayerStream;
import io.egg.badidea.protocol.TrackScheduler;
import io.egg.badidea.speakerHandler.SpeakerThread;
import io.egg.badidea.transcribe.TranscriptionThread;
import io.egg.badidea.tts.TtsThread;
import io.egg.badidea.wakeWordHandler.WakeWordThread;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class Main {
    static SpeakerThread speakerThread;
    static MicThread micThread;
    public static JDA bot;
    public static TranscriptionThread transcriptionThread;
    public static AudioPlayerManager audioManager;
    public static TtsThread ttsThread;
    public static Gson gson;
    public static ConfigFile config;
    public static JoinCodesFile codes;
    public static void main(String[] args) throws LoginException, PorcupineException, IOException {

        gson = new Gson();
        String p = Files.readString(Path.of("config"));
        config = gson.fromJson(p, ConfigFile.class);
        new WakeWordThread().start();
        ttsThread = new TtsThread();
        ttsThread.start();
        transcriptionThread = new TranscriptionThread();
        audioManager = new DefaultAudioPlayerManager();
        if (!(new File("codes.json")).exists()) {
            var s_tmp = gson.toJson(new JoinCodesFile());
            Files.writeString(Path.of("codes.json"), s_tmp);
        }
        p = Files.readString(Path.of("codes.json"));
        codes = gson.fromJson(p, JoinCodesFile.class);

        audioManager.getConfiguration().setOutputFormat(StandardAudioDataFormats.DISCORD_PCM_S16_BE);
        AudioSourceManagers.registerRemoteSources(audioManager);
        AudioMixer.audioPlayer = audioManager.createPlayer();
        AudioMixer.trackScheduler = new TrackScheduler(AudioMixer.audioPlayer);
        AudioMixer.audioPlayer.addListener(AudioMixer.trackScheduler);
        YoutubeHttpContextFilter.setPAPISID(config.PAPISID);
        YoutubeHttpContextFilter.setPSID(config.PSID);
        AudioMixer.audioPlayer.setVolume(config.defaultAudioVolume);
        AudioMixer.musicStream = new WrappedAudioPlayerStream(AudioMixer.audioPlayer);
        DynamicAudioMixer.channels.put("music", AudioMixer.musicStream);
        transcriptionThread.start();

        CommandManager.registerCommand(new PlayCommand());
        CommandManager.registerCommand(new StopCommand());
        CommandManager.registerCommand(new SkipCommand());
        CommandManager.registerCommand(new TranscribeCommand());
        CommandManager.registerCommand(new SpeechTestCommand());
        CommandManager.registerCommand(new PlayingCommand());
        CommandManager.registerCommand(new SynthCommand());
        CommandManager.registerCommand(new VolumeCommand());

        bot = JDABuilder.createDefault(config.token)
                .addEventListeners(new MainEventHandler())
                .setEnabledIntents(EnumSet.allOf(GatewayIntent.class))
                .enableCache(CacheFlag.VOICE_STATE)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .setActivity(null)
                .build();
        
    }

    public static byte[] stripWavHeader(byte[] wav) {
        byte[] stripped = new byte[wav.length - 44];
        System.arraycopy(wav, 44, stripped, 0, wav.length - 44);
        return stripped;
    }
}
