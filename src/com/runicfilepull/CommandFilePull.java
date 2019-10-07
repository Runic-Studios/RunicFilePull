package com.runicfilepull;

import java.io.File;
import java.io.PrintWriter;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class CommandFilePull implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		try {
			File runicFolder = getSubFolder(Plugin.getInstance().getDataFolder().getParentFile(), "RunicQuests");
			if (!runicFolder.exists()) {
				runicFolder.mkdirs();
			}
			File questFolder = getSubFolder(runicFolder, "quests");
			if (!questFolder.exists()) {
				questFolder.mkdirs();
			}
			for (File quest : questFolder.listFiles()) {
				if (quest.isFile()) {
					quest.delete();
				}
			}
			String folderURL = URLUtil.formatGitlabFolderPath(Plugin.getInstance().getConfig().getString("quests-path"));
			JSONArray array = (JSONArray) (new JSONParser()).parse(URLUtil.getDataFromURL(folderURL));
			for (int i = 0; i < array.size(); i++) {
				JSONObject object = (JSONObject) array.get(i);
				if (((String) object.get("type")).equalsIgnoreCase("blob")) {
					String fileURL = URLUtil.formatGitlabFilePath((String) object.get("path"));
					writeToFile(new File(questFolder, (String) object.get("name")), URLUtil.getDataFromURL(fileURL));
				} 
			}
		} catch (Exception exception) {
			sender.sendMessage(ChatColor.RED + "There was an issue getting the files from the github repo. Refer to console for more information.");
			exception.printStackTrace();
			return true;
		}
		sender.sendMessage(ChatColor.GREEN + "Successfully loaded quest files from github!");
		return true;
	}
	
	private static void writeToFile(File file, String data) throws Exception {
		if (!file.exists()) {
			file.createNewFile();
		}
		PrintWriter writer = new PrintWriter(file);	
		writer.print(data);
		writer.close();
	}

	private static File getSubFolder(File folder, String subfolder) {
		for (File file : folder.listFiles()) {
			if (file.getName().equalsIgnoreCase(subfolder)) {
				return file;
			}
		}
		return null;
	}

}
