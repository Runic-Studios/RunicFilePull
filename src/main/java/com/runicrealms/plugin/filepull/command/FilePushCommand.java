package com.runicrealms.plugin.filepull.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import com.runicrealms.plugin.filepull.operation.FilePushOperation;
import com.runicrealms.plugin.filepull.RunicFilePull;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@CommandAlias("filepush")
@CommandPermission("runic.op")
public class FilePushCommand extends BaseCommand {

    private final Set<UUID> confirming = new HashSet<>();

    private @Nullable FilePushOperation currentOperation;

    @Default
    @CatchUnknown
    public void onCommand(Player player) {
        if (confirming.contains(player.getUniqueId())) {
            if (currentOperation != null) {
                player.sendMessage(ChatColor.RED + "Cannot initiate a filepush while one is already active!");
            } else if (RunicFilePull.getInstance().getWebhook().isListening()) {
                player.sendMessage(ChatColor.RED + "Cannot run filepush while filesync is active! Type /filesync to disable it temporarily.");
            } else {
                currentOperation = new FilePushOperation(() -> {
                    currentOperation = null;
                    Bukkit.broadcastMessage(ChatColor.GREEN + "Don't forget to wait a minute before re-enabling filesync with /filesync!");
                });
            }
            confirming.remove(player.getUniqueId());
            return;
        }
        player.sendMessage(ChatColor.RED + "WARNING: this will initiate a filepush including all local file changes and sync it with the github remote.");
        player.sendMessage(ChatColor.DARK_RED.toString() + ChatColor.BOLD + "Do not do this unless you are absolutely sure what you are doing!");
        player.sendMessage(ChatColor.RED + "Type /filepush again to confirm or /filepush cancel to cancel.");
        confirming.add(player.getUniqueId());
    }

    @Subcommand("cancel")
    public void onCommandCancel(Player player) {
        if (!confirming.contains(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Nothing to cancel!");
            return;
        }
        confirming.remove(player.getUniqueId());
        player.sendMessage(ChatColor.GREEN + "Canceled filepush!");
    }

}
