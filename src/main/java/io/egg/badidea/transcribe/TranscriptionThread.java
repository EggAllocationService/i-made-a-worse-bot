package io.egg.badidea.transcribe;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;

import org.jitsi.webrtcvadwrapper.WebRTCVad;
import org.vosk.Model;
import org.vosk.Recognizer;

import io.egg.badidea.Main;
import io.egg.badidea.commands.CommandHandler;
import io.egg.badidea.micHandler.DefaultRecieveHandler;
import io.egg.badidea.mixing.AudioMixer;
import io.egg.badidea.protocol.MicInputStream;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.managers.AudioManager;

public class TranscriptionThread extends Thread {
    public Gson g;
    public Model m;
    public User transcribeFromUser = null;
    public int transcriptionTime = 0;
    public int silentTime = 0;
    public WebRTCVad vad;
    public short[] recordBuffer = new short[16000 * 10];
    public int index = 0;
    public static byte[] ackNoise;
    public static byte[] startNoise;
    public static byte[] successNoise;
    public static byte[] failNoise;
    public Recognizer voskRecognizer;
    public static final int[] SILENCE = new int[320];

    public TranscriptionThread() throws IOException {
        super("Transcription Thread");
        vad = new WebRTCVad(16000, 2);
        System.out.println("WebRTCVad init, ready at 16000Hz with sample size of 320");
        Path p = Paths.get("ack_be.pcm");
        ackNoise = Files.readAllBytes(p);
        g = new Gson();
        m = new Model(Main.config.speechNetwork);
        voskRecognizer = new Recognizer(m, 16000);
        p = Paths.get("start_be.pcm");
        startNoise = Files.readAllBytes(p);

        p = Paths.get("success_be.pcm");
        successNoise = Files.readAllBytes(p);

        p = Paths.get("fail_be.pcm");
        failNoise = Files.readAllBytes(p);
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(18);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
            if (transcribeFromUser != null) {
                if (transcriptionTime == 0) {
                    AudioMixer.notificationSink.push(startNoise);
                }
                if (!DefaultRecieveHandler.audioStreams.containsKey(transcribeFromUser)
                        || DefaultRecieveHandler.audioStreams.get(transcribeFromUser) == null) {
                    transcribeFromUser = null;
                    continue;
                }
                if (transcriptionTime > Main.config.listenTimeoutMs) {
                    System.out.println("Transcription timed out for user " + transcribeFromUser.getAsTag());
                    transcriptionFinished();
                    continue;
                }
                MicInputStream stream = DefaultRecieveHandler.audioStreams.get(transcribeFromUser);
                short[] s = new short[320];
                if (!stream.canProvideLenBytes(320)) {

                    silentTime += 20;
                    vad.isSpeech(SILENCE);
                } else {
                    stream.read(s);
                    System.arraycopy(s, 0, recordBuffer, index, 320);// 320 - 20ms at 16khz mono
                    index += 320;
                    // voskRecognizer.acceptWaveForm(s, s.length);
                    int[] forVad = shortToInt(s);
                    boolean isVoice = vad.isSpeech(forVad);

                    if (!isVoice) {
                        silentTime += 20;
                    } else {
                        silentTime = 0;
                    }
                }

                if (silentTime > 1300) {
                    transcriptionFinished();
                }
                transcriptionTime += 20;

            }

        }
    }

    public int[] shortToInt(short[] in) {
        int[] out = new int[in.length];
        for (int i = 0; i < in.length; i++) {
            out[i] = Short.reverseBytes(in[i]);
        }
        return out;
    }

    public void beginTranscription(User u) {
        transcribeFromUser = u;
        index = 0;
        transcriptionTime = 0;
        silentTime = 0;
    }

    public void transcriptionFinished() {
        System.out.println("Transcription finished, beginning speech recognition");
        transcriptionTime = 0;
        silentTime = 0;
        var u = transcribeFromUser;
        transcribeFromUser = null;

        AudioMixer.notificationSink.push(TranscriptionThread.ackNoise);
        short[] data = new short[index];
        System.arraycopy(recordBuffer, 0, data, 0, index);
        index = 0;
        System.out.println(voskRecognizer.acceptWaveForm(data, data.length));
        String s = voskRecognizer.getFinalResult();
        Bullshit b = g.fromJson(s, Bullshit.class);
        voskRecognizer.reset();
        Member resolved = null;
        List<User> audioUsers;
        for (AudioManager a : Main.bot.getAudioManagers()) {
            if (a.getConnectedChannel() == null) continue;
            audioUsers = a.getConnectedChannel().getMembers().stream()
                    .map(c -> c.getUser())
                    .collect(Collectors.toList());
            if (audioUsers.contains(u)) {
                resolved = a.getGuild().getMember(u);
                break;
            }

        }
        ;
        if (resolved == null) {
            System.out.println("WARN: Could not find guild for member " + u.getAsTag());
        }
        System.out.println(s);
        AudioMixer.stopListening();
        CommandHandler.handleCommand(b.text, resolved);
    }

    public class Bullshit {
        public String text;
    }

}
