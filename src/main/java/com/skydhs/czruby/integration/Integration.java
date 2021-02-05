package com.skydhs.czruby.integration;

import com.skydhs.czruby.Core;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.logging.Level;

public abstract class Integration<T> implements IntegrationInterface<T> {
    private String name;
    private Boolean enabled;

    /**
     * The @integration abstract
     * which helps to implement
     * a new dependency.
     *
     * @param name soft-depend/dependency
     *             plugin name.
     */
    public Integration(final String name) {
        this.name = name;
        this.enabled = setup();

        this.log();
    }

    /**
     * The @integration abstract
     * which helps to implement
     * a new dependency.
     *
     * @param name soft-depend/dependency
     *             plugin name.
     * @param log send an status message
     *            on console.
     */
    public Integration(final String name, Boolean log) {
        this.name = name;
        this.enabled = setup();

        if (log) this.log();
    }

    /**
     * Send an information message
     * on console.
     */
    public void log() {
        sendMessage(isEnabled() ? ChatColor.GREEN + name.toUpperCase() + " has been hooked!" : ChatColor.RED + "We could not hook to " + name.toUpperCase() + " (plugin not found)!");
    }

    /**
     * Get the plugin name.
     *
     * @return plugin name.
     */
    public String getName() {
        return name;
    }

    /**
     * Verify if this plugin
     * is running on your server.
     *
     * @return is plugin is currently
     *          running on your server.
     */
    public Boolean isEnabled() {
        return Bukkit.getServer().getPluginManager().isPluginEnabled(getName());
    }

    public void refresh(Boolean log) {
        this.enabled = setup();

        if (log) {
            this.log();
        }
    }

    /**
     * Setup this dependency.
     *
     * @return if plugin is currently
     *          running on your server.
     */
    private Boolean setup() {
        if (name == null || name.isEmpty()) return false;
        return Bukkit.getServer().getPluginManager().isPluginEnabled(getName());
    }

    /**
     * Logs an message
     * to console.
     *
     * @param message message to send.
     */
    public void sendMessage(final String message) {
        if (message == null || message.isEmpty()) return;
        Core.getInstance().getLogger().log(Level.INFO, message);
    }
}