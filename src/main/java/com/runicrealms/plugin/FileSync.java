package com.runicrealms.plugin;

import org.apache.commons.codec.digest.HmacUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;
import spark.Spark;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class FileSync {

    private static final String SECRET = "TheSkyIsFallin8392"; // Secret x-hub-signature key that we use for salting the sha256 hash to verify its github
    private static final String AUTH_TOKEN = "47a7bcf0b66a78f709f7b39d416f78ef092f9564"; // github auth token for repository
    private static final String REPO_PATH = "Skyfallin/writer-files"; // repository path

    public static void startWebhook() {
        Spark.port(25570);
        Spark.post("/", (request, response) -> {
            String signature = "sha256=" + HmacUtils.hmacSha256Hex(SECRET, request.body());
            String header = request.headers("X-Hub-Signature-256");
            if (header == null || header.isEmpty()) return "Invalid Auth";
            if (!header.equalsIgnoreCase(signature)) return "Invalid Auth";
            JSONObject webhookBody = (JSONObject) new JSONParser().parse(request.body());
            if (!webhookBody.containsKey("after") || !webhookBody.containsKey("before")) return "Unmonitored event";
            String commitBefore = (String) webhookBody.get("before");
            String commitAfter = (String) webhookBody.get("after");
            if (commitBefore.equalsIgnoreCase(commitAfter)) return "Unmonitored event";

            Object[] commitList = ((JSONArray) new JSONParser().parse(getWithAuth("https://api.github.com/repos/" + REPO_PATH + "/commits"))).toArray();
            if (commitList.length < 1) return "Failed";
            JSONObject commitInfo = (JSONObject) commitList[0];
            String commitSha = (String) commitInfo.get("sha");


            JSONObject commit = (JSONObject) new JSONParser().parse(getWithAuth("https://api.github.com/repos/" + REPO_PATH + "/commits/" + commitSha));
            JSONArray commitFiles = (JSONArray) commit.get("files");
            for (Object fileObj : commitFiles) {
                JSONObject fileInList = (JSONObject) fileObj;
                JSONObject file = (JSONObject) new JSONParser().parse(getWithAuth((String) fileInList.get("contents_url")));
                String fileName = (String) file.get("name");
                String filePath = (String) file.get("path");
                if (!fileName.endsWith(".yml")) continue;

                File destination;
                try {
                    destination = new File(RunicFilePull.getInstance().getDataFolder().getParent(), FilePullFolder.getFromPath((String) file.get("path")).getLocalPath());
                } catch (IllegalArgumentException exception) {
                    continue;
                }
                File current = new File(destination, (String) file.get("name"));

                try {

                    String status = (String) fileInList.get("status");
                    if (status.equalsIgnoreCase("removed") || status.equalsIgnoreCase("modified")) {
                        if (current.exists()) {
                            boolean result = current.delete();
                            if (!result) {
                                Bukkit.broadcastMessage(ChatColor.RED + "ERROR: attempted to pull github file change " + fileName + " but could not remove local file. Aborting.");
                                continue;
                            }
                        }
                    }
                    if (status.equalsIgnoreCase("added") || status.equalsIgnoreCase("modified")) {
                        writeBase64ToFile((String) file.get("content"), current);
                    }
                    if (status.equalsIgnoreCase("added")) {
                        Bukkit.broadcastMessage(ChatColor.DARK_GREEN + "[FileSync] " + ChatColor.GREEN + "Added new file " + filePath);
                    } else if (status.equalsIgnoreCase("removed")) {
                        Bukkit.broadcastMessage(ChatColor.DARK_GREEN + "[FileSync] " + ChatColor.GREEN + "Removed file " + filePath);
                    } else if (status.equalsIgnoreCase("modified")) {
                        Bukkit.broadcastMessage(ChatColor.DARK_GREEN + "[FileSync] " + ChatColor.GREEN + "Modified file " + filePath);
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                    Bukkit.broadcastMessage(ChatColor.GREEN + "ERROR: attempted to pull github file change " + fileName + " but failed for indeterminate reason. Check console for more, report to excel with the current time.");
                }
            }

            return "Okay";
        });
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
