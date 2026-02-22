package com.example.justtab.utils;

import com.example.justtab.JustTAB;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class LuckPermsHook {
    private LuckPerms luckPerms;
    private final boolean isHooked;

    public LuckPermsHook(JustTAB plugin) {
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            this.luckPerms = provider.getProvider();
            plugin.getLogger().info("Successfully hooked into LuckPerms!");
            isHooked = true;
        } else {
            plugin.getLogger().warning("LuckPerms not found. Prefixes and Suffixes will not work.");
            isHooked = false;
        }
    }

    public String getPrefix(Player player) {
        if (!isHooked) return "";
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null) return "";
        String val = user.getCachedData().getMetaData().getPrefix();
        return val != null ? val : "";
    }

    public String getSuffix(Player player) {
        if (!isHooked) return "";
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null) return "";
        String val = user.getCachedData().getMetaData().getSuffix();
        return val != null ? val : "";
    }
}