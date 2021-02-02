package com.skydhs.czruby;

import org.bukkit.plugin.java.JavaPlugin;

public class Core extends JavaPlugin {
    private static Core instance;

    private Core() {
    }

    @Override
    public void onEnable() {
        Core.instance = new Core();
    }

    @Override
    public void onDisable() {
    }

    public static Core getInstance() {
        return instance;
    }
}