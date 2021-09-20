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
//=======
//    MOBS("MythicMobs/Mobs", "mobs"/*, "8add383254ac7c71b297a5224dd5eaaeabfbb221"*/),
//    SKILLS("MythicMobs/Skills", "skills"/*, "1e052537ca007ffe2e1f411d48ddc3a5f753d849"*/),
//    RUNIC_ITEMS("RunicItems/items", "runicitems"/*, "cb5ebc3bfcd037142dac7f98f7e14bc57b2f6e45"*/),
//    SCRIPT_ITEMS("RunicItems/script", "scriptitems"/*, "5913f0e8047087855f1dc9fd65e6a89a524eb7ef"*/),
//    QUESTS("RunicQuests/quests", "quests"/*, "9b90b5412a13b7a3c518518a3e7e0a56e6c52b53"*/);
//
//    private final String localPath;
//    private final String githubPath;
//    //private final String treeSha;
//
//    FilePullFolder(String localPath, String githubPath/*, String treeSha*/) {
//        this.localPath = localPath;
//        this.githubPath = githubPath;
//        //this.treeSha = treeSha;
//>>>>>>> 31f79f789b79cbe5980a6f0de9f5f3dbbca0cb77
    }

    public String getLocalPath() {
        return this.localPath;
    }

    public String getGithubPath() {
        return this.githubPath;
    }

//<<<<<<< HEAD
//=======
//    /*public String getTreeSha() {
//        return this.treeSha;
//    }*/
//
//>>>>>>> 31f79f789b79cbe5980a6f0de9f5f3dbbca0cb77
}
