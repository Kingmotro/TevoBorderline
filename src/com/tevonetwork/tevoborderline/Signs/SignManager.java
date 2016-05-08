package com.tevonetwork.tevoborderline.Signs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import com.tevonetwork.tevoapi.Core.LogLevel;
import com.tevonetwork.tevoborderline.ConfigManager;
import com.tevonetwork.tevoborderline.TevoBorderline;
import com.tevonetwork.tevoborderline.Game.GameManager;

public class SignManager {

	private static TevoBorderline main = TevoBorderline.getInstance();
	private static ConfigManager cfm = main.getConfigManager();
	private static ArrayList<GameSign> signs = new ArrayList<GameSign>();
	
	public static boolean newSign(int gameID, Location loc)
	{
		if (!GameManager.doesGameExist(gameID))
		{
			return false;
		}
		if ((loc.getBlock().getType() != Material.WALL_SIGN) && (loc.getBlock().getType() != Material.SIGN_POST))
		{
			return false;
		}
		String newpath = String.valueOf(gameID);
		writeLocationtoSigns(newpath + ".location", loc);
		GameSign sign = new GameSign(gameID, loc);
		sign.update();
		signs.add(sign);
		return true;
	}
	
	public static boolean removeSign(int gameID)
	{
		if (!cfm.getSigns().contains(String.valueOf(gameID)))
		{
			return false;
		}
		for (GameSign currentsigns : signs)
		{
			if (currentsigns.getGameID() == gameID)
			{
				currentsigns.getLocation().getBlock().setType(Material.AIR);
				signs.remove(currentsigns);
				break;
			}
		}
		cfm.getSigns().set(String.valueOf(gameID), null);
		cfm.saveSigns();
		return true;
	}
	
	public static void updateSign(int gameID)
	{
		for (GameSign currentsigns : signs)
		{
			if (currentsigns.getGameID() == gameID)
			{
				currentsigns.update();
			}
		}
	}
	
	public static void updateAll()
	{
		for (GameSign currentsigns : signs)
		{
			currentsigns.update();
		}
	}
	
	public static boolean hasSign(int gameID)
	{
		for (GameSign currentsigns : signs)
		{
			if (currentsigns.getGameID() == gameID)
			{
				return true;
			}
		}
		return false;
	}
	
	public static void load()
	{
		FileConfiguration signconfig = cfm.getSigns();
		Set<String> keyset = signconfig.getKeys(false);
		if (keyset.size() <= 0)
		{
			main.getUtilLogger().logNormal("SignManager> No signs in the config to load.");
			return;
		}
		Iterator<String> itr = keyset.iterator();
		
		while (itr.hasNext())
		{
			int id = Integer.valueOf(itr.next());
			if (GameManager.doesGameExist(id))
			{
				GameSign sign = new GameSign(id, getLocationfromSigns(String.valueOf(id) + ".location"));
				signs.add(sign);
			}
			else
			{
				main.getUtilLogger().logNormal("SignManager> No game with id " + id + " exists, sign was unable to load.");
			}
		}
		
		main.getUtilLogger().logNormal("SignManager> Loaded " + keyset.size() + " signs!");
	}
	
	public static void writeLocationtoSigns(String path, Location loc)
	{
		FileConfiguration games = main.getConfigManager().getSigns();
		String world = loc.getWorld().getName();
		double x = loc.getX();
		double y = loc.getY();
		double z = loc.getZ();
		float yaw = loc.getYaw();
		float pitch = loc.getPitch();
		
		games.set(path + ".world", world);
		games.set(path + ".x", x);
		games.set(path + ".y", y);
		games.set(path + ".z", z);
		games.set(path + ".yaw", yaw);
		games.set(path + ".pitch", pitch);
		main.getConfigManager().saveSigns();
	}
	
	public static Location getLocationfromSigns(String path)
	{
		Location loc = null;
		FileConfiguration games = main.getConfigManager().getSigns();
		
		World world = Bukkit.getWorld(games.getString(path + ".world"));
		if (world != null)
		{
			double x = games.getDouble(path + ".x");
			double y = games.getDouble(path + ".y");
			double z = games.getDouble(path + ".z");
			float yaw = (float)games.getDouble(path + ".yaw");
			float pitch = (float)games.getDouble(path + ".pitch");
			loc = new Location(world, x, y, z, yaw, pitch);
		}
		else
		{
			main.getUtilLogger().logLevel(LogLevel.WARNING, "SignManager> A worldname that was pulled from signs config is invalid!");
		}
		return loc;
	}
	
}
