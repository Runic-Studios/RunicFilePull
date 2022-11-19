package com.runicrealms.plugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

/**
 * A sync/transfer operation to retrieve yaml files from GitHub
 */
public class FilePullOperation {

    // runicrealmsgithub:runicrealmsPASSWORD

    private static final String AUTH_TOKEN = "47a7bcf0b66a78f709f7b39d416f78ef092f9564";

    private static final Map<FilePullFolder, Boolean> folders = new HashMap<>();
    private static final AtomicInteger totalFiles = new AtomicInteger(0);
    private static final AtomicInteger filesCompleted = new AtomicInteger(0);
    private static final AtomicInteger downloadsStarted = new AtomicInteger(0);
    private static final AtomicInteger downloadsNeeded = new AtomicInteger(0);
    private static boolean isRunning = false;

    /**
     * @param folder  the directory of the files to transfer
     * @param enabled true if those files should sync from server
     */
    public static void setFolderEnabled(FilePullFolder folder, Boolean enabled) {
        folders.put(folder, enabled);
    }

    /**
     * @param folder the directory of the files to transfer
     * @return true if those files should be synced from server
     */
    public static Boolean isFolderEnabled(FilePullFolder folder) {
        return folders.get(folder);
    }

    /**
     * Sanitizes the state of the current file pull operation
     */
    private static void reset() {
        isRunning = false;
        totalFiles.set(0);
        filesCompleted.set(0);
        downloadsStarted.set(0);
        downloadsNeeded.set(0);
    }

