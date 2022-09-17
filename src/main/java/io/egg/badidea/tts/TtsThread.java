package io.egg.badidea.tts;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.egg.badidea.Main;

public class TtsThread extends Thread {
    private static List<TtsJob> queue = Collections.synchronizedList(new ArrayList<TtsJob>());
    public HttpClient client;
    public TtsThread() {
        super("Text To Speech thread");
        client = HttpClient.newBuilder()
                        .build();
    }  
    public static void submitJob(TtsJob ttsJob) {
        queue.add(ttsJob);
    }
    @Override
    public void run() {
        while(true) {
            if (queue.size() > 0) {
                // at least one job to do
                // so get first and run job
                TtsJob job = queue.remove(0);
                HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(Main.config.mimicUrl))
                    .POST(BodyPublishers.ofString(job.toGenerate))
                    .build();
                try {
                    var response = client.send(req, BodyHandlers.ofByteArray());
                    job.completionJob.accept(response.body());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                

            }


            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
