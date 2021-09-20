package com.runicrealms.runicfilepull;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class Plugin extends JavaPlugin {

	private static Plugin plugin;
	
	@Override
	public void onEnable() {
		plugin = this;
		this.getConfig().options().copyDefaults(true);
		this.saveConfig();
		FilePull.setFolderEnabled(FilePullFolder.MOBS, this.getConfig().getBoolean("mobs"));
		FilePull.setFolderEnabled(FilePullFolder.SKILLS, this.getConfig().getBoolean("skills"));
		FilePull.setFolderEnabled(FilePullFolder.RUNIC_ITEMS, this.getConfig().getBoolean("runicitems"));
		FilePull.setFolderEnabled(FilePullFolder.SCRIPT_ITEMS, this.getConfig().getBoolean("scriptitems"));
		FilePull.setFolderEnabled(FilePullFolder.QUESTS, this.getConfig().getBoolean("quests"));
		Bukkit.getPluginManager().registerEvents(new FilePullGui(), this);
		FilePullCommand command = new FilePullCommand();
		Bukkit.getPluginCommand("filepull").setExecutor(command);
		Bukkit.getPluginCommand("pull").setExecutor(command);
		Bukkit.getPluginCommand("fp").setExecutor(command);
		/*Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
			try {
				FilePull.grabTreeShas();
			} catch (Exception exception) {
				Bukkit.getLogger().log(Level.SEVERE, "[RunicFilePull] Failed to load TREE_SHAs!");
				Bukkit.getLogger().log(Level.SEVERE, "[RunicFilePull] Disabling this plugin!");
				Bukkit.getScheduler().runTask(Plugin.getInstance(), () -> Bukkit.getPluginManager().disablePlugin(Plugin.getInstance()));
			}
		});*/
	}
	
	public static Plugin getInstance() {
		return plugin;
	}

}
