package com.skydhs.czruby.manager.entity;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.skydhs.czruby.FileUtil;
import com.skydhs.czruby.manager.RubyUtil;
import com.skydhs.czruby.menu.StoreMenu;
import net.minecraft.server.v1_8_R3.MojangsonParseException;
import net.minecraft.server.v1_8_R3.MojangsonParser;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Ruby {
    private static final Map<String, Ruby> RUBIES = new HashMap<>(1024);

    private final String playerName;
    private long fragments, rubies;
    private boolean online;

    // Store purchase logs.
    // This might be null if this player haven't purchased anything on shop
    private List<ItemStack> log = null;

    public Ruby(final String playerName, long fragments, long rubies) {
        this(playerName, fragments, rubies, false);
    }

    public Ruby(final String playerName, long fragments, long rubies, boolean online) {
        this.playerName = playerName;
        this.fragments = fragments;
        this.rubies = rubies;
        this.online = online;

        // Add this entity to cache.
        this.cache();
    }

    private void cache() {
        Ruby.RUBIES.put(playerName.toLowerCase(), this);
    }

    public String getPlayerName() {
        return playerName;
    }

    public Player asPlayer() {
        return isOnline() ? Bukkit.getPlayer(this.playerName) : null;
    }

    public long getFragments() {
        return fragments;
    }

    public void addFragment(long n) {
        this.fragments+=n;
    }

    public void setFragments(long n) {
        this.fragments = n;
    }

    public long getRubies() {
        return rubies;
    }

    public void addRuby(long n) {
        this.rubies+=n;
    }

    public void setRubies(long n) {
        if (rubies - n < 0) n = this.rubies;
        this.rubies = n;
    }

    public boolean isOnline() {
        return online;
    }

    public boolean changeOnline() {
        return this.online = !this.online;
    }

    public List<ItemStack> getLog() {
        return log;
    }

    public void setLog(List<ItemStack> log) {
        this.log = log;
    }

    private void log(final ItemStack item) {
        if (item == null || item.getType().equals(Material.AIR)) return;
        if (this.log == null) this.log = new LinkedList<>();
        this.log.add(item);
    }

    public boolean processPurchase(Player player, ItemStack clicked, int slot) {
        StoreMenu.Display display = RubyUtil.getInstance().getStoreMenu().getDisplayBySlot(slot);
        if (display == null || !display.hasProduct()) return false;

        if (!display.getProduct().hasStock()) {
            player.sendMessage(FileUtil.get().getString("Messages.product-with-no-stock").asString());
            return false;
        }

        final long price = display.getPrice();

        switch (display.getCurrencyType()) {
            case FRAGMENT:
                if (this.fragments < price) {
                    player.sendMessage(FileUtil.get().getString("Messages.no-enough-fragments", new String[] {
                            "%price%"
                    }, new String[] {
                            String.valueOf(price)
                    }).asString());
                    return false;
                }
                this.fragments-=price;
                break;
            case RUBY:
                if (this.rubies < price) {
                    player.sendMessage(FileUtil.get().getString("Messages.no-enough-rubies", new String[] {
                            "%price%"
                    }, new String[] {
                            String.valueOf(price)
                    }).asString());
                    return false;
                }
                this.rubies-=price;
                break;
        }

        // Claim this reward.
        final ItemStack log = display.claim(player, clicked);
        this.log(log);
        return true;
    }

    public void deserializeLogItems(final String json) throws MojangsonParseException {
        if (json == null || json.isEmpty() || json.equals("{}")) {
            this.log = null;
        } else {
            this.log = new LinkedList<>();

            JsonObject object = (JsonObject) new JsonParser().parse(json);
            JsonArray array = object.entrySet().iterator().next().getValue().getAsJsonArray();

            Iterator<JsonElement> iterator = array.iterator();

            while (iterator.hasNext()) {
                JsonElement element = iterator.next();
                if (!(element instanceof JsonObject)) continue;
                JsonObject obj = (JsonObject) element;

                // Create our ItemStack.
                NBTTagCompound tag = MojangsonParser.parse(StringUtils.replaceEach(element.toString(), new String[] {
                        "\"",
                        "[{",
                        "}]",
                        "&"
                }, new String[] {
                        "",
                        "[",
                        "]",
                        String.valueOf(ChatColor.COLOR_CHAR)
                }));
                ItemStack item = CraftItemStack.asBukkitCopy(net.minecraft.server.v1_8_R3.ItemStack.createStack(tag));

                // Add this item into @log list.
                this.log.add(item);
            }
        }
    }

    public String serializeLogToJson() {
        if (log == null || log.isEmpty()) return "{}";
        JsonObject object = new JsonObject();
        final JsonArray array = new JsonArray();

        log.forEach(item -> {
            if (item == null || item.getType().equals(Material.AIR)) return;
            String json = StringUtils.replaceEach(CraftItemStack.asNMSCopy(item).save(new NBTTagCompound()).toString(), new String[] {
                    "[",
                    "]",
                    "&"
            }, new String[] {
                    "[{",
                    "}]",
                    String.valueOf(ChatColor.COLOR_CHAR)
            });
            JsonObject obj = new JsonParser().parse(json).getAsJsonObject();

            // Remove unnecessary keys.
            JsonElement element = obj.get("Damage");

            if (element != null && StringUtils.equals(element.getAsString(), "0s")) {
                obj.remove("Damage");
            }

            array.add(obj);
        });

        object.add("log_items", array);
        return object.toString();
    }

    public String replace(final String text) {
        return text == null || text.isEmpty() ? text : StringUtils.replaceEach(text, placeholders(), replacement());
    }

    public String[] placeholders() {
        return new String[] {
                "%player_name%",
                "%fragments%",
                "%rubies%"
        };
    }

    public String[] replacement() {
        return new String[] {
                this.playerName,
                String.valueOf(this.fragments),
                String.valueOf(this.rubies)
        };
    }

    public static Ruby from(Player player) {
        return from(player.getName());
    }

    public static Ruby from(final String playerName) {
        return Ruby.RUBIES.get(playerName.toLowerCase());
    }

    public static Map<String, Ruby> getRubiesCache() {
        return RUBIES;
    }
}