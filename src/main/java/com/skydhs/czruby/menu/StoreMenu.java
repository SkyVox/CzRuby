package com.skydhs.czruby.menu;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StoreMenu {
    private static StoreMenu instance;

    private final String title;
    private final int rows;
    private final Map<DisplayItem, Reward> items = new HashMap<>(54);

    public StoreMenu(String title, int rows, Map<DisplayItem, Reward> items) {
        StoreMenu.instance = this;

        this.title = title;
        this.rows = rows;
        this.items.putAll(items);
    }

    public static void open(Player player) {
    }

    public static StoreMenu getInstance() {
        return instance;
    }

    public class DisplayItem {
        private ItemStack item;
        private String name;
        private List<String> lore;
        private boolean informative;

        private CurrencyType currency;
        private long price;

        private int slot;

        public DisplayItem() {
        }
    }

    public class Reward {
        private ItemStack[] items;
        private String[] commands, messages;

        // Those are probably paid products.
        private List<String> product;
        private int productAmount;

        public Reward() {
        }
    }
}