package io.egg.badidea;

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
import io.egg.badidea.micHandler.MicThread;
import io.egg.badidea.mixing.AudioMixer;
import io.egg.badidea.protocol.TrackScheduler;
import io.egg.badidea.speakerHandler.SpeakerThread;
import io.egg.badidea.transcribe.TranscriptionThread;
import io.egg.badidea.wakeWordHandler.WakeWordThread;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class Main {
    static SpeakerThread speakerThread;
    static MicThread micThread;
    public static JDA bot;
    public static TranscriptionThread transcriptionThread;
    public static AudioPlayerManager audioManager;
    public static Gson gson;
    public static ConfigFile config;

    public static void main(String[] args) throws LoginException, PorcupineException, IOException {

        gson = new Gson();
        String p = Files.readString(Path.of("config"));
        config = gson.fromJson(p, ConfigFile.class);
        new WakeWordThread().start();
        transcriptionThread = new TranscriptionThread();
        audioManager = new DefaultAudioPlayerManager();

        audioManager.getConfiguration().setOutputFormat(StandardAudioDataFormats.DISCORD_PCM_S16_BE);
        AudioSourceManagers.registerRemoteSources(audioManager);
        AudioMixer.audioPlayer = audioManager.createPlayer();
        AudioMixer.trackScheduler = new TrackScheduler(AudioMixer.audioPlayer);
        AudioMixer.audioPlayer.addListener(AudioMixer.trackScheduler);
        YoutubeHttpContextFilter.setPAPISID(config.PAPISID);
        YoutubeHttpContextFilter.setPSID(config.PSID);
        AudioMixer.audioPlayer.setVolume(config.defaultAudioVolume);
        transcriptionThread.start();
        
        bot = JDABuilder.createDefault(config.token)
                .addEventListeners(new MainEventHandler())
                .setEnabledIntents(EnumSet.allOf(GatewayIntent.class))
                .enableCache(CacheFlag.VOICE_STATE)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .setActivity(Activity.playing("League of Legends"))
                .build();

    }

    public static byte[] stripWavHeader(byte[] wav) {
        byte[] stripped = new byte[wav.length - 44];
        System.arraycopy(wav, 44, stripped, 0, wav.length - 44);
        return stripped;
    }
}
