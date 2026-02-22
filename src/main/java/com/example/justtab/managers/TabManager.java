package com.example.justtab.managers;

import com.example.justtab.JustTAB;
import com.example.justtab.utils.ColorUtil;
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
    private BukkitTask task;

    public TabManager(JustTAB plugin) { this.plugin = plugin; }

    public void startTask() {
        int interval = plugin.getPluginConfig().getInt("update-interval");
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::updateAll, interval, interval);
    }

    public void stopTask() {
        if (task != null && !task.isCancelled()) task.cancel();
    }

    public void updateAll() {
        for (Player p : Bukkit.getOnlinePlayers()) updatePlayer(p);
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
        // Join with \n for header/footer
        return parseLine(String.join("\n", lines), player);
    }

    private Component parseLine(String text, Player player) {
        // 1. Get raw LP data
        String prefixRaw = (plugin.getLuckPermsHook() != null) ? plugin.getLuckPermsHook().getPrefix(player) : "";
        String suffixRaw = (plugin.getLuckPermsHook() != null) ? plugin.getLuckPermsHook().getSuffix(player) : "";

        // 2. Convert LP data (often legacy) to Components
        Component prefixComp = ColorUtil.parseLegacy(prefixRaw);
        Component suffixComp = ColorUtil.parseLegacy(suffixRaw);

        // 3. Stats
        String tps = String.format("%.2f", Bukkit.getTPS()[0]);
        String online = String.valueOf(Bukkit.getOnlinePlayers().size());

        // 4. Resolve
        TagResolver placeholders = TagResolver.resolver(
            Placeholder.parsed("player", player.getName()),
            Placeholder.parsed("ping", String.valueOf(player.getPing())),
            Placeholder.parsed("tps", tps),
            Placeholder.parsed("online", online),
            Placeholder.component("prefix", prefixComp),
            Placeholder.component("suffix", suffixComp)
        );

        // 5. Parse Config String (Hybrid support)
        Component configStringParsed = ColorUtil.parse(text);

        // 6. Merge
        return MiniMessage.miniMessage().deserialize(
            MiniMessage.miniMessage().serialize(configStringParsed), 
            placeholders
        );
    }
          }