    /**
     * Begins the file sync task from git
     *
     * @param initiator who initiated the operation
     */
    public static void startFilePull(Player initiator) {
        if (!isRunning) {
            isRunning = true;

            Bukkit.broadcastMessage(ChatColor.GREEN + "File Pull Initiated - Server Will Restart On Completion");
            Bukkit.broadcastMessage(ChatColor.DARK_GREEN + "WARNING - Mobs and quests may break while File Pull is running!");

            for (FilePullFolder folder : FilePullFolder.values()) {
                if (folders.get(folder)) {
                    downloadsNeeded.addAndGet(1);
                    Bukkit.getScheduler().runTaskAsynchronously(RunicFilePull.getInstance(), () -> {
                        try {
                            mirrorFiles(folder);
                        } catch (Exception exception) {
                            Bukkit.broadcastMessage(ChatColor.RED + "There was an issue downloading the " + folder.getGitHubPath() + "! Check the console for more information. Files are likely missing.");
                            exception.printStackTrace();
                        }
                    });
                }
            }
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (downloadsStarted.get() == downloadsNeeded.get()) {
                        if (filesCompleted.get() >= totalFiles.get()) {
                            if (folders.get(FilePullFolder.MYTHIC_ITEMS) || folders.get(FilePullFolder.MOBS) || folders.get(FilePullFolder.SKILLS)) {
                                Bukkit.getScheduler().runTaskLater(RunicFilePull.getInstance(), () -> {
                                    reset();
                                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mm reload");
                                }, 5 * 20);
                                Bukkit.getOnlinePlayers().forEach(player -> player.sendTitle("", ChatColor.GREEN + "Done! " + ChatColor.DARK_RED + "Reloading MythicMobs in " + ChatColor.RED + "5" + ChatColor.DARK_RED + "...", 0, 40, 0));
                                Bukkit.getScheduler().runTaskLater(RunicFilePull.getInstance(), () -> Bukkit.getOnlinePlayers().forEach(player -> player.sendTitle("", ChatColor.DARK_RED + "Reloading MythicMobs in " + ChatColor.RED + "4" + ChatColor.DARK_RED + "...", 0, 40, 0)), 20L);
                                Bukkit.getScheduler().runTaskLater(RunicFilePull.getInstance(), () -> Bukkit.getOnlinePlayers().forEach(player -> player.sendTitle("", ChatColor.DARK_RED + "Reloading MythicMobs in " + ChatColor.RED + "3" + ChatColor.DARK_RED + "...", 0, 40, 0)), 20L * 2);
                                Bukkit.getScheduler().runTaskLater(RunicFilePull.getInstance(), () -> Bukkit.getOnlinePlayers().forEach(player -> player.sendTitle("", ChatColor.DARK_RED + "Reloading MythicMobs in " + ChatColor.RED + "2" + ChatColor.DARK_RED + "...", 0, 40, 0)), 20L * 3);
                                Bukkit.getScheduler().runTaskLater(RunicFilePull.getInstance(), () -> Bukkit.getOnlinePlayers().forEach(player -> player.sendTitle("", ChatColor.DARK_RED + "Reloading MythicMobs in " + ChatColor.RED + "1" + ChatColor.DARK_RED + "...", 0, 40, 0)), 20L * 4);
                            } else {
                                Bukkit.getScheduler().runTaskLater(RunicFilePull.getInstance(), () -> {
                                    reset();
                                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "rstop");
                                }, 5 * 20);
                                Bukkit.getOnlinePlayers().forEach(player -> player.sendTitle("", ChatColor.GREEN + "Done! " + ChatColor.DARK_RED + "Restarting in " + ChatColor.RED + "5" + ChatColor.DARK_RED + "...", 0, 40, 0));
                                Bukkit.getScheduler().runTaskLater(RunicFilePull.getInstance(), () -> Bukkit.getOnlinePlayers().forEach(player -> player.sendTitle("", ChatColor.DARK_RED + "Restarting in " + ChatColor.RED + "4" + ChatColor.DARK_RED + "...", 0, 40, 0)), 20L);
                                Bukkit.getScheduler().runTaskLater(RunicFilePull.getInstance(), () -> Bukkit.getOnlinePlayers().forEach(player -> player.sendTitle("", ChatColor.DARK_RED + "Restarting in " + ChatColor.RED + "3" + ChatColor.DARK_RED + "...", 0, 40, 0)), 20L * 2);
                                Bukkit.getScheduler().runTaskLater(RunicFilePull.getInstance(), () -> Bukkit.getOnlinePlayers().forEach(player -> player.sendTitle("", ChatColor.DARK_RED + "Restarting in " + ChatColor.RED + "2" + ChatColor.DARK_RED + "...", 0, 40, 0)), 20L * 3);
                                Bukkit.getScheduler().runTaskLater(RunicFilePull.getInstance(), () -> Bukkit.getOnlinePlayers().forEach(player -> player.sendTitle("", ChatColor.DARK_RED + "Restarting in " + ChatColor.RED + "1" + ChatColor.DARK_RED + "...", 0, 40, 0)), 20L * 4);
                            }
                            this.cancel();
                            return;
                        }
                        double percentage = Math.round(((100.0 * filesCompleted.get()) / totalFiles.get()) * 10.0) / 10.0;
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            player.sendTitle("", ChatColor.GREEN + "File Pull: " + ChatColor.YELLOW + percentage + "%", 0, 40, 0);
                        }
                        Bukkit.getLogger().log(Level.INFO, "[RunicFilePull] File Pull Progress: " + percentage + "%");
                    } else {
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            player.sendTitle("", ChatColor.GREEN + "File Pull: " + ChatColor.YELLOW + "Loading" + ChatColor.GREEN + "...", 0, 40, 0);
                        }
                        Bukkit.getLogger().log(Level.INFO, "[RunicFilePull] File Pull Progress: Loading...");
                    }
                }
            }.runTaskTimerAsynchronously(RunicFilePull.getInstance(), 20L, 20L);
        } else {
            initiator.sendMessage(ChatColor.RED + "FilePullOperation is already running!");
        }
    }

    /**
     * @param base64
     * @param file
     * @throws Exception
     */
    private static void writeBase64ToFile(String base64, File file) throws Exception {
        StringBuilder base64Builder = new StringBuilder(base64.replaceAll("\\n", ""));
        while (base64Builder.length() % 4 > 0) {
            base64Builder.append('=');
        }
        base64 = base64Builder.toString();
        PrintWriter writer = new PrintWriter(file);
        writer.print(Base64Coder.decodeString(base64));
        writer.close();
    }

    /**
     * @param folder
     * @throws Exception
     */
    private static void mirrorFiles(FilePullFolder folder) throws Exception {
        JSONObject commit = (JSONObject) (new JSONParser()).parse(getWithAuth("https://api.github.com/repos/Skyfallin/writer-files/branches/master"));
        String treeShaUrl = (String) ((JSONObject) ((JSONObject) ((JSONObject) commit.get("commit")).get("commit")).get("tree")).get("url");
        JSONArray tree = (JSONArray) ((JSONObject) (new JSONParser()).parse(getWithAuth(treeShaUrl))).get("tree");
        JSONArray files = null;
        for (Object object : tree.toArray()) {
            JSONObject treeObject = (JSONObject) object;
            if (((String) treeObject.get("path")).equalsIgnoreCase(folder.getGitHubPath())) {
                String folderURL = (String) treeObject.get("url");
                JSONObject folderJSON = (JSONObject) (new JSONParser()).parse(getWithAuth(folderURL));
                files = (JSONArray) folderJSON.get("tree");
                break;
            }
        }
        if (files == null) {
            Bukkit.broadcastMessage(ChatColor.RED + "There was an error loading the GitHub API, mass ping Excel!");
            return;
        }
        totalFiles.addAndGet(files.size());
        downloadsStarted.addAndGet(1);
        File destination = new File(RunicFilePull.getInstance().getDataFolder().getParent(), folder.getLocalPath());
        if (!destination.exists()) {
            destination.mkdirs();
        }
        for (File file : destination.listFiles()) {
            if (file.exists()) {
                file.delete();
            }
        }
        for (Object object : files.toArray()) {
            JSONObject jsonObject = ((JSONObject) object);
            if (jsonObject.get("type").equals("blob") && ((String) jsonObject.get("path")).endsWith(".yml")) {
                File localFile = new File(destination, (String) jsonObject.get("path"));
                JSONObject gitJson = (JSONObject) (new JSONParser()).parse(getWithAuth((String) ((JSONObject) object).get("url")));
                writeBase64ToFile((String) gitJson.get("content"), localFile);
                filesCompleted.addAndGet(1);
            }
        }
    }

    /**
     * @param url
     * @return
     * @throws Exception
     */
    private static String getWithAuth(String url) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        connection.setDoOutput(true);
        connection.setRequestProperty("Authorization", "token " + AUTH_TOKEN);
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }
        return output.toString();
    }

}
