package com.skydhs.czruby;

import com.skydhs.czruby.commands.FragmentCmd;
import com.skydhs.czruby.commands.RubyCmd;
import com.skydhs.czruby.database.Database;
import com.skydhs.czruby.integration.placeholderapi.PlaceholderIntegration;
import com.skydhs.czruby.integration.placeholderapi.PlaceholderRegister;
import com.skydhs.czruby.listeners.HandleGeneralListeners;
import com.skydhs.czruby.listeners.InventoryClickListener;
import com.skydhs.czruby.manager.RubyUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class Core extends JavaPlugin {
    private static Core instance;

    public final String NAME = getDescription().getName();
    public final String VERSION = getDescription().getVersion();

    private ConsoleCommandSender console = Bukkit.getConsoleSender();

    /*
     * Permissions:
     *  - ruby.admin  ::  Execute admin commands.
     */

    @Override
    public void onEnable() {
        long time = System.currentTimeMillis();
        console.sendMessage("----------");
        console.sendMessage(ChatColor.GRAY + "Enabling " + ChatColor.YELLOW +  NAME + ChatColor.DARK_GRAY + "> " + ChatColor.GRAY + "Version: " + ChatColor.YELLOW + VERSION + ChatColor.GRAY + "!");

        Core.instance = this;

        // -- Generate and setup the configuration files -- \\
        new FileUtil(this, new FileUtil.FileInfo[] {
                new FileUtil.FileInfo((char) 1),
                new FileUtil.FileInfo("store", false),
        });

        // -- Loading Database -- \\
        if (!FileUtil.get().getBoolean("MySQL.enabled") || !Database.from(FileUtil.get()).connect()) {
            console.sendMessage("MySQL is disabled, since this plugin depends on it we're disabling " + NAME + "...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // -- Load all classes instances and the plugin dependencies -- \\
        console.sendMessage("Loading dependencies and instances...");
        new RubyUtil().load(this);
        new PlaceholderIntegration();

        console.sendMessage("Loading command and listeners...");
        getServer().getPluginManager().registerEvents(new HandleGeneralListeners(), this);
        getServer().getPluginManager().registerEvents(new InventoryClickListener(), this);
        getCommand("rubifrag").setExecutor(new FragmentCmd());
        getCommand("rubi").setExecutor(new RubyCmd());

        if (PlaceholderIntegration.getInstance().isEnabled()) {
            new PlaceholderRegister(this).register();
        }

        console.sendMessage(ChatColor.YELLOW +  NAME + ChatColor.DARK_GRAY + "> " + ChatColor.GRAY + "has been enabled! Took " + getSpentTime(time) + "ms.");
        console.sendMessage("----------");
    }

    @Override
    public void onDisable() {
        console.sendMessage("----------");
        console.sendMessage(ChatColor.GRAY + "Disabling " + ChatColor.YELLOW +  NAME + ChatColor.DARK_GRAY + "> " + ChatColor.GRAY + "Version: " + ChatColor.YELLOW + VERSION + ChatColor.GRAY + "!");

        if (Database.getInstance() != null && Database.getInstance().isConnected()) {
            Database.getInstance().close();
        }

        console.sendMessage(ChatColor.YELLOW +  NAME + ChatColor.DARK_GRAY + "> " + ChatColor.GRAY + "has been disabled!");
        console.sendMessage("----------");
    }

    private long getSpentTime(long time) {
        return System.currentTimeMillis() - time;
    }

    public static Core getInstance() {
        return instance;
    }
}