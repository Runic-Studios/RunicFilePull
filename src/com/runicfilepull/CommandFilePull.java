package com.runicfilepull;

import java.io.File;
import java.io.PrintWriter;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

@SuppressWarnings("deprecation")
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
			JSONArray array = getJSONArrayFromURL("https://api.github.com/repos/" + 
					Plugin.getInstance().getConfig().getString("github.user") + "/" + 
					Plugin.getInstance().getConfig().getString("github.repo") + "/contents/" + 
					Plugin.getInstance().getConfig().getString("github.quests-folder"));
			for (int i = 0; i < array.size(); i++) {
				JSONObject object = (JSONObject) array.get(i);
				if (((String) object.get("type")).equalsIgnoreCase("file")) {
					URL url = new URL((String) object.get("download_url"));
					String data = IOUtils.toString(url.openStream());
					File file = new File(questFolder, (String) object.get("name"));
					file.createNewFile();
					PrintWriter writer = new PrintWriter(file);	
					writer.print(data);
					writer.close();
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

	private static JSONArray getJSONArrayFromURL(String urlString) throws Exception {
		URL url = new URL(urlString);
		String data = IOUtils.toString(url.openStream());
		JSONArray jsonArray = (JSONArray) new JSONParser().parse(data);
		return jsonArray;
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
