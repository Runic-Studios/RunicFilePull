package com.runicrealms.plugin.command.admin;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import com.runicrealms.plugin.RunicFilePull;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;

@CommandAlias("filesync")
@CommandPermission("runic.op")
public class FileSyncCommand extends BaseCommand {

    @Default
    @CatchUnknown
    public void onCommand(CommandSender sender) {
        boolean newState = !RunicFilePull.syncActive;
        sender.sendMessage(ChatColor.GREEN + "Turned filesync " + (newState ? "ON" : "OFF") + "! This will not persist through restarts.");
        RunicFilePull.syncActive = newState;
    }

}
