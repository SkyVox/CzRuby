package com.skydhs.czruby.integration.placeholderapi;

import com.skydhs.czruby.Core;
import com.skydhs.czruby.manager.entity.Ruby;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class PlaceholderRegister extends PlaceholderExpansion {
    private Core core;

    public PlaceholderRegister(Core core) {
        this.core = core;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String getAuthor() {
        return core.getDescription().getAuthors().toString();
    }

    @Override
    public String getIdentifier() {
        return "czruby";
    }

    @Override
    public String getVersion() {
        return core.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) return "";

        Ruby ruby = Ruby.from(player);
        if (ruby == null) return "";

        // Placeholder -> "%czruby_fragments%"
        if (identifier.equals("fragments")) {
            return String.valueOf(ruby.getFragments());
        } else if (identifier.equals("rubies")) { // Placeholder -> "%czruby_rubies%"
            return String.valueOf(ruby.getRubies());
        }

        return null;
    }
}