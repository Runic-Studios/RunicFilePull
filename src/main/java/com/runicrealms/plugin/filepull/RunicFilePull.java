package com.runicrealms.plugin.filepull;

import co.aikar.commands.PaperCommandManager;
import com.runicrealms.plugin.filepull.command.FilePullCommand;
import com.runicrealms.plugin.filepull.command.FilePushCommand;
import com.runicrealms.plugin.filepull.command.FileSyncCommand;
import com.runicrealms.plugin.filepull.ui.FilePullUI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class RunicFilePull extends JavaPlugin {

    public static final List<FilePullDestination> destinations = new LinkedList<>();
    public static String SECRET; // Secret x-hub-signature key that we use for salting the sha256 hash to verify our webhook requests are from github
    public static String AUTH_TOKEN; // github auth token for repository (user specific, comes from @RunicRealmsGithub)
    public static String REPO_PATH; // repository path
    public static String BRANCH;
    public static boolean syncActive = true; // changes during runtime
    private static RunicFilePull runicFilePull;

    public static RunicFilePull getInstance() {
        return runicFilePull;
    }

    @Override
    public void onEnable() {
        runicFilePull = this;

        PaperCommandManager commandManager = new PaperCommandManager(this);

        this.getConfig().options().copyDefaults(true);
        this.saveConfig();
        for (FilePullFolder filePullFolder : FilePullFolder.values()) {
            filePullFolder.setEnabled(this.getConfig().getBoolean("destination-enabled." + filePullFolder.getIdentifier()));
        }
        for (FilePullFile filePullFile : FilePullFile.values()) {
            filePullFile.setEnabled(this.getConfig().getBoolean("destination-enabled." + filePullFile.getIdentifier()));
        }
        for (FilePullZipFile filePullZipFile : FilePullZipFile.values()) {
            filePullZipFile.setEnabled(this.getConfig().getBoolean("destination-enabled." + filePullZipFile.getIdentifier()));
        }
        REPO_PATH = this.getConfig().getString("repo-path");
        SECRET = this.getConfig().getString("webhook-secret");
        AUTH_TOKEN = this.getConfig().getString("api-token");
        BRANCH = this.getConfig().getString("branch");

        destinations.addAll(Arrays.asList(FilePullFolder.values()));
        destinations.addAll(Arrays.asList(FilePullFile.values()));
        destinations.addAll(Arrays.asList(FilePullZipFile.values()));

        Bukkit.getPluginManager().registerEvents(new FilePullUI(), this);
        commandManager.registerCommand(new FilePullCommand());
        commandManager.registerCommand(new FilePushCommand());
        commandManager.registerCommand(new FileSyncCommand());

        if (this.getConfig().getBoolean("automatic-sync.enabled")) {
            int port;
            try {
                port = Integer.parseInt(Objects.requireNonNull(this.getConfig().getString("automatic-sync.port")));
            } catch (NumberFormatException | NullPointerException ignored) {
                port = 25570;
            }

            final int sparkPort = port;

            Bukkit.getScheduler().runTaskAsynchronously(this, () -> FileSync.startWebhook(sparkPort));
        }
    }
}
