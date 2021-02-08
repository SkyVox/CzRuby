package com.skydhs.czruby.menu;

import com.skydhs.czruby.manager.ItemBuilder;
import com.skydhs.czruby.manager.entity.Ruby;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class StoreLogMenu {
    private static final int[] GLASS_SLOTS = new int[] {
            0, 1, 2, 3, 4, 5, 6, 7, 8,
            9, 17, 18, 26, 27, 35, 36, 44,
            45, 46, 47, 48, 50, 51, 52, 53
    };
    private static final ItemStack GLASS = new ItemBuilder(Material.valueOf("STAINED_GLASS_PANE"), 1, (short) 7).build();
    private static final ItemStack CLOSE = new ItemBuilder(Material.WOOL, 1, (short) 14).withName(ChatColor.RED + "Fechar").withLore(Arrays.asList(ChatColor.GRAY + "Clique para fechar.")).build();

    private final String title = ChatColor.GRAY + "Rubi Registros";
    private final int rows = 54;

    public StoreLogMenu() {
    }

    public void open(Player player, Ruby ruby) {
        Inventory inventory = Bukkit.createInventory(null, rows, title);
        for (int i : GLASS_SLOTS) inventory.setItem(i, GLASS);
        for (ItemStack item : ruby.getLog()) inventory.addItem(item);
        inventory.setItem(49, CLOSE);
        player.openInventory(inventory);
    }

    public String getTitle() {
        return title;
    }

    public int getRows() {
        return rows;
    }
}