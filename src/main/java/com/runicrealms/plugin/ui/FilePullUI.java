package com.runicrealms.plugin.ui;

import com.runicrealms.plugin.FilePullFolder;
import com.runicrealms.plugin.FilePullOperation;
import com.runicrealms.plugin.RunicFilePull;
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

public class FilePullUI implements Listener {

    private static final int START_ITEM_SLOT = 49;
    private static final String TITLE = ChatColor.GREEN + "File Pull - Sync Tool";
    private static final Set<UUID> viewers = new HashSet<>();

    /**
     * @param player to display inventory to
     */
    public static void open(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 54, TITLE);

        // place all folders in ui
        for (FilePullFolder filePullFolder : FilePullFolder.values()) {
            inventory.setItem(filePullFolder.getInventorySlot(), createIconFromStatus(filePullFolder));
        }

        inventory.setItem(START_ITEM_SLOT, buildIcon(Material.SLIME_BALL, "&aStart File Pull", new String[]{}));
        viewers.add(player.getUniqueId());
        player.openInventory(inventory);
    }

    /**
     * Updates the visual display of file folder according to its sync status
     *
     * @param filePullFolder to grab status from
     * @return an item for the ui
     */
    private static ItemStack createIconFromStatus(FilePullFolder filePullFolder) {
        ItemStack itemStack;
        if (FilePullOperation.isFolderEnabled(filePullFolder)) {
            itemStack = buildIcon(Material.GREEN_STAINED_GLASS, "&6" + filePullFolder.getGitHubPath() + ": &r&2&lENABLED", new String[]{"&7Click to disable"});
        } else {
            itemStack = buildIcon(Material.RED_STAINED_GLASS, "&6" + filePullFolder.getGitHubPath() + ": &r&c&lDISABLED", new String[]{"&7Click to enable"});
        }
        return itemStack;
    }

    /**
     * @param material of the ui item
     * @param name     of the ui item
     * @param lore     to set for the ui item
     * @return an item to display
     */
    private static ItemStack buildIcon(Material material, String name, String[] lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        List<String> loreList = new ArrayList<>();
        for (String line : lore) {
            loreList.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(loreList);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!viewers.contains(event.getWhoClicked().getUniqueId())) return;
        if (event.getRawSlot() > event.getInventory().getSize()) return;
        event.setCancelled(true);

        // Find corresponding folder and toggle its status
        for (FilePullFolder filePullFolder : FilePullFolder.values()) {
            if (event.getRawSlot() != filePullFolder.getInventorySlot()) continue;
            toggleFolderStatus(filePullFolder);
            event.getInventory().setItem(filePullFolder.getInventorySlot(), createIconFromStatus(filePullFolder));
        }

        // Begin sync if button pressed & any files are enabled
        if (event.getSlot() == START_ITEM_SLOT) {
            boolean hasFilesToSync = false;
            for (FilePullFolder filePullFolder : FilePullFolder.values()) {
                if (FilePullOperation.isFolderEnabled(filePullFolder)) {
                    hasFilesToSync = true;
                    break;
                }
            }
            if (hasFilesToSync) {
                event.getWhoClicked().closeInventory();
                FilePullOperation.startFilePull((Player) event.getWhoClicked());
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        viewers.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        viewers.remove(event.getPlayer().getUniqueId());
    }

    /**
     * Toggles the status of the given folder between enabled / disabled
     *
     * @param filePullFolder to toggle
     */
    private void toggleFolderStatus(FilePullFolder filePullFolder) {
        boolean toggled = !FilePullOperation.isFolderEnabled(filePullFolder);
        FilePullOperation.setFolderEnabled(filePullFolder, toggled);
        Bukkit.getScheduler().runTaskAsynchronously(RunicFilePull.getInstance(), () -> {
            RunicFilePull.getInstance().getConfig().set(filePullFolder.getGitHubPath(), toggled);
            RunicFilePull.getInstance().saveConfig();
        });
    }

}
