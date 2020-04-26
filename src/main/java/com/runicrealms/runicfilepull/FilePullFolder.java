package com.runicrealms.runicfilepull;

public enum FilePullFolder {

    MOBS("MythicMobs/Mobs", "mobs"),
    SKILLS("MythicMobs/Skills", "skills"),
    SPAWNERS("MythicMobs/Spawners", "spawners"),
    ITEMS("MythicMobs/Items", "items"),
    QUESTS("RunicQuests/quests", "quests");

    private String localPath;
    private String githubPath;

    FilePullFolder(String localPath, String githubPath) {
        this.localPath = localPath;
        this.githubPath = githubPath;
    }

    public String getLocalPath() {
        return this.localPath;
    }

    public String getGithubPath() {
        return this.githubPath;
    }

}
