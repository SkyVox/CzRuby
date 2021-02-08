package com.skydhs.czruby.menu;

import com.skydhs.czruby.Core;
import com.skydhs.czruby.CurrencyType;
import com.skydhs.czruby.FileUtil;
import com.skydhs.czruby.manager.entity.Ruby;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class StoreMenu {
    private final String title = ChatColor.translateAlternateColorCodes('&', FileUtil.getFile("store").get().getString("Store-Menu.info.title"));
    private final int rows = FileUtil.getFile("store").get().getInt("Store-Menu.info.rows") * 9;

    // Menu items.
    private Set<Display> items;

    public StoreMenu(Set<Display> items) {
        this.items = items;
    }

    public void open(Player player) {
        Ruby ruby = Ruby.from(player);

        if (ruby == null) {
            player.sendMessage(ChatColor.RED + "An error has occurred! Please report this '#1'.");
            return;
        }

        // Create new inventory.
        Inventory inventory = Bukkit.createInventory(null, rows, title);

        this.items.forEach(display -> {
            if (display.slot < 0 || display.slot >= 54 || display.item == null) return;

            ItemStack item = display.item.clone();
            ItemMeta meta = item.getItemMeta();

            if (meta != null) {
                meta.setDisplayName(ruby.replace(display.name));
                meta.setLore(display.lore.stream().map(str -> StringUtils.replaceEach(ruby.replace(str), new String[] {
                        "%price%",
                        "%currency%",
                        "%stock_size%"
                }, new String[] {
                        String.valueOf(display.price),
                        display.currency.getName(),
                        (display.getProduct() == null || !display.getProduct().hasStock() ? FileUtil.get().getString("Settings.no-stock-available").asString() : String.valueOf(display.getProduct().getStock()))
                })).collect(Collectors.toList()));
                item.setItemMeta(meta);
            }

            inventory.setItem(display.slot, item);
        });

        player.openInventory(inventory);
    }

    public String getTitle() {
        return title;
    }

    public int getRows() {
        return rows;
    }

    public Set<Display> getItems() {
        return new HashSet<>(this.items);
    }

    public Display getDisplayBySlot(int slot) {
        return this.items.stream().filter(i -> i.getSlot() == slot).findFirst().orElse(null);
    }

    public static class Display {
        private final String pathId;

        private ItemStack item;
        private String name;
        private List<String> lore;
        private int slot;

        private CurrencyType currency;
        private long price;

        // Product information.
        private Product product;

        public Display(final String pathId, ItemStack item, String name, List<String> lore, int slot, boolean informative, CurrencyType currency, long price, Product product) {
            this.pathId = pathId;
            this.item = item;
            this.name = name;
            this.lore = lore;
            this.slot = slot;
            this.currency = currency;
            this.price = price;
            this.product = informative ? null : product;
        }

        public final String getPathId() {
            return pathId;
        }

        public ItemStack getItem() {
            return item;
        }

        public String getName() {
            return name;
        }

        public List<String> getLore() {
            return lore;
        }

        public int getSlot() {
            return slot;
        }

        public CurrencyType getCurrencyType() {
            return currency;
        }

        public long getPrice() {
            return price;
        }

        public Product getProduct() {
            return product;
        }

        public boolean hasProduct() {
            return product != null;
        }

        /**
         * Process and claim this product.
         *
         * @param player Player who interact with
         *               the current item.
         * @param clicked Item that this player has
         *                clicked on. This will be
         *                used to save as log.
         * @return A clone from @clicked completely re-built as log item.
         */
        public ItemStack claim(Player player, ItemStack clicked) {
            final Product product = getProduct();
            if (!product.hasStock()) return null;

            // Create new lore log.
            List<String> lore = new ArrayList<>();

            lore.add(ChatColor.GRAY + "Registro de compra " + ChatColor.DARK_GRAY + ChatColor.ITALIC + StringUtils.replace(pathId, "_", " "));
            lore.add("");

            if (product.key != null && !product.key.isEmpty()) {
                int i = product.amount;

                lore.add(ChatColor.GRAY + "Chaves:");
                while (i-- > 0) {
                    String str = product.key.remove(0);
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', str));

                    lore.add(ChatColor.YELLOW + " - " + ChatColor.ITALIC + str);
                    if (product.key.size() <= 0) break;
                }
                lore.add("");
            }

            if (product.commands != null) {
                lore.add(ChatColor.GRAY + "Comandos:");
                for (String str : product.commands) {
                    String command = StringUtils.replaceEach(str, new String[] {
                            "[PLAYER]",
                            "[CONSOLE]",
                            "/",
                            "%player_name%"
                    }, new String[] {
                            "",
                            "",
                            "",
                            player.getName()
                    });

                    if (StringUtils.containsIgnoreCase(str, "[CONSOLE]")) {
                        // Console should execute this command.
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                    } else {
                        // Then, Player should execute this command.
                        Bukkit.dispatchCommand(player, command);
                    }
                    lore.add(ChatColor.YELLOW + " - " + ChatColor.ITALIC + command);
                }
                lore.add("");
            }

            if (product.messages != null) {
                for (String str : product.messages) {
                    player.sendMessage(str);
                }
            }

            if (product.items != null && product.items.length > 0) {
                lore.add(ChatColor.GRAY + "Itens:");
                for (ItemStack item : product.items) {
                    // Send this item for the given player.
                    Map<Integer, ItemStack> remainder = player.getInventory().addItem(item.clone());

                    lore.add(ChatColor.YELLOW + " - " + ChatColor.ITALIC + item.getType().name() + "x" + item.getAmount());
                    if (remainder != null && !remainder.isEmpty()) {
                        remainder.values().forEach(entry -> {
                            if (entry != null) {
                                player.getWorld().dropItemNaturally(player.getLocation().add(0D, 1.5D, 0D), entry);
                            }
                        });
                    }
                }
            }

            // Refresh file keys and decrease stock.
            product.saveFile(pathId, FileUtil.getFile("store"));

            ItemStack ret = clicked.clone();
            ItemMeta meta = ret.getItemMeta();

            // Change item description.
            meta.setDisplayName(ChatColor.GOLD + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            meta.setLore(lore);

            ret.setItemMeta(meta);
            return ret;
        }
    }

    public static class Product {
        private int stock, amount;
        private List<String> key;
        private String[] commands, messages;
        private ItemStack[] items;

        public Product(int stock, int amount, List<String> key, String[] commands, String[] messages, ItemStack[] items) {
            this.stock = stock;
            this.amount = amount;
            this.key = key;
            this.commands = commands;
            this.messages = messages;
            this.items = items;
        }

        public int getStock() {
            return stock;
        }

        public boolean hasStock() {
            return stock > 0;
        }

        public int getAmount() {
            return amount;
        }

        public List<String> getKey() {
            return key;
        }

        public String[] getCommands() {
            return commands;
        }

        public String[] getMessages() {
            return messages;
        }

        public ItemStack[] getItems() {
            return items;
        }

        private void saveFile(final String path, FileUtil.FileManager file) {
            this.stock-=1;

            new BukkitRunnable() {
                @Override
                public void run() {
                    file.get().set("Store-Menu.items." + path + ".product.in-stock", stock);
                    file.get().set("Store-Menu.items." + path + ".product.key", key);
                    file.save();
                }
            }.runTaskLaterAsynchronously(Core.getInstance(), 0L);
        }
    }
}