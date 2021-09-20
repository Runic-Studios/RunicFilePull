package com.runicrealms.runicfilepull;

public enum FilePullFolder {

//<<<<<<< HEAD
    MOBS("MythicMobs/Mobs", "mobs"),
    SKILLS("MythicMobs/Skills", "skills"),
    RUNIC_ITEMS("RunicItems/items", "runicitems"),
    SCRIPT_ITEMS("RunicItems/script", "scriptitems"),
    QUESTS("RunicQuests/quests", "quests");

    private final String localPath;
    private final String githubPath;

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
