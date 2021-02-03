package com.skydhs.czruby.menu;

import com.skydhs.czruby.manager.entity.Ruby;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        final StoreMenu menu = getInstance();
        Ruby ruby = Ruby.from(player);

        if (ruby == null) {
            player.sendMessage(ChatColor.RED + "An error has occurred! Please report this '#1'.");
            return;
        }

        Inventory inventory = Bukkit.createInventory(null, menu.rows, menu.title);

        menu.items.keySet().forEach(display -> {
            if (display.slot < 0 || display.slot >= 54 || display.item == null) return;

            ItemStack item = display.item.clone();
            ItemMeta meta = item.getItemMeta();

            if (meta != null) {
                meta.setDisplayName(ruby.replace(display.name));
                meta.setLore(display.lore.stream().map(str -> StringUtils.replaceEach(ruby.replace(str), new String[] {
                        "%price%",
                        "%currency%"
                }, new String[] {
                        String.valueOf(display.price),
                        display.currency.getName()
                })).collect(Collectors.toList()));
                item.setItemMeta(meta);
            }

            inventory.setItem(display.slot, item);
        });

        player.openInventory(inventory);
    }

    public static Map.Entry<DisplayItem, Reward> getEntryBySlot(int slot) {
        // getInstance().items.entrySet().stream().filter(i -> i.getKey().slot == slot).findFirst().map(Map.Entry::getValue).orElse(null);
        return getInstance().items.entrySet().stream().filter(i -> i.getKey().slot == slot).findFirst().orElse(null);
    }

    private static StoreMenu getInstance() {
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

        public DisplayItem(ItemStack item, String name, List<String> lore, boolean informative, CurrencyType currency, long price, int slot) {
            this.item = item;
            this.name = name;
            this.lore = lore;
            this.informative = informative;
            this.currency = currency;
            this.price = price;
            this.slot = slot;
        }
    }

    public class Reward {
        private ItemStack[] items;
        private String[] commands, messages;

        // Those are probably paid products.
        private List<String> product;
        private int productAmount;

        public Reward(ItemStack[] items, String[] commands, String[] messages, List<String> product, int productAmount) {
            this.items = items;
            this.commands = commands;
            this.messages = messages;
            this.product = product;
            this.productAmount = productAmount;
        }

        public void claim(Player player) {
            // TODO.
        }
    }
}