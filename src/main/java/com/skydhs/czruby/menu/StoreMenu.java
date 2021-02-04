package com.skydhs.czruby.menu;

import com.skydhs.czruby.FileUtil;
import com.skydhs.czruby.manager.entity.Ruby;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StoreMenu {
    private static StoreMenu instance;

    private final String title;
    private final int rows;
    private Map<DisplayItem, Reward> items;

    public StoreMenu(String title, int rows, Map<DisplayItem, Reward> items) {
        StoreMenu.instance = this;

        this.title = title;
        this.rows = rows;
        this.items = items;
    }

    public static void open(Player player) {
        final StoreMenu menu = getInstance();
        Ruby ruby = Ruby.from(player);

        if (ruby == null) {
            player.sendMessage(ChatColor.RED + "An error has occurred! Please report this '#1'.");
            return;
        }

        Inventory inventory = Bukkit.createInventory(null, menu.rows, menu.title);

        menu.items.forEach((display, value) -> {
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
                        String.valueOf(value == null ? "0" : value.getStockSize())
                })).collect(Collectors.toList()));
                item.setItemMeta(meta);
            }

            inventory.setItem(display.slot, item);
        });

        player.openInventory(inventory);
    }

    public static Map.Entry<DisplayItem, Reward> getEntryBySlot(int slot) {
        return getInstance().items.entrySet().stream().filter(i -> i.getKey().slot == slot).findFirst().orElse(null);
    }

    private static StoreMenu getInstance() {
        return instance;
    }

    public static class DisplayItem {
        private final String idKey;

        private ItemStack item;
        private String name;
        private List<String> lore;
        private boolean informative;

        private CurrencyType currency;
        private long price;

        private int slot;

        public DisplayItem(final String idKey, ItemStack item, String name, List<String> lore, boolean informative, CurrencyType currency, long price, int slot) {
            this.idKey = idKey;
            this.item = item;
            this.name = name;
            this.lore = lore;
            this.informative = informative;
            this.currency = currency;
            this.price = price;
            this.slot = slot;
        }

        public String getIdKey() {
            return idKey;
        }

        public ItemStack getItem() {
            return item.clone();
        }

        public String getName() {
            return name;
        }

        public List<String> getLore() {
            return lore;
        }

        private boolean isInformative() {
            return informative;
        }

        public CurrencyType getCurrency() {
            return currency;
        }

        public long getPrice() {
            return price;
        }

        public int getSlot() {
            return slot;
        }
    }

    public static class Reward {
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

        public String claim(Player player, String idKey) {
            final StringBuilder log = new StringBuilder("Data: ").append(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append(" - ").append(idKey).append("\n");

            if (messages != null) {
                for (String str : messages) {
                    player.sendMessage(str);
                }
            }

            if (items != null && items.length > 0) {
                log.append("Itens: ");
                for (ItemStack item : items) {
                    log.append(item.getType().name()).append("x").append(item.getAmount()).append(" ");

                    // Send this item for the given player.
                    Map<Integer, ItemStack> remainder = player.getInventory().addItem(item.clone());

                    if (remainder != null && !remainder.isEmpty()) {
                        remainder.values().forEach(entry -> {
                            if (entry != null) {
                                player.getWorld().dropItemNaturally(player.getLocation().add(0D, 1.5D, 0D), entry);
                            }
                        });
                    }
                }
                log.append("\n");
            }

            if (commands != null) {
                log.append("Comandos: ");
                for (String str : commands) {
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

                    log.append(command).append("\n");
                }
                log.append("\n");
            }

            if (product != null) {
                log.append("Produto: x").append(productAmount).append("\n");
                int i = productAmount;

                while (i > 0) {
                    String str = product.remove(0);
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', str));

                    // Log this.
                    log.append(str).append("\n");

                    i--;
                    if (product.size() <= 0) break;
                }
            }

            FileUtil.getFile("store").get().set("products." + idKey + ".product.stock", product);
            FileUtil.getFile("store").save();

            return log.toString();
        }

        public boolean hasStock() {
            return getStockSize() >= productAmount;
        }

        public int getStockSize() {
            return product == null || product.isEmpty() ? 0 : product.size();
        }
    }
}