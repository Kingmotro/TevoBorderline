package com.tevonetwork.tevoborderline.Stats;

import org.bukkit.entity.Player;

import com.tevonetwork.tevoapi.API.Stats.StatManager;
import com.tevonetwork.tevoapi.Core.Gamemodes;

public class StatsManager {

	public static void addKill(Player p)
	{
		StatManager.addKills(p, Gamemodes.BORDERLINE, 1);
	}
	
	public static void addGame(Player p)
	{
		StatManager.addGames(p, Gamemodes.BORDERLINE, 1);
	}
	
	public static void addWin(Player p)
	{
		StatManager.addWins(p, Gamemodes.BORDERLINE, 1);
	}
	
}
