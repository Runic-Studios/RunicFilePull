package com.runicrealms.runicfilepull;

public enum FilePullFolder {

    MOBS("MythicMobs/Mobs", "mobs", "1bef0dba46c127ad026dbb6f974bbbdc18c269eb"),
    SKILLS("MythicMobs/Skills", "skills", "e88ff93caedf156bb9cd3bb1a28fbe823f55b100"),
    RUNIC_ITEMS("RunicItems/items", "runicitems", "0d470cc5dc91d73469e1f75d51d00bdb79543f3d"),
    ITEMS("MythicMobs/Items", "items", "f88faaa2cbf3bbdf9776ee281089c5c41d75e5df"),
    QUESTS("RunicQuests/quests", "quests", "14f09127b7cfe681f35a76eeed60dc6b5f9580e4");

    private final String localPath;
    private final String githubPath;
    private final String treeSha;

    FilePullFolder(String localPath, String githubPath, String treeSha) {
        this.localPath = localPath;
        this.githubPath = githubPath;
        this.treeSha = treeSha;
    }

    public String getLocalPath() {
        return this.localPath;
    }

    public String getGithubPath() {
        return this.githubPath;
    }

    public String getTreeSha() {
        return this.treeSha;
    }

}
