package com.runicrealms.plugin;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.nio.file.Files;
import java.util.Objects;
import java.util.function.Consumer;

public enum FilePullFolder implements FilePullDestination {

    MOBS(Material.ZOMBIE_HEAD, "MythicMobs/Mobs", "mobs", "mobs", false),
    SKILLS(Material.SKELETON_SKULL, "MythicMobs/Skills", "skills", "skills", false),
    ITEMS(Material.GOLD_NUGGET, "RunicItems/items", "runicitems", "items", false), // TODO change githubpath to just items
    QUESTS(Material.PAPER, "RunicQuests/quests", "quests", "quests", false),
    LOOT_CHEST_TEMPLATES(Material.ENDER_CHEST, "RunicCore/loot/chest-templates", "loot/chest-templates", "loot-chest-templates", false),
    LOOT_TABLES(Material.ENDER_CHEST, "RunicCore/loot/loot-tables", "loot/loot-tables", "loot-tables", false),
    LOOT_TIMED(Material.ENDER_CHEST, "RunicCore/loot/timed-loot", "loot/timed-loot", "timed-loot", false),
    FIELD_BOSSES(Material.TNT, "RunicCore/field-bosses", "field-bosses", "field-bosses", false),
    SHOPS(Material.EMERALD, "RunicCore/shops", "shops", "shops", false),
    SPELLS(Material.POPPED_CHORUS_FRUIT, "RunicCore/spells", "spells", "spells", false),
    WORKSTATIONS(Material.ANVIL, "RunicProfessions/workstations", "workstations", "workstations", false),
    GUILDS(Material.SHIELD, "RunicGuilds", "guilds", "guilds-configs", false),
    DOORS(Material.OAK_DOOR, "RunicDoors", "doors", "doors", true);

    private final Material material;
    private final String localPath;
    private final String githubPath;
    private final String identifier;
    private final boolean adminsOnly;

    private boolean enabled;

    FilePullFolder(Material material, String localPath, String githubPath, String identifier, boolean adminsOnly) {
        this.material = material;
        this.localPath = localPath;
        this.githubPath = githubPath;
        this.identifier = identifier;
        this.adminsOnly = adminsOnly;
    }

    public String getLocalPath() {
        return this.localPath;
    }

    public String getGithubPath() {
        return this.githubPath;
    }

    @Override
    public int pull(Consumer<Integer> onProgressUpdate) throws Exception {
        JSONObject commit = (JSONObject) (new JSONParser()).parse(FileUtils.getWithAuth("https://api.github.com/repos/" + RunicFilePull.REPO_PATH + "/branches/" + RunicFilePull.BRANCH, RunicFilePull.AUTH_TOKEN));
        String treeURL = (String) ((JSONObject) ((JSONObject) ((JSONObject) commit.get("commit")).get("commit")).get("tree")).get("url");
        if (githubPath.contains("/")) treeURL += "?recursive=1";
        JSONArray tree = (JSONArray) ((JSONObject) (new JSONParser()).parse(FileUtils.getWithAuth(treeURL, RunicFilePull.AUTH_TOKEN))).get("tree");
        JSONArray files = null;
        for (Object object : tree.toArray()) {
            JSONObject treeObject = (JSONObject) object;
            if (((String) treeObject.get("path")).equalsIgnoreCase(githubPath)) {
                String folderURL = (String) treeObject.get("url");
                JSONObject folderJSON = (JSONObject) (new JSONParser()).parse(FileUtils.getWithAuth(folderURL, RunicFilePull.AUTH_TOKEN));
                files = (JSONArray) folderJSON.get("tree");
                break;
            }
        }
        if (files == null) {
            throw new IllegalStateException("There was an error loading the GitHub API, mass ping Excel!");
        }
        File destination = new File(RunicFilePull.getInstance().getDataFolder().getParent(), localPath);
        if (!destination.exists()) {
            if (!destination.mkdirs())
                throw new IllegalStateException("Could not create destination folder for " + this.identifier);
        }
        for (File file : Objects.requireNonNull(destination.listFiles())) {
            if (file.exists()) {
                if (!file.delete()) throw new IllegalStateException("Could not delete file " + file.getName());
            }
        }
        JSONArray finalFiles = files;
        Bukkit.getScheduler().runTaskLaterAsynchronously(RunicFilePull.getInstance(), () -> {
            int completed = 0;
            for (Object object : finalFiles.toArray()) {
                JSONObject jsonObject = ((JSONObject) object);
                if (jsonObject.get("type").equals("blob") && ((String) jsonObject.get("path")).endsWith(".yml")) {
                    try {
                        File localFile = new File(destination, (String) jsonObject.get("path"));
                        JSONObject gitJson = (JSONObject) (new JSONParser()).parse(FileUtils.getWithAuth((String) ((JSONObject) object).get("url"), RunicFilePull.AUTH_TOKEN));
                        FileUtils.writeBase64ToFile((String) gitJson.get("content"), localFile);
                    } catch (Exception exception) {
                        Bukkit.broadcastMessage(ChatColor.RED + "ERROR WITH FILEPULL downloading " + jsonObject.get("path") + ", please record this time and message excel!");
                    }
                    completed++;
                    onProgressUpdate.accept(completed);
                }
            }
        }, 1);
        return files.size();
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
        File copyDestFolder = new File(githubLocalFolder, githubPath);
        if (copyDestFolder.exists()) {
            for (File file : Objects.requireNonNull(copyDestFolder.listFiles())) {
                if (!file.delete())
                    throw new IllegalStateException("Copy local " + identifier + " could not delete " + file.getName());
            }
        }
        copyDestFolder.mkdirs();
        File origin = new File(RunicFilePull.getInstance().getDataFolder().getParent(), localPath);
        if (origin.exists()) {
            for (File file : Objects.requireNonNull(origin.listFiles())) {
                try {
                    Files.copy(file.toPath(), new File(copyDestFolder, file.getName()).toPath());
                } catch (Exception exception) {
                    throw new IllegalArgumentException("Copy local " + identifier + " could not copy file " + file.toPath());
                }
            }
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

}
