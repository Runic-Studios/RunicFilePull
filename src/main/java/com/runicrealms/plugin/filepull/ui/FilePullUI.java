package com.runicrealms.plugin.filepull.ui;

import com.runicrealms.plugin.filepull.target.Target;
import com.runicrealms.plugin.filepull.operation.FilePullOperation;
import com.runicrealms.plugin.filepull.RunicFilePull;
import com.runicrealms.plugin.common.util.ColorUtil;
import com.runicrealms.plugin.common.util.GUIUtil;
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
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class FilePullUI implements Listener {

    private static final int START_ITEM_SLOT = 49;
    private static final String TITLE = ChatColor.GREEN + "File Pull - Sync Tool";
    private static final Set<UUID> viewers = new HashSet<>();

    private static final Map<Integer, Target> destinations = new HashMap<>();

    private static @Nullable FilePullOperation currentOperation;

    /**
     * @param player to display inventory to
     */
    public static void open(Player player) {
        if (destinations.isEmpty()) {
            int slot = 9;
            for (Target destination : RunicFilePull.getInstance().getFileConfig().getTargets()) {
                destinations.put(slot++, destination);
            }
        }

        Inventory inventory = Bukkit.createInventory(null, 54, TITLE);

        // place all folders in ui
        for (int slot : destinations.keySet()) {
            inventory.setItem(slot, buildIcon(destinations.get(slot)));
        }

        inventory.setItem(START_ITEM_SLOT, GUIUtil.dispItem(Material.SLIME_BALL, "&aStart File Pull"));
        viewers.add(player.getUniqueId());
        player.openInventory(inventory);
    }

    private static ItemStack buildIcon(Target destination) {
        ItemStack item = new ItemStack(destination.isEnabled() ? Material.CYAN_STAINED_GLASS_PANE : destination.getIconMaterial());
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ColorUtil.format(
                "&6" + destination.getIdentifier() + ": &r" + (destination.isEnabled() ? "&2&lENABLED" : "&c&lDISABLED")
        ));
        List<String> lore = destination.adminsOnly() ? new ArrayList<>(List.of(
                ColorUtil.format("&4&lWARNING:"),
                ColorUtil.format("&conly admins should perform this!"))) : new ArrayList<>();
        if (destination.isEnabled()) lore.add(ColorUtil.format("&7Click to disable"));
        else lore.add(ColorUtil.format("&7Click to enable"));
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
        meta.setLore(lore);
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

        if (destinations.containsKey(event.getSlot())) {
            Target destination = destinations.get(event.getSlot());
            toggleDestinationStatus(destination);
            event.getInventory().setItem(event.getSlot(), buildIcon(destination));
        }

        // Begin sync if button pressed & any files are enabled
        if (event.getSlot() == START_ITEM_SLOT) {
            if (RunicFilePull.getInstance().getFileConfig().getTargets().stream().anyMatch(Target::isEnabled)) {
                event.getWhoClicked().closeInventory();
                if (currentOperation != null) {
                    event.getWhoClicked().sendMessage(ChatColor.RED + "Filepull is already running!");
                    return;
                }
                currentOperation = new FilePullOperation(() -> currentOperation = null);
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
     * @param destination to toggle
     */
    private void toggleDestinationStatus(Target destination) {
        boolean toggled = !destination.isEnabled();
        destination.setEnabled(toggled);
        Bukkit.getScheduler().runTaskAsynchronously(RunicFilePull.getInstance(), () -> {
            RunicFilePull.getInstance().getConfig().set("destination-enabled." + destination.getIdentifier(), toggled);
            RunicFilePull.getInstance().saveConfig();
        });
    }

}
