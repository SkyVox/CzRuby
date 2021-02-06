package com.skydhs.czruby.integration.placeholderapi;

import com.skydhs.czruby.integration.Integration;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import org.bukkit.entity.Player;

import java.util.List;

public class PlaceholderIntegration extends Integration<PlaceholderAPIPlugin> {
    private static PlaceholderIntegration instance;

    public PlaceholderIntegration() {
        super("PlaceholderAPI");
        PlaceholderIntegration.instance = this;
    }

    @Override
    public PlaceholderAPIPlugin getMain() {
        return PlaceholderAPIPlugin.getInstance();
    }

    public String setPlaceholders(Player player, final String text) {
        if (!isEnabled()) return text;
        if (text == null || text.isEmpty()) return text;
        return PlaceholderAPI.setPlaceholders(player, text);
    }

    public List<String> setPlaceholders(Player player, final List<String> text) {
        if (!isEnabled()) return text;
        if (text == null || text.isEmpty()) return text;
        return PlaceholderAPI.setPlaceholders(player, text);
    }

    public static PlaceholderIntegration getInstance() {
        return instance;
    }
}