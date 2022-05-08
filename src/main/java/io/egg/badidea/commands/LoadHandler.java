package io.egg.badidea.commands;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import io.egg.badidea.mixing.AudioMixer;
import io.egg.badidea.transcribe.TranscriptionThread;

public class LoadHandler implements AudioLoadResultHandler {

    @Override
    public void trackLoaded(AudioTrack track) {
        AudioMixer.trackScheduler.queue(track);
        System.out.println("Queued " + track.getInfo().title);
        AudioMixer.notificationSink.push(TranscriptionThread.successNoise);
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
        AudioTrack firstTrack = playlist.getSelectedTrack();

        if (firstTrack == null) {
          firstTrack = playlist.getTracks().get(0);
        }

        AudioMixer.trackScheduler.queue(firstTrack);
        System.out.println("Queued " + firstTrack.getInfo().title);
        AudioMixer.notificationSink.push(TranscriptionThread.successNoise);
        
    }

    @Override
    public void noMatches() {
        
        System.out.println("no matches");
    }

    @Override
    public void loadFailed(FriendlyException exception) {
        exception.printStackTrace();
        
    }
    
}
