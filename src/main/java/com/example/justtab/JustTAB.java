package com.example.justtab;

import com.example.justtab.utils.ConfigManager;
import com.example.justtab.utils.LuckPermsHook;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class JustTAB extends JavaPlugin {

    private static JustTAB instance;
    private ConfigManager configManager;
    private TabManager tabManager;
    private LuckPermsHook luckPermsHook;

    @Override
    public void onEnable() {
        instance = this;

        // 1. Load Config
        this.configManager = new ConfigManager(this);

        // 2. Hook into Permissions
        this.luckPermsHook = new LuckPermsHook(this);

        // 3. Start Tab Logic
        this.tabManager = new TabManager(this);
        this.tabManager.startTask();

        getLogger().info("JustTAB has been enabled!");
    }

    @Override
    public void onDisable() {
        if (this.tabManager != null) {
            this.tabManager.stopTask();
        }
        getLogger().info("JustTAB has been disabled.");
    }

    // Simple command handling for reload
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("justtab")) {
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("justtab.reload")) {
                    sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>You do not have permission."));
                    return true;
                }
                configManager.reload();
                tabManager.updateAll();
                sender.sendMessage(MiniMessage.miniMessage().deserialize(configManager.getString("messages.reload")));
                return true;
            }
        }
        return false;
    }

    // Getters for other classes to access
    public static JustTAB getInstance() { return instance; }
    public ConfigManager getPluginConfig() { return configManager; }
    public LuckPermsHook getLuckPermsHook() { return luckPermsHook; }
}