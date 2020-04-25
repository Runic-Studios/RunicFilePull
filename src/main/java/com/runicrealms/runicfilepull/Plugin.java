package com.runicrealms.runicfilepull;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;

public class Plugin extends JavaPlugin implements CommandExecutor {

	private static Plugin plugin;
	private static boolean isRunning = false;

	private static volatile Integer totalFiles = 0;
	private static volatile Integer filesCompleted = 0;
	private static volatile Integer downloadsStarted = 0;
	
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

			Bukkit.broadcastMessage(ChatColor.GREEN + "File Pull Initiated - Server Will Restart On Completion");
			Bukkit.broadcastMessage(ChatColor.DARK_GREEN + "WARNING - Mobs and quests may break while File Pull is running!");

			Bukkit.getScheduler().runTaskAsynchronously(this, () -> {try {mirrorFiles("mobs", "MythicMobs/Mobs");} catch (Exception exception) {Bukkit.broadcastMessage(ChatColor.RED + "There was an issue downloading the mobs! Check the console for more information. Some files may be be missing."); exception.printStackTrace();}});
			Bukkit.getScheduler().runTaskAsynchronously(this, () -> {try {mirrorFiles("skills", "MythicMobs/Skills");} catch (Exception exception) {Bukkit.broadcastMessage(ChatColor.RED + "There was an issue downloading the skills! Check the console for more information. Some files may be be missing."); exception.printStackTrace();}});
			Bukkit.getScheduler().runTaskAsynchronously(this, () -> {try {mirrorFiles("spawners", "MythicMobs/Spawners");} catch (Exception exception) {Bukkit.broadcastMessage(ChatColor.RED + "There was an issue downloading spawners! Check the console for more information. Some files may be be missing."); exception.printStackTrace();}});
			Bukkit.getScheduler().runTaskAsynchronously(this, () -> {try {mirrorFiles("items", "MythicMobs/Items");} catch (Exception exception) {Bukkit.broadcastMessage(ChatColor.RED + "There was an issue downloading the items! Check the console for more information. Some files may be be missing."); exception.printStackTrace();}});
			Bukkit.getScheduler().runTaskAsynchronously(this, () -> {try {mirrorFiles("quests", "RunicQuests/quests");} catch (Exception exception) {Bukkit.broadcastMessage(ChatColor.RED + "There was an issue downloading the quests! Check the console for more information. Some files may be be missing."); exception.printStackTrace();}});

			new BukkitRunnable() {
				@Override
				public void run() {
					if (downloadsStarted == 5) {
						if (filesCompleted >= totalFiles) {
							Bukkit.broadcastMessage(ChatColor.GREEN + "Done! " + ChatColor.DARK_RED + "Server restarting in " + ChatColor.RED + "5" + ChatColor.DARK_RED + " seconds");
							Bukkit.getScheduler().runTaskLater(Plugin.this, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "rstop"), 5 * 20);
							Bukkit.getOnlinePlayers().forEach(player -> player.sendTitle("", ChatColor.DARK_RED + "Restarting in " + ChatColor.RED + "5" + ChatColor.DARK_RED + "...", 0, 40 , 0));
							Bukkit.getScheduler().runTaskLater(Plugin.this, () -> Bukkit.getOnlinePlayers().forEach(player -> player.sendTitle("", ChatColor.DARK_RED + "Restarting in " + ChatColor.RED + "4" + ChatColor.DARK_RED + "...", 0, 40 , 0)), 20L * 1);
							Bukkit.getScheduler().runTaskLater(Plugin.this, () -> Bukkit.getOnlinePlayers().forEach(player -> player.sendTitle("", ChatColor.DARK_RED + "Restarting in " + ChatColor.RED + "3" + ChatColor.DARK_RED + "...", 0, 40 , 0)), 20L * 2);
							Bukkit.getScheduler().runTaskLater(Plugin.this, () -> Bukkit.getOnlinePlayers().forEach(player -> player.sendTitle("", ChatColor.DARK_RED + "Restarting in " + ChatColor.RED + "2" + ChatColor.DARK_RED + "...", 0, 40 , 0)), 20L * 3);
							Bukkit.getScheduler().runTaskLater(Plugin.this, () -> Bukkit.getOnlinePlayers().forEach(player -> player.sendTitle("", ChatColor.DARK_RED + "Restarting in " + ChatColor.RED + "1" + ChatColor.DARK_RED + "...", 0, 40 , 0)), 20L * 4);
							this.cancel();
							return;
						}
						double percentage = Math.round(((100.0 * filesCompleted) / totalFiles * 1.0) * 10.0) / 10.0;
						for (Player player : Bukkit.getOnlinePlayers()) {
							player.sendTitle("", ChatColor.GREEN + "File Pull: " + ChatColor.YELLOW + percentage + "%", 0, 40, 0);
						}
						Bukkit.getLogger().log(Level.INFO, "[RunicFilePull] File Pull Progress: " + percentage + "%");
					} else {
						for (Player player : Bukkit.getOnlinePlayers()) {
							player.sendTitle("", ChatColor.GREEN + "File Pull: " + ChatColor.YELLOW + "Loading" + ChatColor.GREEN + "...", 0, 40, 0);
						}
						Bukkit.getLogger().log(Level.INFO, "[RunicFilePull] File Pull Progress: Loading...");
					}
				}
			}.runTaskTimerAsynchronously(this, 20L, 20L);

		} else {
			sender.sendMessage(ChatColor.RED + "FilePull is already running!");
		}
		return true;
	}

	public static void writeBase64ToFile(String base64, File file) throws Exception {
	    base64 = base64.replaceAll("\\n", "");
	    while (base64.length() % 4 > 0) {
	    	base64 += '=';
		}
	    PrintWriter writer = new PrintWriter(file);
		writer.print(Base64Coder.decodeString(base64));
        writer.close();
	}

	public static void mirrorFiles(String ghPath, String localPath) throws Exception {
		JSONArray files = (JSONArray) (new JSONParser()).parse(getWithAuth("https://api.github.com/repos/Skyfallin/writer-files/contents/" + ghPath, "runicrealmsgithub:runicrealmsPASSWORD"));
		totalFiles += files.size();
		downloadsStarted++;
		File destination = new File(Plugin.getInstance().getDataFolder().getParent(), localPath);
		if (!destination.exists()) {
			destination.mkdirs();
		}
		for (File file : destination.listFiles()) {
			if (file.exists()) {
				file.delete();
			}
		}
		for (Object object : files.toArray()) {
			File localFile = new File(destination, (String) ((JSONObject) object).get("name"));
			JSONObject gitJson = (JSONObject) (new JSONParser()).parse(getWithAuth((String) ((JSONObject) object).get("url"), "runicrealmsgithub:runicrealmsPASSWORD"));
			writeBase64ToFile((String) gitJson.get("content"), localFile);
			filesCompleted++;
		}
	}

	public static String getWithAuth(String url, String auth) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        connection.setDoOutput(true);
        connection.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8)));
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String output = "";
        String line;
        while ((line = reader.readLine()) != null) {
            output += line + "\n";
        }
        return output;
    }

}
