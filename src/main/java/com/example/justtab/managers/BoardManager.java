package com.example.justtab.managers;

import com.example.justtab.JustTAB;
import com.example.justtab.utils.ColorUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class BoardManager {

    private final JustTAB plugin;
    private final MiniMessage mm;
    private BukkitTask task;

    public BoardManager(JustTAB plugin) {
        this.plugin = plugin;
        this.mm = MiniMessage.miniMessage();
    }

    public void startTask() {
        if (!plugin.getPluginConfig().getBoolean("scoreboard.enabled")) return;
        int interval = plugin.getPluginConfig().getInt("update-interval");
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::updateAll, interval, interval);
    }

    public void stopTask() {
        if (task != null) task.cancel();
        // Reset scoreboards for all players
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
    }

    public void updateAll() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            updatePlayer(p);
        }
    }

    private void updatePlayer(Player player) {
        Scoreboard board = player.getScoreboard();
        
        // Ensure player has a private scoreboard
        if (board.equals(Bukkit.getScoreboardManager().getMainScoreboard())) {
            board = Bukkit.getScoreboardManager().getNewScoreboard();
            player.setScoreboard(board);
        }

        Objective obj = board.getObjective("JustTAB");
        if (obj == null) {
            obj = board.registerNewObjective("JustTAB", Criteria.DUMMY, Component.empty());
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        }

        // 1. Update Title (Fix: Convert colors first)
        String rawTitle = plugin.getPluginConfig().getString("scoreboard.title");
        String convertedTitle = ColorUtil.convert(rawTitle);
        obj.displayName(mm.deserialize(convertedTitle));

        // 2. Update Lines
        List<String> lines = plugin.getPluginConfig().getStringList("scoreboard.lines");
        int scoreIndex = lines.size();

        for (String line : lines) {
            // Parse line with placeholders and colors
            Component parsed = parseLine(line, player);
            
            // Generate unique invisible entry key
            String entry = ChatColor.values()[scoreIndex % 15].toString() + ChatColor.RESET;

            Team team = board.getTeam("line_" + scoreIndex);
            if (team == null) {
                team = board.registerNewTeam("line_" + scoreIndex);
                team.addEntry(entry);
            }

            // Update the text via Team Prefix (Standard flicker-free method)
            team.prefix(parsed);

            // Set score
            obj.getScore(entry).setScore(scoreIndex);
            scoreIndex--;
        }

        // Cleanup extra lines
        for (String entry : board.getEntries()) {
            if (obj.getScore(entry).getScore() > lines.size()) {
                board.resetScores(entry);
            }
        }
    }

    private Component parseLine(String text, Player player) {
        // Fetch LP Data
        String prefixRaw = (plugin.getLuckPermsHook() != null) ? plugin.getLuckPermsHook().getPrefix(player) : "";
        String suffixRaw = (plugin.getLuckPermsHook() != null) ? plugin.getLuckPermsHook().getSuffix(player) : "";

        // Convert LP Legacy strings to Components
        Component prefixComp = LegacyComponentSerializer.legacyAmpersand().deserialize(prefixRaw);
        Component suffixComp = LegacyComponentSerializer.legacyAmpersand().deserialize(suffixRaw);

        // Stats
        String tps = String.format("%.2f", Bukkit.getTPS()[0]);
        String date = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
        String online = String.valueOf(Bukkit.getOnlinePlayers().size());

        // Resolvers
        TagResolver placeholders = TagResolver.resolver(
            Placeholder.parsed("player", player.getName()),
            Placeholder.parsed("ping", String.valueOf(player.getPing())),
            Placeholder.parsed("tps", tps),
            Placeholder.parsed("date", date),
            Placeholder.parsed("online", online),
            Placeholder.component("prefix", prefixComp),
            Placeholder.component("suffix", suffixComp)
        );

        // FIX: Convert Config String (colors) to MiniMessage format first
        String convertedText = ColorUtil.convert(text);

        // Deserialize final string
        return mm.deserialize(convertedText, placeholders);
    }
}
