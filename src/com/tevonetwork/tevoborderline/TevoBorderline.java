package com.tevonetwork.tevoborderline;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import com.tevonetwork.tevoapi.API.Util.UtilLogger;
import com.tevonetwork.tevoborderline.Commands.GameCMD;
import com.tevonetwork.tevoborderline.Commands.LeaveCMD;
import com.tevonetwork.tevoborderline.Game.GameManager;
import com.tevonetwork.tevoborderline.Game.Kits.KitManager;
import com.tevonetwork.tevoborderline.Game.spectate.MenuUpdater;
import com.tevonetwork.tevoborderline.Listeners.EntityListeners;
import com.tevonetwork.tevoborderline.Listeners.PlayerListeners;
import com.tevonetwork.tevoborderline.Listeners.SignListeners;

public class TevoBorderline extends JavaPlugin{
	
	private static TevoBorderline main;
	private UtilLogger logger;
	private ConfigManager cfm;
	
	@Override
	public void onEnable()
	{
		main = this;
		checkDependencies();
		this.logger = new UtilLogger(this);
		startManagers();
		registerCMDs();
		registerListeners();
		startTasks();
		this.logger.logEnableDisable(true);
	}
	
	@Override
	public void onDisable()
	{
		this.logger.logEnableDisable(false);
	}
	
	public static TevoBorderline getInstance()
	{
		return main;
	}
	
	public UtilLogger getUtilLogger()
	{
		return this.logger;
	}
	
	public ConfigManager getConfigManager()
	{
		return this.cfm;
	}
	
	private void startManagers()
	{
		this.logger.logNormal("Plugin> Starting Managers...");
		this.cfm = new ConfigManager();
		this.cfm.load();
		GameManager.loadGames();
		this.logger.logNormal("Plugin> Managers have been started.");
	}
	
	private void registerCMDs()
	{
		this.logger.logNormal("Plugin> Registering Commands...");
		this.getCommand("game").setExecutor(new GameCMD());
		this.getCommand("leave").setExecutor(new LeaveCMD());
		this.logger.logNormal("Plugin> Commands have been registered!");
	}
	
	private void registerListeners()
	{
		this.logger.logNormal("Plugin> Registering Listeners...");
		PluginManager pm = Bukkit.getServer().getPluginManager();
		pm.registerEvents(new PlayerListeners(), this);
		pm.registerEvents(new EntityListeners(), this);
		pm.registerEvents(new SignListeners(), this);
		this.logger.logNormal("Plugin> Listeners have been registered.");
	}
	
	private void startTasks()
	{
		this.logger.logNormal("Plugin> Starting Tasks...");
		BukkitScheduler sch = Bukkit.getScheduler();
		sch.scheduleSyncRepeatingTask(this, new KitManager(), 20L, 1L);
		sch.scheduleSyncRepeatingTask(this, new MenuUpdater(), 20L, 20L);
		this.logger.logNormal("Plugin> Tasks have been started!");
	}
	
	private void checkDependencies()
	{
		if (this.getServer().getPluginManager().getPlugin("TevoAPI") == null)
		{
			this.getLogger().warning("Plugin> Could not find TevoAPI, disabling!");
			this.setEnabled(false);
		}
		else
		{
			this.getLogger().info("Plugin> Found TevoAPI!");
		}
		if (this.getServer().getPluginManager().getPlugin("LibsDisguises") == null)
		{
			this.getLogger().warning("Plugin> Could not find LibsDisguises, disabling!");
			this.setEnabled(false);
		}
		else
		{
			this.getLogger().info("Plugin> Found LibsDisguises!");
		}
	}
	
}
