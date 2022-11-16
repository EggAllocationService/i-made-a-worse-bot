package io.egg.badidea.plugins;

import java.net.URLClassLoader;
import java.util.HashMap;

public class PluginManager {
    public static HashMap<String, ShitBotPlugin> plugins = new HashMap<>();
    public static HashMap<String, URLClassLoader> pluginLoaders = new HashMap<>();

    public static ShitBotPlugin pluginForClass(Class<?> target) {
        for (var s : pluginLoaders.keySet()) {
            var u = pluginLoaders.get(s);
            if (u == target.getClassLoader()) {
                return plugins.get(s);
            }
        }
        return null;
    }
    public static ShitBotPlugin pluginForObject(Object target) {
        return pluginForClass(target.getClass());
    }

}
