package io.egg.badidea;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

import com.google.gson.Gson;

public class JoinCodesFile {
    public HashMap<String, String> codes = new HashMap<>();
    public JoinCodesFile() {
        codes.put("place", "holder");
    }
    public void save() {
        var g = new Gson();
        var txt = g.toJson(this);
        
        try {
            Files.writeString(Path.of("codes.json"), txt);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
