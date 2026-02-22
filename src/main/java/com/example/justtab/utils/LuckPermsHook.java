package com.example.justtab.utils;

import com.example.justtab.JustTAB;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class LuckPermsHook {
    private LuckPerms luckPerms;

    public LuckPermsHook(JustTAB plugin) {
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            this.luckPerms = provider.getProvider();
            plugin.getLogger().info("Hooked into LuckPerms.");
        }
    }

    public String getPrefix(Player player) {
        if (luckPerms == null) return "";
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null) return "";
        String p = user.getCachedData().getMetaData().getPrefix();
        return p == null ? "" : p;
    }

    public String getSuffix(Player player) {
        if (luckPerms == null) return "";
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null) return "";
        String s = user.getCachedData().getMetaData().getSuffix();
        return s == null ? "" : s;
    }
}
