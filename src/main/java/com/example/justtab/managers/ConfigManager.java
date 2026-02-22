package com.example.justtab.managers;

import com.example.justtab.JustTAB;
import org.bukkit.configuration.file.FileConfiguration;
import java.util.List;

public class ConfigManager {
    private final JustTAB plugin;
    private FileConfiguration config;

    public ConfigManager(JustTAB plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    public String getString(String path) {
        return config.getString(path, "");
    }
    
    public boolean getBoolean(String path) {
        return config.getBoolean(path, true);
    }

    public List<String> getStringList(String path) {
        return config.getStringList(path);
    }

    public int getInt(String path) {
        return config.getInt(path, 20);
    }
}
