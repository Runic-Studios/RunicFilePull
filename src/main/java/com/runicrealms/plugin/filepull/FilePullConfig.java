package com.runicrealms.plugin.filepull;

import com.runicrealms.plugin.filepull.target.FileTarget;
import com.runicrealms.plugin.filepull.target.FolderTarget;
import com.runicrealms.plugin.filepull.target.ZipTarget;
import com.runicrealms.plugin.filepull.target.Target;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class FilePullConfig {

    private final String repo;
    private final String branch;
    private final List<Target> targets;

    /**
     * Retrieves info from the GitHub repo about our config
     * Should be called async
     */
    public FilePullConfig(String configGitHubPath, String configRepo, String configBranch) throws Exception {
        // Load file-mappings target from remote github
        JSONObject commit = (JSONObject) (new JSONParser()).parse(FileUtils.getWithAuth(
                "https://api.github.com/repos/"
                        + configRepo
                        + "/contents/"
                        + configGitHubPath
                        + "?ref=" + configBranch
                , RunicFilePull.GH_AUTH_TOKEN));
        String fileContents = FileUtils.decodeBase64((String) commit.get("content"));
        FileConfiguration config = new YamlConfiguration();
        config.loadFromString(fileContents);

        this.repo = Objects.requireNonNull(config.getString("repo.path"));
        this.branch = Objects.requireNonNull(config.getString("repo.branch"));

        // Parse file-mappings.yml
        if (!config.contains("targets")) {
            this.targets = new LinkedList<>();
            return;
        }
        ConfigurationSection targetsSection = Objects.requireNonNull(config.getConfigurationSection("targets"));
        Set<String> targetsKeys = targetsSection.getKeys(false);
        this.targets = new ArrayList<>(targetsKeys.size());
        for (String targetIdentifier : targetsKeys) {
            ConfigurationSection targetSection = Objects.requireNonNull(targetsSection.getConfigurationSection(targetIdentifier));
            String type = Objects.requireNonNull(targetSection.getString("type"));
            String githubPath = Objects.requireNonNull(targetSection.getString("github"));
            String localPath = Objects.requireNonNull(targetSection.getString("local"));
            Material material = Objects.requireNonNull(Material.getMaterial(Objects.requireNonNull(targetSection.getString("icon.material"))));
            boolean adminsOnly = targetSection.getBoolean("icon.admins-only");
            switch (type) {
                case "file" -> this.targets.add(new FileTarget(material, localPath, githubPath, targetIdentifier, adminsOnly));
                case "folder" -> this.targets.add(new FolderTarget(material, localPath, githubPath, targetIdentifier, adminsOnly));
                case "zip" -> this.targets.add(new ZipTarget(material, localPath, githubPath, targetIdentifier, adminsOnly));
                default -> throw new IllegalArgumentException("Bad FP target type " + type + " for target identifier " + targetIdentifier);
            }
        }
    }

    public String getTargetRepo() {
        return this.repo;
    }

    public String getTargetBranch() {
        return this.branch;
    }

    public List<Target> getTargets() {
        return this.targets;
    }

}
