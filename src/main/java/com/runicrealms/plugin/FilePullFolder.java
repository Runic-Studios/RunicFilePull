package com.runicrealms.plugin;

public enum FilePullFolder {

    MOBS(2, "MythicMobs/Mobs", "mobs"),
    SKILLS(4, "MythicMobs/Skills", "skills"),
    RUNIC_ITEMS(6, "RunicItems/items", "runicitems"),
    SCRIPT_ITEMS(11, "RunicItems/script", "scriptitems"),
    MYTHIC_ITEMS(13, "MythicMobs/Items", "mythicitems"),
    QUESTS(15, "RunicQuests/quests", "quests"),
    SHOPS(20, "RunicCore/shops", "shops"),
    WORKSTATIONS(22, "RunicProfessions/workstations", "workstations");

    private final int inventorySlot;
    private final String localPath;
    private final String githubPath;

    /**
     * @param inventorySlot the slot in the ui menu
     * @param localPath     the path on the server where the files should be stored
     * @param githubPath    the folder name in the writer-files repo
     */
    FilePullFolder(int inventorySlot, String localPath, String githubPath) {
        this.inventorySlot = inventorySlot;
        this.localPath = localPath;
        this.githubPath = githubPath;
    }

    public String getGitHubPath() {
        return this.githubPath;
    }

    public int getInventorySlot() {
        return inventorySlot;
    }

    public String getLocalPath() {
        return this.localPath;
    }

    public static FilePullFolder getFromPath(String githubPath)  {
        for (FilePullFolder value : values()) {
            if (githubPath.startsWith(value.githubPath)) return value;
        }
        throw new IllegalArgumentException("Invalid github path " + githubPath);
    }

}
