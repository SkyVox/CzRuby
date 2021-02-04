package com.skydhs.czruby;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class Core extends JavaPlugin {
    private static Core instance;

    public final String NAME = getDescription().getName();
    public final String VERSION = getDescription().getVersion();

    private ConsoleCommandSender console = Bukkit.getConsoleSender();

    private Core() {
    }

    @Override
    public void onEnable() {
        Core.instance = new Core();
        long time = System.currentTimeMillis();
        console.sendMessage("----------");
        console.sendMessage(ChatColor.GRAY + "Enabling " + ChatColor.YELLOW +  NAME + ChatColor.DARK_GRAY + "> " + ChatColor.GRAY + "Version: " + ChatColor.YELLOW + VERSION + ChatColor.GRAY + "!");

        // -- Generate and setup the configuration files -- \\
        new FileUtil(this, new FileUtil.FileInfo[] {
                new FileUtil.FileInfo((char) 1),
                new FileUtil.FileInfo("store", false),
        });

        console.sendMessage(ChatColor.YELLOW +  NAME + ChatColor.DARK_GRAY + "> " + ChatColor.GRAY + "has been enabled! Took " + getSpentTime(time) + "ms.");
        console.sendMessage("----------");
    }

    @Override
    public void onDisable() {
    }

    private long getSpentTime(long time) {
        return System.currentTimeMillis() - time;
    }

    public static Core getInstance() {
        return instance;
    }
}