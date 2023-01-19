package com.runicrealms.plugin;

import com.runicrealms.plugin.command.admin.FilePullCommand;
import com.runicrealms.plugin.ui.FilePullUI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class RunicFilePull extends JavaPlugin {

    public static final String SECRET = "TheSkyIsFallin8392"; // Secret x-hub-signature key that we use for salting the sha256 hash to verify our webhook requests are from github
    public static final String AUTH_TOKEN = "47a7bcf0b66a78f709f7b39d416f78ef092f9564"; // github auth token for repository (user specific, comes from @RunicRealmsGithub)
    public static final String REPO_PATH = "Skyfallin/writer-files"; // repository path

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

        Bukkit.getScheduler().runTaskAsynchronously(this, FileSync::startWebhook);
    }
}
