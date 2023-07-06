package com.runicrealms.plugin;

import org.bukkit.Material;

import java.io.File;
import java.util.function.Consumer;

/**
 * Represents one of the many toggleable filepull destinations such as mobs, skills, quests, or single files like script-items.yml
 */
public interface FilePullDestination {

    /**
     * Pulls files from github for this destination
     *
     * @param onProgressUpdate Consumer that runs each time we download a file
     * @return the number of files we have to download
     * @throws Exception Misc exception
     */
    int pull(Consumer<Integer> onProgressUpdate) throws Exception;

    boolean isEnabled();

    void setEnabled(boolean enabled);

    String getIdentifier();

    void copyLocal(File githubLocalFolder) throws Exception; // File is base git dir but on local

    boolean adminsOnly();

    Material getIconMaterial();

}
