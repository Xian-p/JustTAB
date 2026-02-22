package com.example.justtab;

import com.example.justtab.managers.BoardManager;
import com.example.justtab.managers.ConfigManager;
import com.example.justtab.managers.TabManager;
import com.example.justtab.utils.ColorUtil;
import com.example.justtab.utils.LuckPermsHook;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class JustTAB extends JavaPlugin {

    private static JustTAB instance;
    private ConfigManager configManager;
    private TabManager tabManager;
    private BoardManager boardManager;
    private LuckPermsHook luckPermsHook;

    @Override
    public void onEnable() {
        instance = this;

        // 1. Config
        this.configManager = new ConfigManager(this);

        // 2. Hook LuckPerms safely
        if (Bukkit.getPluginManager().getPlugin("LuckPerms") != null) {
            this.luckPermsHook = new LuckPermsHook(this);
        } else {
            getLogger().warning("LuckPerms not found! Prefixes/Suffixes disabled.");
        }

        // 3. Start Managers
        this.tabManager = new TabManager(this);
        this.boardManager = new BoardManager(this);
        
        startTasks();

        getLogger().info("JustTAB enabled! Supporting Legacy (&), Hex (&#), and MiniMessage.");
    }

    @Override
    public void onDisable() {
        stopTasks();
        getLogger().info("JustTAB disabled.");
    }

    public void startTasks() {
        if (tabManager != null) tabManager.startTask();
        if (boardManager != null) boardManager.startTask();
    }

    public void stopTasks() {
        if (tabManager != null) tabManager.stopTask();
        if (boardManager != null) boardManager.stopTask();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("justtab")) {
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("justtab.reload")) {
                    // FIX: Use convert() then deserialize
                    String msg = ColorUtil.convert("<red>No permission.");
                    sender.sendMessage(MiniMessage.miniMessage().deserialize(msg));
                    return true;
                }
                configManager.reload();
                stopTasks();
                startTasks();
                
                // Force immediate update
                if (tabManager != null) tabManager.updateAll();
                if (boardManager != null) boardManager.updateAll();

                // FIX: Use convert() then deserialize
                String rawMsg = configManager.getString("messages.reload");
                String convertedMsg = ColorUtil.convert(rawMsg);
                sender.sendMessage(MiniMessage.miniMessage().deserialize(convertedMsg));
                return true;
            }
        }
        return false;
    }

    public static JustTAB getInstance() { return instance; }
    public ConfigManager getPluginConfig() { return configManager; }
    public LuckPermsHook getLuckPermsHook() { return luckPermsHook; }
}
