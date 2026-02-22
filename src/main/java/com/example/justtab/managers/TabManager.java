package com.example.justtab.managers;

import com.example.justtab.JustTAB;
import com.example.justtab.utils.ColorUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::updateAll, interval, interval);
    }

    public void stopTask() {
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
    }

    public void updateAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updatePlayer(player);
        }
    }

    private void updatePlayer(Player player) {
        List<String> headerLines = plugin.getPluginConfig().getStringList("header");
        List<String> footerLines = plugin.getPluginConfig().getStringList("footer");
        String format = plugin.getPluginConfig().getString("player-format");

        Component header = parseList(headerLines, player);
        Component footer = parseList(footerLines, player);
        Component tabName = parseLine(format, player);

        player.sendPlayerListHeaderAndFooter(header, footer);
        player.playerListName(tabName);
    }

    private Component parseList(List<String> lines, Player player) {
        if (lines.isEmpty()) return Component.empty();
        // Join lines with newline tag
        String joined = String.join("<newline>", lines);
        return parseLine(joined, player);
    }

    private Component parseLine(String text, Player player) {
        // 1. Fetch LuckPerms Data (Safely)
        String prefixRaw = (plugin.getLuckPermsHook() != null) ? plugin.getLuckPermsHook().getPrefix(player) : "";
        String suffixRaw = (plugin.getLuckPermsHook() != null) ? plugin.getLuckPermsHook().getSuffix(player) : "";

        // 2. Convert LuckPerms Legacy Strings to Components
        // This preserves the colors inside the prefix itself
        Component prefixComp = LegacyComponentSerializer.legacyAmpersand().deserialize(prefixRaw);
        Component suffixComp = LegacyComponentSerializer.legacyAmpersand().deserialize(suffixRaw);

        // 3. Prepare Stats
        String tps = String.format("%.2f", Bukkit.getTPS()[0]);
        String online = String.valueOf(Bukkit.getOnlinePlayers().size());

        // 4. Create Resolver for Placeholders
        TagResolver placeholders = TagResolver.resolver(
            Placeholder.parsed("player", player.getName()),
            Placeholder.parsed("ping", String.valueOf(player.getPing())),
            Placeholder.parsed("tps", tps),
            Placeholder.parsed("online", online),
            Placeholder.component("prefix", prefixComp),
            Placeholder.component("suffix", suffixComp)
        );

        // 5. Convert Config String to MiniMessage Format
        // "&aPlayer: <player>" becomes "<green>Player: <player>"
        String convertedText = ColorUtil.convert(text);

        // 6. Final Deserialize (Combines Colors + Placeholders)
        return mm.deserialize(convertedText, placeholders);
    }
}
