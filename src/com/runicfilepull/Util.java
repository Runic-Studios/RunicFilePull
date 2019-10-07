package com.runicfilepull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.bukkit.Bukkit;

public class Util {

	public static String encodeStringForURL(String value) throws Exception {
		return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
	}

	public static String getDataFromURL(String url) throws Exception {
		URL website = new URL(url);
		URLConnection connection = website.openConnection();
		BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		StringBuilder response = new StringBuilder();
		String inputLine;
		while ((inputLine = reader.readLine()) != null) {
			response.append(inputLine);
			response.append("\n");
		}
		reader.close();
		return response.toString();
	}

	public static String formatGitlabFilePath(String path) throws Exception {
		String formattedPath;
		formattedPath = path.startsWith("/") ? path.substring(1) : path;
		formattedPath = path.endsWith("/") ? path.substring(0, path.length() - 2) : path;
		StringBuilder builder = new StringBuilder();
		builder.append("https://gitlab.com/api/v4/projects/");
		builder.append(Plugin.getInstance().getConfig().getString("project-id"));
		builder.append("/repository/files/");
		builder.append(encodeStringForURL(formattedPath));
		builder.append("/raw?ref=");
		builder.append(Plugin.getInstance().getConfig().getString("branch"));
		builder.append("&private_token=");
		builder.append(Plugin.getInstance().getConfig().getString("access-token"));
		return builder.toString();
	}

	public static String formatGitlabFolderPath(String path) throws Exception {
		String formattedPath;
		formattedPath = path.startsWith("/") ? path.substring(1) : path;
		formattedPath = path.endsWith("/") ? path.substring(0, path.length() - 2) : path;
		StringBuilder builder = new StringBuilder();
		builder.append("https://gitlab.com/api/v4/projects/");
		builder.append(Plugin.getInstance().getConfig().getString("project-id"));
		builder.append("/repository/tree?path=");
		builder.append(encodeStringForURL(formattedPath));
		builder.append("&ref=");
		builder.append(Plugin.getInstance().getConfig().getString("branch"));
		builder.append("&private_token=");
		builder.append(Plugin.getInstance().getConfig().getString("access-token"));
		return builder.toString();
	}
	
	public static void writeToFile(File file, String data) throws Exception {
		if (!file.exists()) {
			file.createNewFile();
		}
		FileOutputStream outputStream = new FileOutputStream(file);
	    byte[] strToBytes = data.getBytes();
	    outputStream.write(strToBytes);
	    outputStream.close();
	}

	public static File getSubFolder(File folder, String subfolder) {
		for (File file : folder.listFiles()) {
			if (file.getName().equalsIgnoreCase(subfolder)) {
				return file;
			}
		}
		return null;
	}
	
	public static org.bukkit.plugin.Plugin getPlugin(String name) {
		for (org.bukkit.plugin.Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
			if (plugin.getName().equalsIgnoreCase(name)) {
				return plugin;
			}
		}
		return null;
	}

}
