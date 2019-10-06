package com.runicfilepull;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class Plugin extends JavaPlugin {

	private static Plugin plugin;
	
	@Override
	public void onEnable() {
		plugin = this;
		CommandFilePull commandExecutor = new CommandFilePull();
		String[] commands = new String[] {"filepull", "fp", "pull"};
		for (int i = 0; i < commands.length; i++) {
			PluginCommand pluginCommand = this.getCommand(commands[i]);
			pluginCommand.setExecutor(commandExecutor);
		}
	}
	
	public static Plugin getInstance() {
		return plugin;
	}
	
}
