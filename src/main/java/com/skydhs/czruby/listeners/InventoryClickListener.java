package com.skydhs.czruby.listeners;

import com.skydhs.czruby.manager.entity.Ruby;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import static com.skydhs.czruby.manager.RubyUtil.getInstance;

public class InventoryClickListener implements Listener {
    private final String TITLE = getInstance().getStoreMenu().getTitle();

    public InventoryClickListener() {
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        ItemStack clicked = event.getCurrentItem();
        int slot = event.getSlot();

        /* Player has clicked outside the inventory. */
        if (slot == -999) return;
        if (clicked == null || clicked.getType().equals(Material.AIR)) return;

        if (title.equals(TITLE)) {
            event.setCancelled(true);
            if (event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT) {
                event.setResult(InventoryClickEvent.Result.DENY);
                return;
            }
            if (event.getClickedInventory() == player.getInventory()) return;

            Ruby ruby = Ruby.from(player);
            if (ruby == null) return;

            // Process this clicked slot.
            ruby.processPurchase(player, clicked, slot);
        }
    }
}