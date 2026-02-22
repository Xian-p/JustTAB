package com.example.justtab.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtil {
    
    // Pattern to find Hex colors like &#FFFFFF
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    /**
     * Converts a string containing Legacy (&) and Hex (&#) codes 
     * into a pure MiniMessage format string.
     * 
     * Example: "&aHello <player>" -> "<green>Hello <player>"
     */
    public static String convert(String text) {
        if (text == null) return "";

        // 1. Convert Hex: &#123456 -> <#123456>
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "<#" + matcher.group(1) + ">");
        }
        matcher.appendTail(sb);
        text = sb.toString();

        // 2. Convert Legacy: &a -> <green>
        // We replace all standard color codes with their MiniMessage tag equivalents.
        return text
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
    }
}
