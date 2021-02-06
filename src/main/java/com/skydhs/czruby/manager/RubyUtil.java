package com.skydhs.czruby.manager;

import com.skydhs.czruby.Core;
import com.skydhs.czruby.FileUtil;
import com.skydhs.czruby.menu.CurrencyType;
import com.skydhs.czruby.menu.StoreMenu;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RubyUtil {

    public RubyUtil() {
    }

    public void asyncLoad(Core core) {
        new BukkitRunnable() {
            @Override
            public void run() {
                syncLoad();

                // TODO. Load online players from database.
            }
        }.runTaskLaterAsynchronously(core, 0L);
    }

    public void syncLoad() {
        // Load the store system.
        loadStore(FileUtil.getFile("store"));
    }

    private static void loadStore(FileUtil.FileManager file) {
        final String title = ChatColor.translateAlternateColorCodes('&', file.get().getString("Menu.info.title"));
        final int rows = file.get().getInt("Menu.info.rows");
        Map<StoreMenu.DisplayItem, StoreMenu.Reward> items = new HashMap<>(54);

        file.get().getConfigurationSection("products").getKeys(false).forEach(section -> {
            final String path = "products." + section;

            // Display information.
            ItemBuilder builder = ItemBuilder.get(file.get(), path);
            String name = ChatColor.translateAlternateColorCodes('&', file.get().getString(path + ".name"));
            List<String> lore = file.get().getStringList(path + ".lore").stream().map(str -> ChatColor.translateAlternateColorCodes('&', str)).collect(Collectors.toList());
            boolean informative = file.get().getString(path + ".informative").equalsIgnoreCase("true");
            CurrencyType currency = CurrencyType.valueOf(file.get().getString(path + ".currency").toUpperCase());
            long price = file.get().getLong(path + "price");
            int slot = file.get().getInt(path + "slot");

            // Reward information.
            ItemStack[] rewardItems = file.get().getConfigurationSection(path + ".product.rewards.items").getKeys(false).stream().map(item -> ItemBuilder.get(file.get(), path + ".items." + item).build()).toArray(ItemStack[]::new);
            String[] commands = file.get().getStringList(path + ".product.rewards.commands").toArray(new String[0]);
            String[] messages = file.get().getStringList(path + ".product.rewards.messages").stream().map(str -> ChatColor.translateAlternateColorCodes('&', str)).toArray(String[]::new);
            List<String> product = file.get().getStringList(path + ".product.rewards.stock");
            int productAmount = file.get().getInt(path + ".product.rewards.amount");

            // Add this to items.
            items.put(
                    new StoreMenu.DisplayItem(section, builder.build(), name, lore, informative, currency, price, slot),
                    new StoreMenu.Reward(rewardItems, commands, messages, product, productAmount)
            );
        });

        // Create new object.
        new StoreMenu(title, rows, items);
    }
}