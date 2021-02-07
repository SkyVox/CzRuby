package com.skydhs.czruby.listeners;

import com.skydhs.czruby.Core;
import com.skydhs.czruby.database.Database;
import com.skydhs.czruby.manager.entity.Ruby;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class HandleGeneralListeners implements Listener {

    public HandleGeneralListeners() {
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        new BukkitRunnable() {
            @Override
            public void run() {
                Ruby ruby = Database.getInstance().getRubyTable().getPlayerRuby(player.getName(), true);

                if (ruby == null) {
                    ruby = new Ruby(player.getName(), 0L, 0L, true);
                }
            }
        }.runTaskLaterAsynchronously(Core.getInstance(), 0L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Ruby ruby = Ruby.from(event.getPlayer());

        if (ruby != null) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    Database.getInstance().getRubyTable().update(ruby);
                }
            }.runTaskLaterAsynchronously(Core.getInstance(), 0L);
        }
    }
}