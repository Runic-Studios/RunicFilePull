package com.runicrealms.plugin;

import com.runicrealms.plugin.command.admin.FilePullCommand;
import com.runicrealms.plugin.ui.FilePullUI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class RunicFilePull extends JavaPlugin {

    public static String SECRET; // Secret x-hub-signature key that we use for salting the sha256 hash to verify our webhook requests are from github
    public static String AUTH_TOKEN; // github auth token for repository (user specific, comes from @RunicRealmsGithub)
    public static String REPO_PATH; // repository path
    public static String BRANCH;
    private static RunicFilePull runicFilePull;

    public static RunicFilePull getInstance() {
        return runicFilePull;
    }

    private void initializeFolderStates() {
        for (FilePullFolder filePullFolder : FilePullFolder.values()) {
            FilePullOperation.setFolderEnabled(filePullFolder, this.getConfig().getBoolean("folder-enabled." + filePullFolder.getGitHubPath()));
        }
        REPO_PATH = this.getConfig().getString("repo-path");
        SECRET = this.getConfig().getString("webhook-secret");
        AUTH_TOKEN = this.getConfig().getString("api-token");
        BRANCH = this.getConfig().getString("branch");
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

        if(this.getConfig().getBoolean("automatic-sync.enabled")) {
            int port;
            try {
                port = Integer.parseInt(this.getConfig().getString("automatic-sync.port"));
            } catch (NumberFormatException | NullPointerException ignored) {
                port = 25570;
            }

            final int sparkPort = port;

            Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                FileSync.startWebhook(sparkPort);
            });
        }
    }
}
