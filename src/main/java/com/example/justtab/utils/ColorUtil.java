package com.example.justtab.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtil {
    
    // Pattern to find &#123456
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    /**
     * Parses a string containing MiniMessage, Legacy (&), or Hex (&#) colors.
     */
    public static Component parse(String text) {
        if (text == null) return Component.empty();

        // 1. Convert &#123456 to <#123456> (MiniMessage format)
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "<#" + matcher.group(1) + ">");
        }
        matcher.appendTail(sb);
        text = sb.toString();

        // 2. Convert Legacy &a to <green> (MiniMessage format)
        // This is a simple replacement to ensure MiniMessage handles everything
        text = text
                .replace("&0", "<black>")
                .replace("&1", "<dark_blue>")
                .replace("&2", "<dark_green>")
                .replace("&3", "<dark_aqua>")
                .replace("&4", "<dark_red>")
                .replace("&5", "<dark_purple>")
                .replace("&6", "<gold>")
                .replace("&7", "<gray>")
                .replace("&8", "<dark_gray>")
                .replace("&9", "<blue>")
                .replace("&a", "<green>")
                .replace("&b", "<aqua>")
                .replace("&c", "<red>")
                .replace("&d", "<light_purple>")
                .replace("&e", "<yellow>")
                .replace("&f", "<white>")
                .replace("&k", "<obfuscated>")
                .replace("&l", "<bold>")
                .replace("&m", "<strikethrough>")
                .replace("&n", "<underlined>")
                .replace("&o", "<italic>")
                .replace("&r", "<reset>");

        // 3. Final Parse with MiniMessage
        return MiniMessage.miniMessage().deserialize(text);
    }

    /**
     * Specifically handles LuckPerms prefixes which might only be Legacy.
     */
    public static Component parseLegacy(String text) {
        if (text == null) return Component.empty();
        return LegacyComponentSerializer.legacyAmpersand().deserialize(text);
    }
          }
