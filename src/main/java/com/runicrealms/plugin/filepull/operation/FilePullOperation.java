package com.runicrealms.plugin.filepull.operation;

import com.runicrealms.plugin.common.util.Pair;
import com.runicrealms.plugin.filepull.RunicFilePull;
import com.runicrealms.plugin.filepull.target.Target;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A sync/transfer operation to retrieve yaml files from GitHub
 */
public class FilePullOperation {

    private final Map<Target, Pair<AtomicInteger, Integer>> filesCount = new ConcurrentHashMap<>();

    /**
     * Begins the file sync task from git
     */
    public FilePullOperation(Runnable onComplete) {
        Bukkit.broadcastMessage(ChatColor.GREEN + "File Pull Initiated - Server Will Restart On Completion");
        Bukkit.broadcastMessage(ChatColor.DARK_GREEN + "WARNING - Mobs and quests may break while File Pull is running!");

        for (Target destination : RunicFilePull.getInstance().getFileConfig().getTargets()) {
            if (destination.isEnabled()) {
                Bukkit.getScheduler().runTaskAsynchronously(RunicFilePull.getInstance(), () -> {
                    try {
                        filesCount.put(destination, new Pair<>(new AtomicInteger(0), destination.pull(completed -> filesCount.get(destination).first.set(completed))));
                    } catch (Exception exception) {
                        Bukkit.broadcastMessage(ChatColor.RED + "There was an issue downloading the " + destination.getIdentifier() + "! Check the console for more information. Files are likely missing.");
                        Bukkit.broadcastMessage(ChatColor.DARK_RED + "Error message: " + exception.getMessage());
                        exception.printStackTrace();
                        onComplete.run();
                    }
                });
            }
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                int downloaded = filesCount.values().stream().mapToInt(pair -> pair.first.get()).sum();
                int needed = filesCount.values().stream().mapToInt(pair -> pair.second).sum();
                if (downloaded >= needed) {
                    Bukkit.getOnlinePlayers().forEach(player -> player.sendTitle("", ChatColor.GREEN + "Done!", 0, 40, 0));
                    Bukkit.broadcastMessage(ChatColor.GREEN + "Finished file pull! Type /rstop or /mm reload to apply your changes.");
                    onComplete.run();
                    this.cancel();
                } else {
                    double percentage = Math.round(((100.0 * downloaded) / needed) * 10.0) / 10.0;
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.sendTitle("", ChatColor.GREEN + "File Pull: " + ChatColor.YELLOW + percentage + "%", 0, 40, 0);
                    }
                }
            }
        }.runTaskTimerAsynchronously(RunicFilePull.getInstance(), 20L, 5L);
    }

}
