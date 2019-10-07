package com.runicfilepull;

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
			String folderURL = Util.formatGitlabFolderPath(Plugin.getInstance().getConfig().getString("quests-path"));
			JSONArray array = (JSONArray) (new JSONParser()).parse(Util.getDataFromURL(folderURL));
			for (int i = 0; i < array.size(); i++) {
				JSONObject object = (JSONObject) array.get(i);
				if (((String) object.get("type")).equalsIgnoreCase("blob")) {
					String fileURL = Util.formatGitlabFilePath((String) object.get("path"));
					Util.writeToFile(new File(questFolder, (String) object.get("name")), Util.getDataFromURL(fileURL));
				} 
			}
			PluginUtil.reload(Util.getPlugin("RunicQuests"));
		} catch (Exception exception) {
			sender.sendMessage(ChatColor.RED + "There was an issue getting the files from the github repo. Refer to console for more information.");
			exception.printStackTrace();
			return true;
		}
		sender.sendMessage(ChatColor.GREEN + "Successfully loaded quest files from github!");
		return true;
	}

}
