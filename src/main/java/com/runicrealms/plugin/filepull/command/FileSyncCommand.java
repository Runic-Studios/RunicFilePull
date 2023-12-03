package com.runicrealms.plugin.filepull.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import com.runicrealms.plugin.filepull.RunicFilePull;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

@CommandAlias("filesync")
@CommandPermission("runic.op")
public class FileSyncCommand extends BaseCommand {

    @Default
    @CatchUnknown
    public void onCommand(CommandSender sender) {
        boolean newState = !RunicFilePull.syncActive;
        Bukkit.broadcastMessage(ChatColor.GREEN + "Turned filesync " + (newState ? "ON" : "OFF") + "! This will not persist through restarts.");
        RunicFilePull.syncActive = newState;
    }

}
