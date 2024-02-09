package com.runicrealms.plugin.filepull.target;

import com.runicrealms.plugin.filepull.FileUtils;
import com.runicrealms.plugin.filepull.RunicFilePull;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.nio.file.Files;
import java.util.function.Consumer;

public class FileTarget implements Target {

//    NPCS(Material.PLAYER_HEAD, "RunicNpcs/npcs.yml", "npcs.yml", "npcs", true),
//    SCRIPT_ITEMS(Material.COMMAND_BLOCK, "RunicItems/script-items.yml", "script-items.yml", "script-items", false),
//    REGIONS(Material.BARRIER, "WorldGuard/cache/profiles.sqlite", "world-guard-profiles.sqlite", "wg-regions", true),
//    WORKSTATIONS_LOCATIONS(Material.CRAFTING_TABLE, "RunicProfessions/workstations.yml", "workstations.yml", "workstations-locations", false),
//    RUNIC_RESTART_CONFIG(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "RunicRestart/config.yml", "runic-restart-config.yml", "runic-restart-config", false),
//    RUNIC_WARPS_CONFIG(Material.ENDER_PEARL, "RunicWarps/config.yml", "runic-warps-config.yml", "warps", true),
//    WEAPON_SKINS(Material.WOODEN_SWORD, "RunicItems/weapon-skins.yml", "weapon-skins.yml", "weapon-skins", false),
//    REGENERATIVE_CHESTS(Material.CHEST, "RunicCore/loot/regenerative-chests.yml", "loot/regenerative-chests.yml", "regenerative-chests", true),
//    HOLOGRAMS(Material.ARMOR_STAND, "HolographicDisplays/database.yml", "holograms-database.yml", "holograms", true);

    private final Material material;
    private final String localPath;
    private final String githubPath;
    private final String identifier;
    private final boolean adminsOnly;

    private boolean enabled = false;

    public FileTarget(Material material, String localPath, String githubPath, String identifier, boolean adminsOnly) {
        this.material = material;
        this.localPath = localPath;
        this.githubPath = githubPath;
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
                                + githubPath
                                + "?ref=" + RunicFilePull.getInstance().getFileConfig().getTargetBranch(),
                        RunicFilePull.GH_AUTH_TOKEN));
                File destination = new File(RunicFilePull.getInstance().getDataFolder().getParent(), localPath);
                if (destination.exists()) {
                    if (!destination.delete())
                        throw new IllegalStateException("Could not delete file " + this.localPath);
                }
                FileUtils.writeToFile(FileUtils.decodeBase64((String) commit.get("content")), destination);
            } catch (Exception exception) {
                Bukkit.broadcastMessage(ChatColor.RED + "ERROR WITH FILEPULL downloading " + this.localPath + ", please record this time and message excel!");
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
    public void copyLocal(File githubLocalFolder) {
        File destination = new File(githubLocalFolder, githubPath);
        if (destination.exists()) {
            if (!destination.delete())
                throw new IllegalStateException("Copy local " + identifier + " could not delete " + destination.getName());
        }
        File origin = new File(RunicFilePull.getInstance().getDataFolder().getParent(), localPath);
        try {
            Files.copy(origin.toPath(), destination.toPath());
        } catch (Exception exception) {
            exception.printStackTrace();
            throw new IllegalArgumentException("Copy local " + identifier + " could not copy file " + origin.toPath());
        }
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
    public String getLocalPath() {
        return this.localPath;
    }

    @Override
    public String getGitHubPath() {
        return this.githubPath;
    }

}
