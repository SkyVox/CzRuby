package com.skydhs.czruby.manager;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public class ItemBuilder implements Cloneable {
    private static final Pattern COMPARE_WITH_PLACEHOLDERS = Pattern.compile("\\%[^]]*\\%");
    private static Material[] byId;

    private ItemStack item; // Final item.

    static {
        byId = new Material[0];
        for (Material material : Material.values()) {
            if (byId.length > material.getId()) {
                byId[material.getId()] = material;
            } else {
                byId = Arrays.copyOfRange(byId, 0, material.getId() + 2);
                byId[material.getId()] = material;
            }
        }
    }

    /**
     * Create a new ItemBuilder
     * object.
     *
     * @param item final item to add.
     */
    public ItemBuilder(ItemStack item) {
        Validate.notNull(item, "@Item cannot be null.");

        this.item = item;
    }

    /**
     * Create a new ItemBuilder
     * object.
     *
     * @param material material type
     *                 to create.
     */
    public ItemBuilder(Material material) {
        this(material, 1);
    }

    /**
     * Create a new ItemBuilder
     * object.
     *
     * @param material material type
     *                 to create.
     * @param amount amount of items.
     */
    public ItemBuilder(Material material, int amount) {
        Validate.notNull(material, "@Material cannot be null.");

        if (StringUtils.equals(material.toString(), "SKULL_ITEM")) {
            this.item = new ItemStack(material, amount, (short) 3);
        } else {
            this.item = new ItemStack(material, amount);
        }
    }

    /**
     * Create a new ItemBuilder
     * object.
     *
     * @param material material type
     *                 to create.
     * @param amount amount of items.
     * @param durability item durability/data.
     */
    public ItemBuilder(Material material, int amount, short durability) {
        Validate.notNull(material, "@Material cannot be null.");

        if (StringUtils.equals(material.toString(), "SKULL_ITEM") && durability != 3) {
            this.item = new ItemStack(material, amount, (short) 3);
        } else {
            item = new ItemStack(material, amount, durability);
        }
    }

    /**
     * Set display item name.
     *
     * @param name the name.
     * @return {@link ItemBuilder}
     */
    public ItemBuilder withName(final String name) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return this;

        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        item.setItemMeta(meta);
        return this;
    }

    /**
     * Set item lore.
     *
     * Using the methods "withLore" will
     * set the given lore.
     * To add new lines on the
     * current item lore, use "addLore"
     * methods.
     *
     * @param lore the lore to set.
     * @return {@link ItemBuilder}
     */
    public ItemBuilder withLore(List<String> lore) {
        return withLore(lore, null, null);
    }

    /**
     * Set item lore.
     *
     * Using the methods "withLore" will
     * set the given lore.
     * To add new lines on the
     * current item lore, use "addLore"
     * methods.
     *
     * @param lore the lore to set.
     * @return {@link ItemBuilder}
     */
    public ItemBuilder withLore(String[] lore) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null || lore == null || lore.length <= 0) return this;

        List<String> ret = new ArrayList<>(lore.length);

        for (String str : lore) {
            ret.add(str);
        }

        return withLore(ret, null, null);
    }

    /**
     * Set item lore.
     *
     * Using the methods "withLore" will
     * set the given lore.
     * To add new lines on the
     * current item lore, use "addLore"
     * methods.
     *
     * @param lore the lore to set.
     * @param placeholders the custom placeholders.
     * @param replacers the placeholder
     *                  replacers.
     * @return {@link ItemBuilder}
     */
    public ItemBuilder withLore(List<String> lore, String[] placeholders, String[] replacers) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null || lore == null || lore.size() <= 0) return this;

        List<String> toAdd = new ArrayList<>(lore.size());

        for (String str : lore) {
            toAdd.add(ChatColor.translateAlternateColorCodes('&', applyPlaceholder(str, placeholders, replacers)));
        }

        meta.setLore(toAdd);
        item.setItemMeta(meta);
        return this;
    }

    /**
     * Add new lines to
     * the current item lore.
     *
     * @param lore lore lines to be
     *             added.
     * @return {@link ItemBuilder}
     */
    public ItemBuilder addLore(List<String> lore) {
        return addLore(lore, null, null);
    }

    /**
     * Add new lines to
     * the current item lore.
     *
     * @param lore line to be added.
     * @return {@link ItemBuilder}
     */
    public ItemBuilder addLore(String lore) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null || lore == null || lore.isEmpty()) return this;

        return addLore(new String[] { lore });
    }

    /**
     * Add new lines to
     * the current item lore.
     *
     * @param lore lore lines to be
     *             added.
     * @return {@link ItemBuilder}
     */
    public ItemBuilder addLore(String[] lore) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null || lore == null) return this;

        List<String> ret = new ArrayList<>(lore.length);

        for (String str : lore) {
            ret.add(str);
        }

        return addLore(ret, null, null);
    }

    /**
     * Add new lines to
     * the current item lore.
     *
     * @param lore lore lines to be
     *             added.
     * @param placeholders the custom placeholders.
     * @param replacers the placeholder
     *                  replacers
     * @return {@link ItemBuilder}
     */
    public ItemBuilder addLore(List<String> lore, String[] placeholders, String[] replacers) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null || lore == null || lore.size() <= 0) return this;

        List<String> toAdd = null;

        if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
            List<String> oldLore = item.getItemMeta().getLore();
            toAdd = new ArrayList<>(oldLore.size() + lore.size());

            for (String str : oldLore) {
                toAdd.add(str);
            }
        } else {
            toAdd = new ArrayList<>(lore.size());
        }

        for (String str : lore) {
            toAdd.add(ChatColor.translateAlternateColorCodes('&', applyPlaceholder(str, placeholders, replacers)));
        }

        meta.setLore(toAdd);
        item.setItemMeta(meta);
        return this;
    }

    /**
     * Remove a specific line
     * on the current item
     * lore.
     *
     * @param line text to be removed.
     * @return {@link ItemBuilder}
     */
    public ItemBuilder removeLore(String line) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasLore() || line == null || line.isEmpty()) return this;

        List<String> toAdd = new ArrayList<>(meta.getLore());

        for (String str : meta.getLore()) {
            if (isStringEquals(line, str)) {
                toAdd.remove(str);
                break;
            }
        }

        meta.setLore(toAdd);
        item.setItemMeta(meta);
        return this;
    }

    /**
     * Remove a specific lines
     * on the current item
     * lore.
     *
     * @param lore texts to be
     *             removed.
     * @return {@link ItemBuilder}
     */
    public ItemBuilder removeLore(List<String> lore) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasLore() || lore == null || lore.size() <= 0) return this;

        List<String> toAdd = new ArrayList<>(meta.getLore());

        for (String values : lore) {
            for (String str : meta.getLore()) {
                if (isStringEquals(values, str)) {
                    toAdd.remove(str);
                    break;
                }
            }
        }

        meta.setLore(toAdd);
        item.setItemMeta(meta);
        return this;
    }

    /**
     * Remove a specific line
     * on the current item lore,
     * remove it by the index.
     *
     * @param index index to be
     *              removed.
     * @return {@link ItemBuilder}
     */
    public ItemBuilder removeLore(int index) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasLore()) return this;

        List<String> toAdd = new ArrayList<>(meta.getLore());
        if (toAdd.size() < index) return this;
        toAdd.remove(index);

        meta.setLore(toAdd);
        item.setItemMeta(meta);
        return this;
    }

    /**
     * Add a custom enchantment
     * to item.
     *
     * @param enchantment the enchant.
     * @return {@link ItemBuilder}
     */
    public ItemBuilder withEnchantment(Enchantment enchantment) {
        return withEnchantment(enchantment, 1, false);
    }

    /**
     * Add a custom enchantment
     * to item.
     *
     * @param enchantment the enchant.
     * @param level enchant level.
     * @return {@link ItemBuilder}
     */
    public ItemBuilder withEnchantment(Enchantment enchantment, int level) {
        return withEnchantment(enchantment, level, false);
    }

    /**
     * Add a custom enchantment
     * to item.
     *
     * @param enchantment the enchant.
     * @param level enchant level.
     * @param unsafe add this enchantment
     *               as unsafe enchantment?
     * @return {@link ItemBuilder}
     */
    public ItemBuilder withEnchantment(Enchantment enchantment, int level, Boolean unsafe) {
        if (enchantment == null) return this;

        if (unsafe) {
            item.addUnsafeEnchantment(enchantment, level);
        } else {
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return this;
            meta.addEnchant(enchantment, level, true);
            item.setItemMeta(meta);
        }

        return this;
    }

    /**
     * Set this item glowing.
     *
     * @return {@link ItemBuilder}
     */
    public ItemBuilder setGlow() {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return this;

        meta.addEnchant(Enchantment.OXYGEN, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        item.setItemMeta(meta);
        return this;
    }

    /**
     * Set the skull owner.
     *
     * @param owner owner name.
     * @return {@link ItemBuilder}
     */
    public ItemBuilder setSkullOwner(String owner) {
        if (!StringUtils.contains(item.getType().toString(), "SKULL")) return this;
        if (owner == null || owner.isEmpty()) return this;

        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta == null) return this;

        meta.setOwner(owner);
        item.setItemMeta(meta);
        return this;
    }

    /**
     * Set a custom texture
     * for the skull.
     *
     * @param url skull url.
     * @return {@link ItemBuilder}
     */
    public ItemBuilder setCustomTexture(String url) {
        if (!StringUtils.contains(item.getType().toString(), "SKULL")) return this;
        if (url == null || url.isEmpty()) return this;

        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta == null) return this;

        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        profile.getProperties().put("textures", new Property("textures", url));

        try {
            Field field = meta.getClass().getDeclaredField("profile");
            field.setAccessible(true);
            field.set(meta, profile);
        } catch (IllegalArgumentException | NoSuchFieldException | SecurityException | IllegalAccessException ex) {
            ex.printStackTrace();
        }

        item.setItemMeta(meta);
        return this;
    }

    /**
     * Build the current itemStack
     * and replace the given placeholders.
     *
     * @param placeholder
     * @param replace
     * @return the built itemStack.
     */
    public ItemStack build(String[] placeholder, String[] replace) {
        ItemStack item = this.item;
        ItemMeta meta = item.getItemMeta();

        if (meta.hasLore()) {
            withLore(meta.getLore(), placeholder, replace);
        }
        return this.item;
    }

    /**
     * Build the current
     * itemStack.
     *
     * @return the built itemStack.
     */
    public ItemStack build() {
        return item;
    }

    public ItemBuilder clone() {
        return new ItemBuilder(this.item.clone());
    }

    /**
     * Create a new ItemBuilder
     * object.
     *
     * @param item material type to
     *             build.
     * @return {@link ItemBuilder}
     */
    public static ItemBuilder get(final String item) {
        return new ItemBuilder(getMaterial(item));
    }

    /**
     * Builds a new ItemBuilder
     * object by the configuration
     * file item.
     *
     * @param file file to get the
     *             item information.
     * @param where the file path to
     *              the item information.
     * @return {@link ItemBuilder}
     */
    public static ItemBuilder get(FileConfiguration file, final String where) {
        return get(file, where, null, null, null);
    }

    /**
     * Builds a new {@link ItemBuilder}
     * object by the configuration
     * file item.
     *
     * @param file file to get the
     *             item information.
     * @param where the file path to
     *              the item information.
     * @param placeholders the custom placeholders.
     * @param replacers the placeholder
     *                  replacers.
     * @return {@link ItemBuilder}
     */
    public static ItemBuilder get(FileConfiguration file, final String where, String[] placeholders, String[] replacers, String playerName) {
        ItemBuilder ret = null;

        try {
            String type = StringUtils.replace(file.getString(where + ".type"), " ", "").toUpperCase();
            int amount = file.contains(where + ".amount") ? file.getInt(where + ".amount") : 1;

            if (StringUtils.contains(type, ":")) {
                String[] typeSplit = type.split(":");
                short durability = Short.parseShort(typeSplit[1]);

                ret = new ItemBuilder(getMaterial(typeSplit[0]), amount, durability);
            } else {
                ret = new ItemBuilder(getMaterial(type), amount);
            }

            if (file.contains(where + ".owner")) {
                String owner = file.getString(where + ".owner");

                if (owner.length() <= 17) {
                    ret.setSkullOwner(StringUtils.replace(owner, "%player_name%", playerName == null ? "" : playerName));
                } else {
                    ret.setCustomTexture(owner);
                }
            }

            if (file.contains(where + ".name")) {
                String name = ChatColor.translateAlternateColorCodes('&', file.getString(where + ".name"));
                ret.withName(applyPlaceholder(name, placeholders, replacers));
            }

            if (file.contains(where + ".lore")) {
                ret.withLore(file.getStringList(where + ".lore"), placeholders, replacers);
            }

            if (file.contains(where + ".glow") && file.getString(where + ".glow").equalsIgnoreCase("true")) {
                ret.setGlow();
            } else if (file.contains(where + ".enchants")) {
                for (String str : file.getStringList(where + ".enchants")) {
                    String enchantment = StringUtils.replace(str, " ", "");

                    try {
                        if (StringUtils.contains(enchantment, ",")) {
                            String[] enchantmentSplit = enchantment.split(",");
                            ret.withEnchantment(Enchantment.getByName(enchantmentSplit[0]), Integer.parseInt(enchantmentSplit[1]));
                        } else {
                            ret.withEnchantment(Enchantment.getByName(enchantment));
                        }
                    } catch (IndexOutOfBoundsException | NumberFormatException ex) {
                        // Could not found this enchantment.
                    }
                }
            }
        } catch (NullPointerException | NumberFormatException | IndexOutOfBoundsException ex) {
            ex.printStackTrace();
        }

        return ret;
    }

    public static Material getMaterial(String type) {
        Material ret = null;

        if (type == null || type.isEmpty()) return null;

        try {
            int value = Integer.parseInt(type);
            ret = getMaterial(value);
        } catch (Exception ex) {
            ret = Material.getMaterial(type.toUpperCase());
        }

        return ret;
    }

    private static Material getMaterial(int type) {
        return byId.length > type && type >= 0 ? byId[type] : null;
    }

    private static String applyPlaceholder(String text, String[] placeholders, String[] replacers) {
        if (text == null || text.isEmpty()) return text;

        if (placeholders != null && placeholders.length > 0 && placeholders.length == replacers.length) {
            text = StringUtils.replaceEach(text, placeholders, replacers);
        }

        return text;
    }

    /**
     * This method will compare
     * two strings.
     * If this String has some
     * placeholders, @COMPARE_WITH_PLACEHOLDERS
     * will remove and then compare it.
     *
     * @param one This is the first String
     *            this String should be
     *            the default String.
     *            example: String str = Hello %player_name%.
     * @param two This is the second String
     *            this should be the
     *            replaced string.
     *            example: String str = Hello SkyVox.
     * @return If the Strings are equals.
     */
    private static Boolean isStringEquals(String one, String two) {
        if (one == null && two == null) return true;
        if (one == null || two == null) return false;
        if (StringUtils.equalsIgnoreCase(one, two)) return true;

        String regex = COMPARE_WITH_PLACEHOLDERS.matcher(ChatColor.translateAlternateColorCodes('&', one)).replaceAll("(.*)");
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(two).matches();
    }
}