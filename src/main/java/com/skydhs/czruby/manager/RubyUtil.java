package com.skydhs.czruby.manager;

import com.skydhs.czruby.Core;
import com.skydhs.czruby.CurrencyType;
import com.skydhs.czruby.FileUtil;
import com.skydhs.czruby.menu.StoreMenu;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RubyUtil {
    private static RubyUtil instance;

    private StoreMenu storeMenu;

    public RubyUtil() {
        RubyUtil.instance = this;
    }

    public StoreMenu getStoreMenu() {
        return storeMenu;
    }

    public static RubyUtil getInstance() {
        return instance;
    }

    public void load(Core core) {
        new BukkitRunnable() {
            @Override
            public void run() {
                loadStore(FileUtil.getFile("store"));
                // TODO, Load online players from database.
            }
        }.runTaskLaterAsynchronously(core, 0L);
    }

    public void unload() {
        // TODO, Unload cached players save their stats on database.
    }

    private void loadStore(FileUtil.FileManager file) {
        Set<StoreMenu.Display> display = new HashSet<>(54);

        file.get().getConfigurationSection("Store-Menu.items").getKeys(false).forEach(section -> {
            final String path = "Store-Menu.items." + section;

            ItemBuilder builder = ItemBuilder.get(file.get(), path);
            String name = ChatColor.translateAlternateColorCodes('&', file.get().getString(path + ".name"));
            List<String> lore = file.get().getStringList(path + ".lore").stream().map(str -> ChatColor.translateAlternateColorCodes('&', str)).collect(Collectors.toList());
            int slot = file.get().getInt(path + "slot");
            boolean informative = file.get().getString(path + ".informative").equalsIgnoreCase("true");
            CurrencyType currency = CurrencyType.valueOf(file.get().getString(path + ".currency").toUpperCase());
            long price = file.get().getLong(path + "price");

            // Loading the product information.
            int stock = file.get().getInt(path + ".product.in-stock"), amount = file.get().getInt(path + ".product.amount");
            List<String> key = file.get().contains(path + ".product.key") ? file.get().getStringList(path + ".product.key") : null;
            String[] commands = file.get().contains(path + ".product.commands") ? file.get().getStringList(path + ".product.commands").toArray(new String[0]) : null;
            String[] messages = file.get().contains(path + ".product.messages") ? file.get().getStringList(path + ".product.messages").stream().map(str -> ChatColor.translateAlternateColorCodes('&', str)).toArray(String[]::new) : null;
            ItemStack[] items = file.get().contains(path + ".product.items") ? file.get().getConfigurationSection(path + ".product.items").getKeys(false).stream().map(item -> ItemBuilder.get(file.get(), path + ".product.items." + item).build()).toArray(ItemStack[]::new) : null;

            // Finally cache this item.
            StoreMenu.Product product = new StoreMenu.Product(stock, amount, key, commands, messages, items);
            display.add(new StoreMenu.Display(section, builder.build(), name, lore, slot, informative, currency, price, product));
        });

        // Create new StoreMenu object.
        this.storeMenu = new StoreMenu(display);
    }
}