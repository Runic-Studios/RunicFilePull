package com.runicrealms.runicfilepull;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

public class Plugin extends JavaPlugin implements CommandExecutor {

	private static Plugin plugin;
	private static boolean isRunning = false;
	
	@Override
	public void onEnable() {
		plugin = this;
		String[] commands = new String[] {"filepull", "fp", "pull"};
		this.getCommand("filepull").setExecutor(this);
		this.getCommand("fp").setExecutor(this);
		this.getCommand("pull").setExecutor(this);
	}
	
	public static Plugin getInstance() {
		return plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!isRunning) {
			isRunning = true;
			Bukkit.getScheduler().runTaskAsynchronously(this, new Runnable() {
				@Override
				public void run() {
					try {
						if (!getDataFolder().exists()) {
							getDataFolder().mkdir();
						}
						Bukkit.broadcastMessage(ChatColor.GREEN + "File Pull Initiated - Server Will Restart On Completion");
						GitHub github = new GitHubBuilder().withOAuthToken("8b7fd07ca5e9d08bfc8b878ab0da2913c2a62014").build();
						GHRepository repo = github.getRepository("Skyfallin/writer-files");
						Bukkit.broadcastMessage(ChatColor.GREEN + "Downloading mobs " + ChatColor.GRAY + "[0/5]");
						mirrorFiles(repo, "mobs", "MythicMobs/Mobs");
						Bukkit.broadcastMessage(ChatColor.GREEN + "Downloading skills " + ChatColor.GRAY + "[1/5]");
						mirrorFiles(repo, "skills", "MythicMobs/Skills");
						Bukkit.broadcastMessage(ChatColor.GREEN + "Downloading spawners " + ChatColor.GRAY + "[2/5]");
						mirrorFiles(repo, "spawners", "MythicMobs/Spawners");
						Bukkit.broadcastMessage(ChatColor.GREEN + "Downloading items " + ChatColor.GRAY + "[3/5]");
						mirrorFiles(repo, "items", "MythicMobs/Items");
						Bukkit.broadcastMessage(ChatColor.GREEN + "Downloading quests " + ChatColor.GRAY + "[4/5]");
						mirrorFiles(repo, "quests", "RunicQuests/quests");
						Bukkit.broadcastMessage(ChatColor.GREEN + "Done! " + ChatColor.GRAY + "[5/5]" + ChatColor.DARK_RED + " Server restarting in " + ChatColor.RED + "5" + ChatColor.DARK_RED + " seconds");
						Bukkit.getScheduler().runTaskLater(Plugin.this, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "rstop"), 5 * 20);
					} catch (Exception exception) {
						Bukkit.broadcastMessage("&aThere was an issue downloading the files from gitlab! Check the console for more information. Some files may be be missing.");
						exception.printStackTrace();
					}
				}
			});
		} else {
			sender.sendMessage(ChatColor.RED + "FilePull is already running!");
		}
		return true;
	}

	public static void writeDataFromUrl(String url, File file) throws Exception {
		URL website = new URL(url);
		URLConnection connection = website.openConnection();
		BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String inputLine;
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		while ((inputLine = reader.readLine()) != null) {
			writer.write(inputLine);
			writer.newLine();
		}
		reader.close();
		writer.close();
	}

	public static void mirrorFiles(GHRepository repo, String ghPath, String localPath) throws Exception {
		List<GHContent> mobsFolder = repo.getDirectoryContent(ghPath);
		File destination = new File(Plugin.getInstance().getDataFolder().getParent(), localPath);
		if (!destination.exists()) {
			destination.mkdirs();
		}
		for (File file : destination.listFiles()) {
			if (file.isFile()) {
				file.delete();
			}
		}
		for (GHContent content : mobsFolder) {
			File file = new File(destination, content.getName());
			if (file.exists()) {
				file.delete();
			}
			file.createNewFile();
			writeDataFromUrl(content.getDownloadUrl(), file);
		}
	}

}
