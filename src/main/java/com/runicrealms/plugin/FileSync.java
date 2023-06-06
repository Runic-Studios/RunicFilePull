package com.runicrealms.plugin;

import org.apache.commons.codec.digest.HmacUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import spark.Spark;

import java.io.File;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileSync {

    public static void startWebhook() {
        Spark.port(25570);
        Spark.post("/", (request, response) -> {
            // Create a SHA256 hash with our secret key, and compare it to the requests' x-hub-signature header for github authentication
            String signature = "sha256=" + HmacUtils.hmacSha256Hex(RunicFilePull.SECRET, request.body());
            String header = request.headers("X-Hub-Signature-256");
            if (header == null || header.isEmpty()) return "Invalid Auth";
            if (!header.equalsIgnoreCase(signature)) return "Invalid Auth";
            JSONObject webhookBody = (JSONObject) new JSONParser().parse(request.body());

            // Check if we have changed commits (i.e. the webhook request has some type of file change). Other events like pings and pull requests would be ignored.
            if (!webhookBody.containsKey("after") || !webhookBody.containsKey("before")) return "Unmonitored event";
            String commitBefore = (String) webhookBody.get("before");
            String commitAfter = (String) webhookBody.get("after");
            if (commitBefore.equalsIgnoreCase(commitAfter)) return "Unmonitored event";

            // Get the list of commits to find the latest one
            Object[] commitList = ((JSONArray) new JSONParser().parse(FileUtils.getWithAuth("https://api.github.com/repos/" + RunicFilePull.REPO_PATH + "/commits", RunicFilePull.AUTH_TOKEN))).toArray();
            if (commitList.length < 1) return "Failed";
            JSONObject commitInfo = (JSONObject) commitList[0];
            String commitSha = (String) commitInfo.get("sha");

            // Grab the latest commit and get the list of files changed
            JSONObject commit = (JSONObject) new JSONParser().parse(FileUtils.getWithAuth("https://api.github.com/repos/" + RunicFilePull.REPO_PATH + "/commits/" + commitSha, RunicFilePull.AUTH_TOKEN));
            JSONArray commitFiles = (JSONArray) commit.get("files");
            for (Object fileObj : commitFiles) {
                JSONObject fileInList = (JSONObject) fileObj;
                JSONObject file = (JSONObject) new JSONParser().parse(FileUtils.getWithAuth((String) fileInList.get("contents_url"), RunicFilePull.AUTH_TOKEN));
                String fileName = (String) file.get("name"); // Name of file
                String filePath = (String) file.get("path"); // Name of file with path (mobs/file.yml)
                if (!fileName.endsWith(".yml")) continue;

                // Create the local file path for the file we are changing
                File destination;
                try {
                    destination = new File(RunicFilePull.getInstance().getDataFolder().getParent(), FilePullFolder.getFromPath((String) file.get("path")).getLocalPath());
                } catch (IllegalArgumentException exception) {
                    continue;
                }
                File current = new File(destination, (String) file.get("name"));

                try {
                    // Status states what change was made to the file, added, removed, modified, or renamed
                    String status = (String) fileInList.get("status");

                    if (status.equalsIgnoreCase("renamed") && fileInList.containsKey("previous_filename")) {
                        String previousFileNamePath = (String) fileInList.get("previous_filename"); // mobs/mymob.yml
                        Matcher matcher = Pattern.compile("[^/]*$").matcher(previousFileNamePath);
                        if (matcher.find()) {
                            String previousFileName = matcher.group(); // mymob.yml
                            File previousFile = new File(destination, previousFileName);
                            try {
                                Files.delete(previousFile.toPath());
                            } catch (Exception exception) {
                                Bukkit.broadcastMessage(ChatColor.RED + "ERROR: attempted to pull github file change " + fileName + ", and when deleting previous file name " + previousFile.getName() +
                                        ", failed to remove local file. Message excel to get stacktrace with more info. Continuing with pull.");
                                exception.printStackTrace();
                            }
                        } else {
                            Bukkit.broadcastMessage(ChatColor.RED + "Could not find local file to delete: " + previousFileNamePath + ". Continuing with sync...");
                        }
                    }

                    if (status.equalsIgnoreCase("removed") || status.equalsIgnoreCase("modified")) {
                        if (current.exists()) {
                            try {
                                Files.delete(current.toPath());
                            } catch (Exception exception) {
                                Bukkit.broadcastMessage(ChatColor.RED + "ERROR: attempted to pull github file change " + fileName + " but could not remove local file. Message excel to get stacktrace with more info. Aborting.");
                                exception.printStackTrace();
                                continue;
                            }
                        }
                    }
                    if (status.equalsIgnoreCase("added") || status.equalsIgnoreCase("modified") || status.equalsIgnoreCase("renamed")) {
                        FileUtils.writeBase64ToFile((String) file.get("content"), current);
                    }
                    if (status.equalsIgnoreCase("added")) {
                        Bukkit.broadcastMessage(ChatColor.DARK_GREEN + "[FileSync] " + ChatColor.GREEN + "Added new file " + filePath);
                    } else if (status.equalsIgnoreCase("removed")) {
                        Bukkit.broadcastMessage(ChatColor.DARK_GREEN + "[FileSync] " + ChatColor.GREEN + "Removed file " + filePath);
                    } else if (status.equalsIgnoreCase("modified")) {
                        Bukkit.broadcastMessage(ChatColor.DARK_GREEN + "[FileSync] " + ChatColor.GREEN + "Modified file " + filePath);
                    } else if (status.equalsIgnoreCase("renamed")) {
                        Bukkit.broadcastMessage(ChatColor.DARK_GREEN + "[FileSync] " + ChatColor.GREEN + "Renamed (and modified) file " + filePath);
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                    Bukkit.broadcastMessage(ChatColor.GREEN + "ERROR: attempted to pull github file change " + fileName + " but failed for indeterminate reason. Check console for more, report to excel with the current time.");
                }
            }

            return "Okay";
        });
    }

}
