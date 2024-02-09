package com.runicrealms.plugin.filepull;

import com.runicrealms.plugin.filepull.target.FileTarget;
import com.runicrealms.plugin.filepull.target.FolderTarget;
import com.runicrealms.plugin.filepull.target.Target;
import com.runicrealms.plugin.filepull.target.ZipTarget;
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

public class FileSyncWebhook {

    private final boolean webhookActive;
    private boolean listening = true;

    public FileSyncWebhook(boolean webhookActive, int port) {
        if (webhookActive)
            Bukkit.getScheduler().runTaskAsynchronously(RunicFilePull.getInstance(), () -> startWebhook(port));
        this.webhookActive = webhookActive;
    }

    public boolean isListening() {
        return webhookActive && listening;
    }

    public void toggleListening() {
        this.listening = !this.listening;
    }

    private void startWebhook(int port) {
        Spark.port(port);
        Spark.post("/", (request, response) -> {
            if (!isListening()) return "Unmonitored event (sync not active)";
            // Create a SHA256 hash with our secret key, and compare it to the requests' x-hub-signature header for github authentication
            String signature = "sha256=" + HmacUtils.hmacSha256Hex(RunicFilePull.WEBHOOK_SECRET, request.body());
            String header = request.headers("X-Hub-Signature-256");
            if (header == null || header.isEmpty()) return "Invalid Auth (header empty)";
            if (!header.equalsIgnoreCase(signature)) return "Invalid Auth (header invalid signature)";
            JSONObject webhookBody = (JSONObject) new JSONParser().parse(request.body());

            String ref = (String) webhookBody.get("ref");
            if (!ref.endsWith(RunicFilePull.getInstance().getFileConfig().getTargetBranch())) {
                Bukkit.broadcastMessage(ChatColor.DARK_GREEN + "[FileSync] " + ChatColor.RED + "Unmonitored change detected on ref " + ref + ", ignoring...");
                return "Unmonitored event (not current FileSync branch)";
            }

            // Check if we have changed commits (i.e. the webhook request has some type of file change). Other events like pings and pull requests would be ignored.
            if (!webhookBody.containsKey("after") || !webhookBody.containsKey("before"))
                return "Unmonitored event (does not contain before or after)";
            String commitBefore = (String) webhookBody.get("before");
            String commitAfter = (String) webhookBody.get("after");
            if (commitBefore.equalsIgnoreCase(commitAfter)) return "Unmonitored event (no commit hash change)";

            // Get the list of commits to find the latest one
//            JSONArray commitList = ((JSONArray) new JSONParser().parse(FileUtils.getWithAuth("https://api.github.com/repos/" + RunicFilePull.REPO_PATH + "/commits", RunicFilePull.AUTH_TOKEN))).toArray();
//            if (commitList.size() < 1) return "Failed";
//            JSONObject commitInfo = (JSONObject) commitList[0];
//            String commitSha = (String) commitInfo.get("sha");

//            JSONArray commitsInPayload = (JSONArray) webhookBody.get("commits");
//            String headBranch = null;
//            for (Object commitInPayloadObj : commitsInPayload) {
//                JSONObject commitInPayload = (JSONObject) commitInPayloadObj;
//                String commitInPayloadSha = (String) commitInPayload.get("id");
//                JSONObject branchesWhereHead = (JSONArray) new JSONParser().parse(FileUtils.getWithAuth("https://api.github.com/repos/" + RunicFilePull.REPO_PATH + "/commits/" + commitInPayloadSha + "/branches-where-head", RunicFilePull.AUTH_TOKEN));
//            }

            JSONArray commitList = (JSONArray) webhookBody.get("commits");
            if (commitList.size() == 0) return "Unmonitored event (no commits)";

            for (Object commitInList : commitList) {

                // Grab the latest commit and get the list of files changed
                String commitSha = (String) ((JSONObject) commitInList).get("id");

                JSONObject commit = (JSONObject) new JSONParser().parse(FileUtils.getWithAuth(
                        "https://api.github.com/repos/"
                                + RunicFilePull.getInstance().getFileConfig().getTargetRepo()
                                + "/commits/"
                                + commitSha,
                        RunicFilePull.GH_AUTH_TOKEN));
                JSONArray commitFiles = (JSONArray) commit.get("files");
                for (Object fileObj : commitFiles) {
                    JSONObject fileInList = (JSONObject) fileObj;
                    JSONObject file = (JSONObject) new JSONParser().parse(FileUtils.getWithAuth((String) fileInList.get("contents_url"), RunicFilePull.GH_AUTH_TOKEN));
                    String fileName = (String) file.get("name"); // Name of file
                    String filePath = (String) file.get("path"); // Name of file with path (mobs/file.yml)
                    if (!fileName.endsWith(".yml")) continue;

                    // Create the local file path for the file we are changing
                    File destination = null;
                    for (Target target : RunicFilePull.getInstance().getFileConfig().getTargets()) {
                        if (target instanceof FolderTarget) {
                            if (filePath.startsWith(target.getGitHubPath())) {
                                destination = new File(RunicFilePull.getInstance().getDataFolder().getParentFile().getParent(), target.getLocalPath());
                                destination = new File(destination, fileName);
                                break;
                            }
                        } else if (target instanceof FileTarget) {
                            if (filePath.equalsIgnoreCase(target.getGitHubPath())) {
                                destination = new File(RunicFilePull.getInstance().getDataFolder().getParentFile().getParent(), target.getLocalPath());
                                break;
                            }
                        } else if (target instanceof ZipTarget) {
                            if (filePath.equalsIgnoreCase(target.getGitHubPath())) {
                                Bukkit.broadcastMessage(ChatColor.RED + "WARNING: GH target modified zip file " + filePath + " which is not compatible with FileSync. Ignoring...");
                                return "Unmonitored event (zip file)";
                            }
                        }
                    }

                    if (destination == null) return "Unmonitored event (unknown destination)";

//                    File destination;
//                    try {
//                        destination = new File(RunicFilePull.getInstance().getDataFolder().getParentFile().getParent(), FilePullFolder.getFromPath((String) file.get("path")).getLocalPath());
//                    } catch (IllegalArgumentException exception) {
//                        continue;
//                    }
//                    File current = new File(destination, (String) file.get("name"));

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
                            if (destination.exists()) {
                                try {
                                    Files.delete(destination.toPath());
                                } catch (Exception exception) {
                                    Bukkit.broadcastMessage(ChatColor.RED + "ERROR: attempted to pull github file change " + fileName + " but could not remove local file. Message excel to get stacktrace with more info. Aborting.");
                                    exception.printStackTrace();
                                    continue;
                                }
                            }
                        }
                        if (status.equalsIgnoreCase("added") || status.equalsIgnoreCase("modified") || status.equalsIgnoreCase("renamed")) {
                            FileUtils.writeToFile(FileUtils.decodeBase64((String) file.get("content")), destination);
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
            }

            return "Okay";
        });
    }

}
