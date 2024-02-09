package com.runicrealms.plugin.filepull.target;

import com.runicrealms.plugin.filepull.FileUtils;
import com.runicrealms.plugin.filepull.RunicFilePull;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class ZipTarget implements Target {
//    SPAWNERS(Material.ZOMBIE_SPAWN_EGG, "MythicMobs/Spawners", "spawners-compressed.zip", "spawners-temp.zip", "spawners-zip", true);

    private static final AtomicInteger ZIP_FILE_TEMP_COUNTER = new AtomicInteger(0);

    private final Material material;
    private final String localFolderPath;
    private final String githubFilePath;
    private final String localTempPath;
    private final String identifier;
    private final boolean adminsOnly;

    private boolean enabled = false;

    public ZipTarget(Material material, String localFolderPath, String githubFilePath, String identifier, boolean adminsOnly) {
        this.material = material;
        this.localFolderPath = localFolderPath;
        this.githubFilePath = githubFilePath;
        this.localTempPath = "fp-zip-temp-" + ZIP_FILE_TEMP_COUNTER.getAndIncrement() + ".zip";
        this.identifier = identifier;
        this.adminsOnly = adminsOnly;
    }

    @Override
    public int pull(Consumer<Integer> onProgressUpdate) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(RunicFilePull.getInstance(), () -> {
            try {
                JSONObject commit = (JSONObject) (new JSONParser()).parse(FileUtils.getWithAuth(
                        "https://api.github.com/repos/"
                                + RunicFilePull.getInstance().getFileConfig().getTargetRepo()
                                + "/contents/"
                                + githubFilePath
                                + "?ref=" + RunicFilePull.getInstance().getFileConfig().getTargetBranch(),
                        RunicFilePull.GH_AUTH_TOKEN));
                File destination = new File(RunicFilePull.getInstance().getDataFolder(), localTempPath);
                if (destination.exists()) {
                    if (!destination.delete())
                        throw new IllegalStateException("Could not delete file " + localTempPath);
                }
                FileUtils.writeToFile(FileUtils.decodeBase64((String) commit.get("content")), destination);
                FileUtils.clearAndUnzipDirectory(destination, new File(RunicFilePull.getInstance().getDataFolder().getParentFile().getParent(), localFolderPath));
            } catch (Exception exception) {
                Bukkit.broadcastMessage(ChatColor.RED + "ERROR WITH FILEPULL downloading " + githubFilePath + ", please record this time and message excel!");
                exception.printStackTrace();
            } finally {
                onProgressUpdate.accept(1);
            }
        }, 1);
        return 1;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String getIdentifier() {
        return this.identifier;
    }

    @Override
    public void copyLocal(File githubLocalFolder) throws IOException {
        File local = new File(RunicFilePull.getInstance().getDataFolder(), "spawners-compressed.zip");
        if (local.exists()) local.delete();
//            if (!local.delete()) throw new IllegalStateException("Could not delete local " + identifier + " zip");
        FileUtils.zipDirectoryAndMove(
                new File(RunicFilePull.getInstance().getDataFolder().getParentFile().getParent(), localFolderPath),
                local,
                githubLocalFolder);
//        if (!local.delete())
//            throw new IllegalStateException("Copy local " + identifier + " could not delete local " + local.getName());
    }

    @Override
    public boolean adminsOnly() {
        return this.adminsOnly;
    }

    @Override
    public Material getIconMaterial() {
        return this.material;
    }

    @Override
    public String getGitHubPath() {
        return this.githubFilePath;
    }

    @Override
    public String getLocalPath() {
        return this.localFolderPath;
    }

}
