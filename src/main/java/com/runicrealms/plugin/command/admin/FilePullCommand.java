package com.runicrealms.plugin.command.admin;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import com.runicrealms.plugin.ui.FilePullUI;
import org.bukkit.entity.Player;

@CommandAlias("filepull|fp")
@CommandPermission("runic.op")
public class FilePullCommand extends BaseCommand {

    @Default
    @CatchUnknown
    public void onCommand(Player player) {
        FilePullUI.open(player);
    }

}
