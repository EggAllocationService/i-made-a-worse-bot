package io.egg.badidea.mixing;

import java.util.HashMap;
import java.util.ArrayList;

public class DynamicAudioMixer {
    public static HashMap<String, CabotAudioStream> channels = new HashMap<>();

    static public short[] mixAudio() {
        var audioBuffer = new short[1920];
        var channelsToMerge = new ArrayList<short[]>();
        short channelCount = 0;
        var priorityChannels = false;
        for (var c : channels.values()) {
            if (c.canProvideFrame() && !c.isPriority()) {
               channelsToMerge.add( c.provide());
               channelCount++;
            } else if (c.isPriority() && c.isOpen()) {
                priorityChannels = true;
            }
        }
        if (channelsToMerge.size() == 1) {
            return channelsToMerge.get(0);
        }
        
        for (var c : channelsToMerge) {
            div(c, channelCount);
            add(audioBuffer, c);
        }
        if (priorityChannels) {
           div(audioBuffer, (short) 10);
            for (var c : channels.values()) {
                if (c.canProvideFrame() && c.isPriority()) {
                    var vec = c.provide();
                    add(audioBuffer, vec);
                }
            }
        }
        return audioBuffer;
    }
    static boolean canProvideFrame() {
        return channels.values().stream()
                .map(CabotAudioStream::canProvideFrame)
                .reduce(false, (a, b) -> {return a || b;});
    }
    static void div(short[] a, short b) {
        for (int i = 0; i < a.length; i++) {
            a[i] = (short) (a[i] / b);
        }
    }
    static void add(short[] a, short[] b) {
        for (int i = 0; i < a.length; i++) {
            a[i] = (short) (a[i] + b[i]);
        }
    }
}