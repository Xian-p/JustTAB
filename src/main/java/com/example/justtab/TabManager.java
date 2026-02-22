package com.example.justtab;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

public class TabManager {

    private final JustTAB plugin;
    private final MiniMessage mm;
    private BukkitTask task;

    public TabManager(JustTAB plugin) {
        this.plugin = plugin;
        this.mm = MiniMessage.miniMessage();
    }

    public void startTask() {
        int interval = plugin.getPluginConfig().getInt("update-interval");
        
        // Ensure interval isn't too low to prevent lag
        if (interval < 1) interval = 20;

        task = Bukkit.getScheduler().runTaskTimer(plugin, this::updateAll, interval, interval);
    }

    public void stopTask() {
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
    }

    public void updateAll() {
        // Iterate through all players and update their tab list
        for (Player player : Bukkit.getOnlinePlayers()) {
            updatePlayer(player);
        }
    }

    private void updatePlayer(Player player) {
        // 1. Get raw strings from config
        List<String> headerLines = plugin.getPluginConfig().getStringList("header");
        List<String> footerLines = plugin.getPluginConfig().getStringList("footer");
        String formatRaw = plugin.getPluginConfig().getString("player-format");

        // 2. Parse Components using MiniMessage
        Component header = parseList(headerLines, player);
        Component footer = parseList(footerLines, player);
        Component nameFormat = parseLine(formatRaw, player);

        // 3. Send to player
        player.sendPlayerListHeaderAndFooter(header, footer);
        player.playerListName(nameFormat);
    }

    /**
     * Joins a list of strings with newlines and parses them.
     */
    private Component parseList(List<String> lines, Player player) {
        if (lines.isEmpty()) return Component.empty();
        // Join lines with the MiniMessage newline tag
        String joined = String.join("<newline>", lines);
        return parseLine(joined, player);
    }

    /**
     * Parses a string into a Component, replacing placeholders.
     */
    private Component parseLine(String text, Player player) {
        // Fetch data
        String prefix = plugin.getLuckPermsHook().getPrefix(player);
        String suffix = plugin.getLuckPermsHook().getSuffix(player);
        
        // Format TPS
        double tpsRaw = Bukkit.getTPS()[0];
        String tps = String.format("%.2f", tpsRaw);

        // Create TagResolver for high-performance placeholder replacement
        TagResolver placeholders = TagResolver.resolver(
            Placeholder.parsed("player", player.getName()),
            Placeholder.parsed("ping", String.valueOf(player.getPing())),
            Placeholder.parsed("prefix", prefix),
            Placeholder.parsed("suffix", suffix),
            Placeholder.parsed("tps", tps)
        );

        // Deserialize string to component with placeholders applied
        return mm.deserialize(text, placeholders);
    }
}