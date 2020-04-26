package com.runicrealms.runicfilepull;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class FilePullGui implements Listener {

    private static Set<UUID> viewers = new HashSet<UUID>();

    public static void open(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 18, "File Pull");
        inventory.setItem(0, FilePull.isFolderEnabled(FilePullFolder.MOBS) ?
                buildIcon(Material.GREEN_STAINED_GLASS, "&6Mobs: &r&2&lENABLED", new String[] {"&7Click to disabled"}) :
                buildIcon(Material.RED_STAINED_GLASS, "&6Mobs: &r&c&lDISABLED", new String[] {"&7Click to enable"}));
        inventory.setItem(2, FilePull.isFolderEnabled(FilePullFolder.SKILLS) ?
                buildIcon(Material.GREEN_STAINED_GLASS, "&6Skills: &r&2&lENABLED", new String[] {"&7Click to disabled"}) :
                buildIcon(Material.RED_STAINED_GLASS, "&6Skills: &r&c&lDISABLED", new String[] {"&7Click to enable"}));
        inventory.setItem(4, FilePull.isFolderEnabled(FilePullFolder.SPAWNERS) ?
                buildIcon(Material.GREEN_STAINED_GLASS, "&6Spawners: &r&2&lENABLED", new String[] {"&7Click to disabled"}) :
                buildIcon(Material.RED_STAINED_GLASS, "&6Spawners: &r&c&lDISABLED", new String[] {"&7Click to enable"}));
        inventory.setItem(6, FilePull.isFolderEnabled(FilePullFolder.ITEMS) ?
                buildIcon(Material.GREEN_STAINED_GLASS, "&6Items: &r&2&lENABLED", new String[] {"&7Click to disabled"}) :
                buildIcon(Material.RED_STAINED_GLASS, "&6Items: &r&c&lDISABLED", new String[] {"&7Click to enable"}));
        inventory.setItem(8, FilePull.isFolderEnabled(FilePullFolder.QUESTS) ?
                buildIcon(Material.GREEN_STAINED_GLASS, "&6Quests: &r&2&lENABLED", new String[] {"&7Click to disabled"}) :
                buildIcon(Material.RED_STAINED_GLASS, "&6Quests: &r&c&lDISABLED", new String[] {"&7Click to enable"}));
        inventory.setItem(13, buildIcon(Material.SLIME_BALL, "&aStart File Pull", new String[] {}));
        viewers.add(player.getUniqueId());
        player.openInventory(inventory);
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            if (viewers.contains(event.getWhoClicked().getUniqueId())) {
                if (event.getRawSlot() < event.getInventory().getSize()) {
                    if (event.getSlot() == 0) {
                        boolean toggled = FilePull.isFolderEnabled(FilePullFolder.MOBS) ? false : true;
                        FilePull.setFolderEnabled(FilePullFolder.MOBS, toggled);
                        Plugin.getInstance().getConfig().set("mobs", toggled);
                        Plugin.getInstance().saveConfig();
                        event.getWhoClicked().closeInventory();
                        open((Player) event.getWhoClicked());
                    } else if (event.getSlot() == 2) {
                        boolean toggled = FilePull.isFolderEnabled(FilePullFolder.SKILLS) ? false : true;
                        FilePull.setFolderEnabled(FilePullFolder.SKILLS, toggled);
                        Plugin.getInstance().getConfig().set("skills", toggled);
                        Plugin.getInstance().saveConfig();
                        event.getWhoClicked().closeInventory();
                        open((Player) event.getWhoClicked());
                    } else if (event.getSlot() == 4) {
                        boolean toggled = FilePull.isFolderEnabled(FilePullFolder.SPAWNERS) ? false : true;
                        FilePull.setFolderEnabled(FilePullFolder.SPAWNERS, toggled);
                        Plugin.getInstance().getConfig().set("spawners", toggled);
                        Plugin.getInstance().saveConfig();
                        event.getWhoClicked().closeInventory();
                        open((Player) event.getWhoClicked());
                    } else if (event.getSlot() == 6) {
                        boolean toggled = FilePull.isFolderEnabled(FilePullFolder.ITEMS) ? false : true;
                        FilePull.setFolderEnabled(FilePullFolder.ITEMS, toggled);
                        Plugin.getInstance().getConfig().set("items", toggled);
                        Plugin.getInstance().saveConfig();
                        event.getWhoClicked().closeInventory();
                        open((Player) event.getWhoClicked());
                    } else if (event.getSlot() == 8) {
                        boolean toggled = FilePull.isFolderEnabled(FilePullFolder.QUESTS) ? false : true;
                        FilePull.setFolderEnabled(FilePullFolder.QUESTS, toggled);
                        Plugin.getInstance().getConfig().set("quests", toggled);
                        Plugin.getInstance().saveConfig();
                        event.getWhoClicked().closeInventory();
                        open((Player) event.getWhoClicked());
                    } else if (event.getSlot() == 13) {
                        if (FilePull.isFolderEnabled(FilePullFolder.MOBS) ||
                                FilePull.isFolderEnabled(FilePullFolder.SKILLS) ||
                                FilePull.isFolderEnabled(FilePullFolder.SPAWNERS) ||
                                FilePull.isFolderEnabled(FilePullFolder.ITEMS) ||
                                FilePull.isFolderEnabled(FilePullFolder.QUESTS)) {
                            event.getWhoClicked().closeInventory();
                            FilePull.startFilePull((Player) event.getWhoClicked());
                        }
                    }
                }
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (viewers.contains(event.getPlayer().getUniqueId())) {
            viewers.remove(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (viewers.contains(event.getPlayer().getUniqueId())) {
            viewers.remove(event.getPlayer().getUniqueId());
        }
    }

    private static ItemStack buildIcon(Material material, String name, String[] lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        List<String> loreList = new ArrayList<String>();
        for (String line : lore) {
            loreList.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(loreList);
        item.setItemMeta(meta);
        return item;
    }

}