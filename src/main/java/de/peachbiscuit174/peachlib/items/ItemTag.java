package de.peachbiscuit174.peachlib.items;


import de.peachbiscuit174.peachlib.PeachLib;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class ItemTag {
    private static final Plugin plugin = PeachLib.getPlugin();
    private static final Map<String, NamespacedKey> keyCache = new ConcurrentHashMap<>();

    private static NamespacedKey getKey(String key) {
        return keyCache.computeIfAbsent(key, k ->
                new NamespacedKey(plugin, k.toLowerCase().replace(" ", "_"))
        );
    }

    public static ItemStack setItemTag (ItemStack itemStack, String key) {
        if (itemStack != null && itemStack.getType().isItem()) {
            if (key != null) {
                if (!isItemTag(itemStack, key)) {
                    ItemMeta meta = itemStack.getItemMeta();
                    if (meta != null) {
                        NamespacedKey namespacedKey = getKey(key);
                        if (namespacedKey != null) {
                            meta.getPersistentDataContainer().set(namespacedKey, PersistentDataType.BOOLEAN, true);
                            itemStack.setItemMeta(meta);
                        }
                    }
                }
            }
        }
        return itemStack;
    }

    public static boolean isItemTag (ItemStack itemStack, String key) {
        boolean check = false;
        if (itemStack != null && itemStack.getType().isItem()) {
            if (key != null) {
                ItemMeta meta = itemStack.getItemMeta();
                if (meta != null) {
                    NamespacedKey namespacedKey = getKey(key);
                    if (namespacedKey != null) {
                        boolean value = meta.getPersistentDataContainer().has(namespacedKey, PersistentDataType.BOOLEAN);
                        if (value) {
                            check = true;
                        }
                    }
                }
            }
        }
        return check;
    }

    public static ItemStack removeItemTag(ItemStack itemStack, String key) {
        if (itemStack != null && itemStack.getType().isItem()) {
            if (key != null) {
                if (isItemTag(itemStack, key)) {
                    ItemMeta meta = itemStack.getItemMeta();
                    if (meta != null) {
                        NamespacedKey namespacedKey = getKey(key);
                        if (namespacedKey != null) {
                            meta.getPersistentDataContainer().remove(namespacedKey);
                            itemStack.setItemMeta(meta);
                        }
                    }
                }
            }
        }


        return itemStack;
    }
}
