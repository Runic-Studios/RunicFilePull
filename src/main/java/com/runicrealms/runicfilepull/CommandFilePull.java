package com.runicrealms.runicfilepull;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.rylinaux.plugman.util.PluginUtil;

public class CommandFilePull implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Bukkit.getScheduler().runTaskAsynchronously(Plugin.getInstance(), new Runnable() {
            @Override
            public void run() {
                try {
                    sendAllPlayers("&aFile pull initiated, lag incoming");
                    sendAllPlayers("&aClearing quests...");
                    File runicFolder = Util.getSubFolder(Plugin.getInstance().getDataFolder().getParentFile(), "RunicQuests");
                    if (!runicFolder.exists()) {
                        runicFolder.mkdirs();
                    }
                    File questFolder = Util.getSubFolder(runicFolder, "quests");
                    if (!questFolder.exists()) {
                        questFolder.mkdirs();
                    }
                    for (File quest : questFolder.listFiles()) {
                        if (quest.isFile()) {
                            quest.delete();
                        }
                    }
                    sendAllPlayers("&aDownloading quests from gitlab...");
                    String questsFolderURL = Util.formatGitlabFolderPath(Plugin.getInstance().getConfig().getString("quests-path"));
                    JSONArray quests = (JSONArray) (new JSONParser()).parse(Util.getDataFromURL(questsFolderURL));
                    for (int i = 0; i < quests.size(); i++) {
                        JSONObject object = (JSONObject) quests.get(i);
                        if (((String) object.get("type")).equalsIgnoreCase("blob")) {
                            String fileURL = Util.formatGitlabFilePath((String) object.get("path"));
                            Util.writeToFile(new File(questFolder, (String) object.get("name")), Util.getDataFromURL(fileURL));
                        }
                    }
                    sendAllPlayers("&aReloading RunicQuests...");
                    PluginUtil.reload(Util.getPlugin("RunicQuests"));
                    sendAllPlayers("&aClearing mobs...");
                    File mmFolder = Util.getSubFolder(Plugin.getInstance().getDataFolder().getParentFile(), "MythicMobs");
                    if (!mmFolder.exists()) {
                        mmFolder.mkdirs();
                    }
                    File mobsFolder = Util.getSubFolder(mmFolder, "Mobs");
                    if (!mobsFolder.exists()) {
                        mobsFolder.mkdirs();
                    }
                    for (File mob : mobsFolder.listFiles()) {
                        if (mob.isFile()) {
                            mob.delete();
                        }
                    }
                    sendAllPlayers("&aDownloading mobs from gitlab...");
                    String mobsFolderURL = Util.formatGitlabFolderPath(Plugin.getInstance().getConfig().getString("mobs-path"));
                    JSONArray mobs = (JSONArray) (new JSONParser()).parse(Util.getDataFromURL(mobsFolderURL));
                    for (int i = 0; i < mobs.size(); i++) {
                        JSONObject object = (JSONObject) mobs.get(i);
                        if (((String) object.get("type")).equalsIgnoreCase("blob")) {
                            String fileURL = Util.formatGitlabFilePath((String) object.get("path"));
                            Util.writeToFile(new File(mobsFolder, (String) object.get("name")), Util.getDataFromURL(fileURL));
                        }
                    }
                    sendAllPlayers("&aClearing items...");
                    File itemsFolder = Util.getSubFolder(mmFolder, "Items");
                    if (!itemsFolder.exists()) {
                        itemsFolder.mkdirs();
                    }
                    for (File item : itemsFolder.listFiles()) {
                        if (item.isFile()) {
                            item.delete();
                        }
                    }
                    sendAllPlayers("&aDownloading mobs from gitlab...");
                    String itemsFolderURL = Util.formatGitlabFolderPath(Plugin.getInstance().getConfig().getString("items-path"));
                    JSONArray items = (JSONArray) (new JSONParser()).parse(Util.getDataFromURL(itemsFolderURL));
                    for (int i = 0; i < items.size(); i++) {
                        JSONObject object = (JSONObject) items.get(i);
                        if (((String) object.get("type")).equalsIgnoreCase("blob")) {
                            String fileURL = Util.formatGitlabFilePath((String) object.get("path"));
                            Util.writeToFile(new File(itemsFolder, (String) object.get("name")), Util.getDataFromURL(fileURL));
                        }
                    }
                    sendAllPlayers("&aClearing skills...");
                    File skillsFolder = Util.getSubFolder(mmFolder, "Skills");
                    if (!skillsFolder.exists()) {
                        skillsFolder.mkdirs();
                    }
                    for (File item : skillsFolder.listFiles()) {
                        if (item.isFile()) {
                            item.delete();
                        }
                    }
                    sendAllPlayers("&aDownloading skills from gitlab...");
                    String skillsFolderURL = Util.formatGitlabFolderPath(Plugin.getInstance().getConfig().getString("skills-path"));
                    JSONArray skills = (JSONArray) (new JSONParser()).parse(Util.getDataFromURL(skillsFolderURL));
                    for (int i = 0; i < skills.size(); i++) {
                        JSONObject object = (JSONObject) skills.get(i);
                        if (((String) object.get("type")).equalsIgnoreCase("blob")) {
                            String fileURL = Util.formatGitlabFilePath((String) object.get("path"));
                            Util.writeToFile(new File(skillsFolder, (String) object.get("name")), Util.getDataFromURL(fileURL));
                        }
                    }
                    Bukkit.getScheduler().callSyncMethod(Plugin.getInstance(), () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mm reload"));
                } catch (Exception exception) {
                    sendAllPlayers("&aThere was an issue downloading the files from gitlab! Check the console for more information.");
                    exception.printStackTrace();
                }
                sendAllPlayers(ChatColor.GREEN + "Successfully loaded files from gitlab!");
            }
        });
        return true;
    }

    private static void sendAllPlayers(String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        }
    }

}
