package com.runicrealms.runicfilepull;

import java.io.File;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.rylinaux.plugman.util.PluginUtil;

public class CommandFilePull implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		try {
			sender.sendMessage(ChatColor.GREEN + "Downloading quests from Github...");
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
			sender.sendMessage(ChatColor.GREEN + "Writing quests to file...");
			String questsFolderURL = Util.formatGitlabFolderPath(Plugin.getInstance().getConfig().getString("quests-path"));
			JSONArray quests = (JSONArray) (new JSONParser()).parse(Util.getDataFromURL(questsFolderURL));
			for (int i = 0; i < quests.size(); i++) {
				JSONObject object = (JSONObject) quests.get(i);
				if (((String) object.get("type")).equalsIgnoreCase("blob")) {
					String fileURL = Util.formatGitlabFilePath((String) object.get("path"));
					Util.writeToFile(new File(questFolder, (String) object.get("name")), Util.getDataFromURL(fileURL));
				} 
			}
			PluginUtil.reload(Util.getPlugin("RunicQuests"));
			sender.sendMessage(ChatColor.GREEN + "Downloading mobs from Github...");
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
			sender.sendMessage(ChatColor.GREEN + "Writing mobs to file...");
			String mobsFolderURL = Util.formatGitlabFolderPath(Plugin.getInstance().getConfig().getString("mobs-path"));
			JSONArray mobs = (JSONArray) (new JSONParser()).parse(Util.getDataFromURL(mobsFolderURL));
			for (int i = 0; i < mobs.size(); i++) {
				JSONObject object = (JSONObject) mobs.get(i);
				if (((String) object.get("type")).equalsIgnoreCase("blob")) {
					String fileURL = Util.formatGitlabFilePath((String) object.get("path"));
					Util.writeToFile(new File(mobsFolder, (String) object.get("name")), Util.getDataFromURL(fileURL));
				}
			}
			File itemsFolder = Util.getSubFolder(mmFolder, "Items");
			if (!itemsFolder.exists()) {
				itemsFolder.mkdirs();
			}
			for (File item : itemsFolder.listFiles()) {
				if (item.isFile()) {
					item.delete();
				}
			}
			sender.sendMessage(ChatColor.GREEN + "Writing items to file...");
			String itemsFolderURL = Util.formatGitlabFolderPath(Plugin.getInstance().getConfig().getString("items-path"));
			JSONArray items = (JSONArray) (new JSONParser()).parse(Util.getDataFromURL(itemsFolderURL));
			for (int i = 0; i < items.size(); i++) {
				JSONObject object = (JSONObject) items.get(i);
				if (((String) object.get("type")).equalsIgnoreCase("blob")) {
					String fileURL = Util.formatGitlabFilePath((String) object.get("path"));
					Util.writeToFile(new File(itemsFolder, (String) object.get("name")), Util.getDataFromURL(fileURL));
				}
			}
			PluginUtil.reload(Util.getPlugin("MythicMobs"));
		} catch (Exception exception) {
			sender.sendMessage(ChatColor.RED + "There was an issue getting the files from the github repo. Refer to console for more information.");
			exception.printStackTrace();
			return true;
		}
		sender.sendMessage(ChatColor.GREEN + "Successfully loaded files from github!");
		return true;
	}

}
