package com.runicrealms.runicfilepull;

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
import java.util.logging.Level;

public class FilePull {

    // runicrealmsgithub:runicrealmsPASSWORD

    private static final String AUTH_TOKEN = "47a7bcf0b66a78f709f7b39d416f78ef092f9564";

    private static Map<FilePullFolder, Boolean> folders = new HashMap<>();
    //private static Map<FilePullFolder, String> treeShas = new HashMap<>();

    private static boolean isRunning = false;
    private static volatile Integer totalFiles = 0;
    private static volatile Integer filesCompleted = 0;
    private static volatile Integer downloadsStarted = 0;
    private static volatile Integer downloadsNeeded = 0;

    public static void setFolderEnabled(FilePullFolder folder, Boolean enabled) {
        folders.put(folder, enabled);
    }

    public static Boolean isFolderEnabled(FilePullFolder folder) {
        return folders.get(folder);
    }

    private static void reset() {
        isRunning = false;
        totalFiles = 0;
        filesCompleted = 0;
        downloadsStarted = 0;
        downloadsNeeded = 0;
    }

    public static void startFilePull(Player initiator) {
        if (!isRunning) {
            isRunning = true;

            Bukkit.broadcastMessage(ChatColor.GREEN + "File Pull Initiated - Server Will Restart On Completion");
            Bukkit.broadcastMessage(ChatColor.DARK_GREEN + "WARNING - Mobs and quests may break while File Pull is running!");

            for (FilePullFolder folder : FilePullFolder.values()) {
                if (folders.get(folder) == true) {
                    downloadsNeeded++;
                    Bukkit.getScheduler().runTaskAsynchronously(Plugin.getInstance(), () -> {try {mirrorFiles(folder);} catch (Exception exception) {Bukkit.broadcastMessage(ChatColor.RED + "There was an issue downloading the " + folder.getGithubPath() + "! Check the console for more information. Some files may be be missing."); exception.printStackTrace();}});
                }
            }
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (downloadsStarted == downloadsNeeded) {
                        if (filesCompleted >= totalFiles) {
                            if (folders.get(FilePullFolder.QUESTS) || folders.get(FilePullFolder.RUNIC_ITEMS)) {
                                Bukkit.getScheduler().runTaskLater(Plugin.getInstance(), () -> {reset(); Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "rstop");}, 5 * 20);
                                Bukkit.getOnlinePlayers().forEach(player -> player.sendTitle("", ChatColor.GREEN + "Done! " + ChatColor.DARK_RED + "Restarting in " + ChatColor.RED + "5" + ChatColor.DARK_RED + "...", 0, 40, 0));
                                Bukkit.getScheduler().runTaskLater(Plugin.getInstance(), () -> Bukkit.getOnlinePlayers().forEach(player -> player.sendTitle("", ChatColor.DARK_RED + "Restarting in " + ChatColor.RED + "4" + ChatColor.DARK_RED + "...", 0, 40, 0)), 20L * 1);
                                Bukkit.getScheduler().runTaskLater(Plugin.getInstance(), () -> Bukkit.getOnlinePlayers().forEach(player -> player.sendTitle("", ChatColor.DARK_RED + "Restarting in " + ChatColor.RED + "3" + ChatColor.DARK_RED + "...", 0, 40, 0)), 20L * 2);
                                Bukkit.getScheduler().runTaskLater(Plugin.getInstance(), () -> Bukkit.getOnlinePlayers().forEach(player -> player.sendTitle("", ChatColor.DARK_RED + "Restarting in " + ChatColor.RED + "2" + ChatColor.DARK_RED + "...", 0, 40, 0)), 20L * 3);
                                Bukkit.getScheduler().runTaskLater(Plugin.getInstance(), () -> Bukkit.getOnlinePlayers().forEach(player -> player.sendTitle("", ChatColor.DARK_RED + "Restarting in " + ChatColor.RED + "1" + ChatColor.DARK_RED + "...", 0, 40, 0)), 20L * 4);
                                this.cancel();
                                return;
                            } else {
                                Bukkit.getScheduler().runTaskLater(Plugin.getInstance(), () -> {reset(); Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mm reload");}, 5 * 20);
                                Bukkit.getOnlinePlayers().forEach(player -> player.sendTitle("", ChatColor.GREEN + "Done! " + ChatColor.DARK_RED + "Reloading MythicMobs in " + ChatColor.RED + "5" + ChatColor.DARK_RED + "...", 0, 40, 0));
                                Bukkit.getScheduler().runTaskLater(Plugin.getInstance(), () -> Bukkit.getOnlinePlayers().forEach(player -> player.sendTitle("", ChatColor.DARK_RED + "Reloading MythicMobs in " + ChatColor.RED + "4" + ChatColor.DARK_RED + "...", 0, 40, 0)), 20L * 1);
                                Bukkit.getScheduler().runTaskLater(Plugin.getInstance(), () -> Bukkit.getOnlinePlayers().forEach(player -> player.sendTitle("", ChatColor.DARK_RED + "Reloading MythicMobs in " + ChatColor.RED + "3" + ChatColor.DARK_RED + "...", 0, 40, 0)), 20L * 2);
                                Bukkit.getScheduler().runTaskLater(Plugin.getInstance(), () -> Bukkit.getOnlinePlayers().forEach(player -> player.sendTitle("", ChatColor.DARK_RED + "Reloading MythicMobs in " + ChatColor.RED + "2" + ChatColor.DARK_RED + "...", 0, 40, 0)), 20L * 3);
                                Bukkit.getScheduler().runTaskLater(Plugin.getInstance(), () -> Bukkit.getOnlinePlayers().forEach(player -> player.sendTitle("", ChatColor.DARK_RED + "Reloading MythicMobs in " + ChatColor.RED + "1" + ChatColor.DARK_RED + "...", 0, 40, 0)), 20L * 4);
                                this.cancel();
                                return;
                            }
                        }
                        double percentage = Math.round(((100.0 * filesCompleted) / totalFiles * 1.0) * 10.0) / 10.0;
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
            }.runTaskTimerAsynchronously(Plugin.getInstance(), 20L, 20L);
        } else {
            initiator.sendMessage(ChatColor.RED + "FilePull is already running!");
        }
    }

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

    private static void mirrorFiles(FilePullFolder folder) throws Exception {
        JSONObject commit = (JSONObject) (new JSONParser()).parse(getWithAuth("https://api.github.com/repos/Skyfallin/writer-files/branches/master"));
        String treeShaUrl = (String) ((JSONObject) ((JSONObject) ((JSONObject) commit.get("commit")).get("commit")).get("tree")).get("url");
        JSONArray tree = (JSONArray) ((JSONObject) (new JSONParser()).parse(getWithAuth(treeShaUrl))).get("tree");
        JSONArray files = null;
        for (Object object : tree.toArray()) {
            JSONObject treeObject = (JSONObject) object;
            if (((String) treeObject.get("path")).equalsIgnoreCase(folder.getGithubPath())) {
                String folderURL = (String) treeObject.get("url");
                JSONObject folderJSON = (JSONObject) (new JSONParser()).parse(getWithAuth(folderURL));
                files = (JSONArray) folderJSON.get("tree");
                break;
            }
        }
        if (files == null) {
            Bukkit.broadcastMessage(ChatColor.RED + "There was an error loading the GitHub API, mass ping Excel!");
        }
        totalFiles += files.size();
        downloadsStarted++;
        File destination = new File(Plugin.getInstance().getDataFolder().getParent(), folder.getLocalPath());
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
                filesCompleted++;
            }
        }
    }

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
