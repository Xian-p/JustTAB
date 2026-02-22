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
    private BukkitTask task;

    public BoardManager(JustTAB plugin) { this.plugin = plugin; }

    public void startTask() {
        if (!plugin.getPluginConfig().getBoolean("scoreboard.enabled")) return;
        int interval = plugin.getPluginConfig().getInt("update-interval");
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::updateAll, interval, interval);
    }

    public void stopTask() {
        if (task != null) task.cancel();
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
    }

    public void updateAll() {
        for (Player p : Bukkit.getOnlinePlayers()) updatePlayer(p);
    }

    private void updatePlayer(Player player) {
        Scoreboard board = player.getScoreboard();
        if (board.equals(Bukkit.getScoreboardManager().getMainScoreboard())) {
            board = Bukkit.getScoreboardManager().getNewScoreboard();
            player.setScoreboard(board);
        }

        Objective obj = board.getObjective("JustTAB");
        if (obj == null) {
            obj = board.registerNewObjective("JustTAB", Criteria.DUMMY, Component.empty());
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        }

        // Title
        String title = plugin.getPluginConfig().getString("scoreboard.title");
        obj.displayName(ColorUtil.parse(title));

        // Lines
        List<String> lines = plugin.getPluginConfig().getStringList("scoreboard.lines");
        int score = lines.size();

        for (String line : lines) {
            Component parsed = parseLine(line, player);
            
            // Generate invisible unique entry based on score index
            String entry = ChatColor.values()[score % 15].toString() + ChatColor.RESET;

            Team team = board.getTeam("line_" + score);
            if (team == null) team = board.registerNewTeam("line_" + score);

            if (!team.hasEntry(entry)) team.addEntry(entry);

            // Update visible text
            team.prefix(parsed); 

            obj.getScore(entry).setScore(score);
            score--;
        }
        
        // Clean extra lines
        for(String entry : board.getEntries()) {
            if(obj.getScore(entry).getScore() > lines.size()) board.resetScores(entry);
        }
    }

    private Component parseLine(String text, Player player) {
        String prefixRaw = (plugin.getLuckPermsHook() != null) ? plugin.getLuckPermsHook().getPrefix(player) : "";
        String suffixRaw = (plugin.getLuckPermsHook() != null) ? plugin.getLuckPermsHook().getSuffix(player) : "";

        Component prefixComp = ColorUtil.parseLegacy(prefixRaw);
        Component suffixComp = ColorUtil.parseLegacy(suffixRaw);

        String tps = String.format("%.2f", Bukkit.getTPS()[0]);
        String date = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
        String online = String.valueOf(Bukkit.getOnlinePlayers().size());

        TagResolver placeholders = TagResolver.resolver(
            Placeholder.parsed("player", player.getName()),
            Placeholder.parsed("ping", String.valueOf(player.getPing())),
            Placeholder.parsed("tps", tps),
            Placeholder.parsed("date", date),
            Placeholder.parsed("online", online),
            Placeholder.component("prefix", prefixComp),
            Placeholder.component("suffix", suffixComp)
        );

        Component configParsed = ColorUtil.parse(text);
        
        // Merge Config Text with Placeholders
        return MiniMessage.miniMessage().deserialize(
            MiniMessage.miniMessage().serialize(configParsed), 
            placeholders
        );
    }
  }
