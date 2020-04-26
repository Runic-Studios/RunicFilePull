package com.runicrealms.runicfilepull;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FilePullCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            FilePullGui.open((Player) sender);
        } else {
            sender.sendMessage("You cannot run this command from console!");
        }
        return true;
    }

}
