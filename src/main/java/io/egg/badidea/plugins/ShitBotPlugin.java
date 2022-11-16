package io.egg.badidea.plugins;

public class ShitBotPlugin {
    public String name;
    Object config;
    public ShitBotPlugin(String pluginName) {
        name = pluginName;
    }



    /**
     * Called when the plugin is about to be activated. All command registration should be done in this function.
     * This function may be called from any thread, do not assume any operation will be safe.
     * The following functions are guaranteed to be safe:
     * registerCommand(BaseCommand);
     * registerConfig(Class<?>);
     */
    public void activate() {

    }

    /**
     * Called when the bot is shutting down.
     */
    public void deactivate() {

    }

    public void saveConfig() {
        
    }

    public void register() {
        activate();
        
    }

    public <T> T getConfig(Class<?> asClass) {
        return (T) config;
    }
}
