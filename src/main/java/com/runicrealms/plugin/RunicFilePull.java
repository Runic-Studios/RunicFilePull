package com.runicrealms.plugin;

import com.runicrealms.plugin.command.admin.FilePullCommand;
import com.runicrealms.plugin.ui.FilePullUI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class RunicFilePull extends JavaPlugin {

    private static RunicFilePull runicFilePull;

    public static RunicFilePull getInstance() {
        return runicFilePull;
    }

    private void initializeFolderStates() {
        for (FilePullFolder filePullFolder : FilePullFolder.values()) {
            FilePullOperation.setFolderEnabled(filePullFolder, this.getConfig().getBoolean(filePullFolder.getGitHubPath()));
        }
    }

    @Override
    public void onEnable() {
        runicFilePull = this;
        this.getConfig().options().copyDefaults(true);
        this.saveConfig();
        initializeFolderStates();
        Bukkit.getPluginManager().registerEvents(new FilePullUI(), this);
        FilePullCommand command = new FilePullCommand();
        Bukkit.getPluginCommand("filepull").setExecutor(command);
        Bukkit.getPluginCommand("pull").setExecutor(command);
        Bukkit.getPluginCommand("fp").setExecutor(command);

        FileSync.startWebhook();
    }
}
