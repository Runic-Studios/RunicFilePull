package com.runicrealms.plugin.filepull;

import co.aikar.commands.PaperCommandManager;
import com.runicrealms.plugin.filepull.command.FilePullCommand;
import com.runicrealms.plugin.filepull.command.FilePushCommand;
import com.runicrealms.plugin.filepull.command.FileSyncCommand;
import com.runicrealms.plugin.filepull.target.Target;
import com.runicrealms.plugin.filepull.ui.FilePullUI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.logging.Level;

public class RunicFilePull extends JavaPlugin {

    public static String WEBHOOK_SECRET; // Secret x-hub-signature key that we use for salting the sha256 hash to verify our webhook requests are from github
    public static String GH_AUTH_TOKEN; // github auth token for repository (user specific, comes from @RunicRealmsGithub)

    private static RunicFilePull instance;

    private FilePullConfig config;
    private FileSyncWebhook webhook;

    public static RunicFilePull getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        PaperCommandManager commandManager = new PaperCommandManager(this);

        this.getConfig().options().copyDefaults(true);
        this.saveConfig();
        WEBHOOK_SECRET = this.getConfig().getString("webhook-secret");
        GH_AUTH_TOKEN = this.getConfig().getString("api-token");

        // Register commands
        Bukkit.getPluginManager().registerEvents(new FilePullUI(), this);
        commandManager.registerCommand(new FilePullCommand());
        commandManager.registerCommand(new FilePushCommand());
        commandManager.registerCommand(new FileSyncCommand());

        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            try {
                this.config = new FilePullConfig(
                        this.getConfig().getString("config.file-path"),
                        this.getConfig().getString("config.repo.path"),
                        this.getConfig().getString("config.repo.branch"));
            } catch (Exception exception) {
                Bukkit.getLogger().log(Level.SEVERE, "Error loading configuration for RunicFilePull! Disabling...");
                exception.printStackTrace();
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }

            // Set config toggled on/off
            for (Target target : this.config.getTargets()) {
                target.setEnabled(this.getConfig().getBoolean("destination-enabled." + target.getIdentifier(), false));
            }

            // Enable file sync
            boolean webhookActive = this.getConfig().getBoolean("automatic-sync.enabled", false);
            int port = -1;
            try {
                port = Integer.parseInt(Objects.requireNonNull(this.getConfig().getString("automatic-sync.port")));
            } catch (NumberFormatException | NullPointerException ignored) {
            }
            this.webhook = new FileSyncWebhook(webhookActive, port);
        });
    }

    public FilePullConfig getFileConfig() {
        return this.config;
    }

    public FileSyncWebhook getWebhook() {
        return this.webhook;
    }

}
