package com.tevonetwork.tevoborderline;

import org.bukkit.configuration.file.FileConfiguration;

import com.tevonetwork.tevoapi.API.Configs.ConfigFile;

public class ConfigManager {

	private ConfigFile config;
	private ConfigFile games;
	private ConfigFile signs;
	
	public void load()
	{
		TevoBorderline main = TevoBorderline.getInstance();
		this.config = new ConfigFile(main, main.getDataFolder(), "config", true);
		this.games = new ConfigFile(main, main.getDataFolder(), "games", false);
		this.signs = new ConfigFile(main, main.getDataFolder(), "signs", false);
		
		reloadConfig();
		reloadGames();
		reloadSigns();
		
	}
	
	public FileConfiguration getConfig()
	{
		return this.config.getConfig();
	}
	
	public FileConfiguration getGames()
	{
		return this.games.getConfig();
	}
	
	public FileConfiguration getSigns()
	{
		return this.signs.getConfig();
	}
	
	public void saveConfig()
	{
		this.config.save();
	}
	
	public void saveGames()
	{
		this.games.save();
	}
	
	public void saveSigns()
	{
		this.signs.save();
	}
	
	public void reloadConfig()
	{
		this.config.reload();
	}
	
	public void reloadGames()
	{
		this.games.reload();
	}
	
	public void reloadSigns()
	{
		this.signs.reload();
	}
	
}
