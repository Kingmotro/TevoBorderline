package com.tevonetwork.tevoborderline.Game;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.tevonetwork.tevoapi.Core.LogLevel;
import com.tevonetwork.tevoborderline.ConfigManager;
import com.tevonetwork.tevoborderline.TevoBorderline;
import com.tevonetwork.tevoborderline.Signs.SignManager;

public class GameManager {

	private static TevoBorderline main = TevoBorderline.getInstance();
	private static ConfigManager cfm = main.getConfigManager();
	private static ArrayList<Game> games = new ArrayList<Game>();

	public static boolean newGame(String mapname, String mapauthor, Location mapcenter) {
		for (Game currentgames : games) {
			if (currentgames.getMapCenter().getWorld().getName().equalsIgnoreCase(mapcenter.getWorld().getName())) {
				main.getUtilLogger().logNormal("GameManager> A game could not be created as a game already exists in the world!");
				return false;
			}
		}
		int gameID = cfm.getGames().getInt("arenacount", -1) + 1;

		Game game = new Game(gameID, mapname, mapauthor, mapcenter);
		game.setState(GameState.ERROR);
		games.add(game);

		cfm.getGames().set("arenas." + String.valueOf(gameID) + ".mapname", mapname);
		cfm.getGames().set("arenas." + String.valueOf(gameID) + ".mapauthor", mapauthor);
		writeLocationtoGames("arenas." + String.valueOf(gameID) + ".arena.center", mapcenter);
		cfm.getGames().set("arenacount", gameID);
		cfm.saveGames();
		main.getUtilLogger().logNormal("GameManager> Created new game with ID " + game.getID());
		return true;
	}

	public static boolean deleteGame(int id) {
		if (!cfm.getGames().contains(String.valueOf(id))) {
			return false;
		}
		Game game = null;
		for (Game currentgames : games) {
			if (currentgames.getID() == id) {
				game = currentgames;
			}
		}
		if (game == null) {
			return false;
		}
		int currentcount = cfm.getGames().getInt("arenacount", 0);
		int newcount = currentcount - 1;
		game.reset();
		SignManager.removeSign(game.getID());
		cfm.getGames().set("arenacount", newcount);
		cfm.getGames().set("arenas." + String.valueOf(game.getID()), null);
		cfm.saveGames();
		main.getUtilLogger().logNormal("GameManager> Deleted game with ID " + game.getID());
		games.remove(game);
		return true;
	}

	public static boolean setGameLobby(int id, Location loc) {
		for (Game currentgames : games) {
			if (currentgames.getID() == id) {
				currentgames.setMap_Lobby(loc);
				writeLocationtoGames("arenas." + String.valueOf(id) + ".arena.lobby", loc);
				main.getUtilLogger().logNormal("GameManager> The waiting lobby of game " + currentgames.getID() + " was set! World: " + loc.getWorld().getName());
				return true;
			}
		}
		return false;
	}

	public static boolean addSpawnPoint(int id, Location loc) {
		Game game = null;
		for (Game currentgames : games) {
			if (currentgames.getID() == id) {
				game = currentgames;
				break;
			}
		}
		if (game == null) {
			return false;
		}
		if (game.getSpawnpoints().size() >= 20) {
			return false;
		}
		game.addSpawnpoint(loc);
		writeLocationtoGames("arenas." + String.valueOf(id) + ".arena.spawnpoints." + String.valueOf(game.getSpawnpoints().size()), loc);
		main.getUtilLogger().logNormal("GameManager> Spawnpoint was added to game " + game.getID());
		return true;
	}

	public static boolean removeLastSpawnPoint(int id) {
		Game game = null;
		for (Game currentgames : games) {
			if (currentgames.getID() == id) {
				game = currentgames;
				break;
			}
		}
		if (game == null) {
			return false;
		}
		game.removeLastSpawnpoint();
		if (cfm.getGames().contains(String.valueOf(id) + "arena.spawnpoints")) {
			Set<String> spawnpoints = cfm.getGames().getConfigurationSection(String.valueOf(id) + "arena.spawnpoints").getKeys(false);
			Iterator<String> itr = spawnpoints.iterator();
			while (itr.hasNext()) {
				String path = itr.next();
				if (!itr.hasNext()) {
					cfm.getGames().set(String.valueOf(id) + "arena.spawnpoints." + path, null);
				}
			}
			cfm.saveGames();
		}
		main.getUtilLogger().logNormal("GameManager> Spawnpoint removed from game " + game.getID());
		return true;
	}

	public static boolean doesGameExist(int id) {
		for (Game currentgames : games) {
			if (currentgames.getID() == id) {
				return true;
			}
		}
		return false;
	}

	public static void addKill(Player p) {
		for (Game currentgames : games) {
			currentgames.addKill(p);
		}
	}

	public static void addAssist(Player p) {
		for (Game currentgames : games) {
			currentgames.addAssist(p);
		}
	}

	public static boolean isPlaying(Player p) {
		for (Game currentgames : games) {
			if (currentgames.containsPlayer(p)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isSpectating(Player p) {
		for (Game currentgames : games) {
			if (currentgames.containsSpectator(p)) {
				return true;
			}
		}
		return false;
	}

	public static void killPlayer(Player p) {
		for (Game currentgames : games) {
			if (currentgames.containsPlayer(p)) {
				currentgames.killPlayer(p);
			}
		}
	}

	public static Game getPlayerGame(Player p) {
		Game game = null;
		for (Game currentgames : games) {
			if ((currentgames.containsPlayer(p)) || (currentgames.containsSpectator(p))) {
				game = currentgames;
				break;
			}
		}
		return game;
	}

	public static void joinPlayer(Player p, int id) {
		for (Game currentgames : games) {
			if (currentgames.getID() == id) {
				currentgames.addPlayer(p);
			}
		}
	}

	public static void forceStartGame(int id) {
		for (Game currentgames : games) {
			if (currentgames.getID() == id) {
				currentgames.forceStart(null);
				break;
			}
		}
	}

	public static int waitingGames() {
		int amount = 0;
		for (Game currentgames : games) {
			if (currentgames.getGameState() == GameState.WAITING) {
				amount++;
			}
		}
		return amount;
	}

	public static int gamesInProgress() {
		int amount = 0;
		for (Game currentgames : games) {
			if (currentgames.getGameState() == GameState.INGAME) {
				amount++;
			}
		}
		return amount;
	}

	public static void handleJoin(Player p) {
		for (Game currentgames : games) {
			currentgames.handleJoin(p);
		}
	}

	public static void removePlayer(Player p, boolean logout) {
		for (Game currentgames : games) {
			if ((currentgames.containsPlayer(p)) || (currentgames.containsSpectator(p))) {
				currentgames.removePlayer(p, logout);
			}
		}
	}

	public static int getLoadedGamesCount() {
		return games.size();
	}

	public static ArrayList<Game> getLoadedGames() {
		return games;
	}

	public static Game getGame(int id) {
		Game game = null;
		for (Game currentgames : games) {
			if (currentgames.getID() == id) {
				game = currentgames;
				break;
			}
		}
		return game;
	}

	public static void loadGames() {
		if (!cfm.getGames().contains("arenas")) {
			main.getUtilLogger().logNormal("GameManager> There are no games to load!");
			return;
		}
		Set<String> keys = cfm.getGames().getConfigurationSection("arenas").getKeys(false);
		if (keys.size() <= 0) {
			main.getUtilLogger().logNormal("GameManager> There were no games to load!");
			return;
		}
		Iterator<String> itr = keys.iterator();
		int loaded_games = 0;
		while (itr.hasNext()) {
			String path = itr.next();
			Game game = new Game(Integer.valueOf(path), cfm.getGames().getString("arenas." + path + ".mapname", "_"), cfm.getGames().getString("arenas." + path + ".mapauthor", "Tevo Build Team"),
					getLocationfromGames("arenas." + path + ".arena.center"));
			Set<String> spawnpoints = cfm.getGames().getConfigurationSection("arenas." + path + ".arena.spawnpoints").getKeys(false);
			if (spawnpoints.size() < 20) {
				main.getUtilLogger().logLevel(LogLevel.WARNING, "GameManager> Game " + path + " could not be loaded, insufficient amount of spawnpoints.");
				break;
			}
			Iterator<String> spitr = spawnpoints.iterator();
			List<Location> spanwpointslist = new ArrayList<Location>();

			while (spitr.hasNext()) {
				String sppath = spitr.next();
				Location spawnpoint = getLocationfromGames("arenas." + path + ".arena.spawnpoints." + sppath);
				spanwpointslist.add(spawnpoint);
			}
			game.setMap_Lobby(getLocationfromGames("arenas." + path + ".arena.lobby"));
			game.setSpawnpoints(spanwpointslist);

			main.getUtilLogger().logNormal("GameManager> Loaded game " + path + " with " + spanwpointslist.size() + " spawnpoints.");

			game.setState(GameState.WAITING);
			games.add(game);
			loaded_games++;
		}
		main.getUtilLogger().logNormal("GameManager> " + loaded_games + " games were loaded!");
		SignManager.load();
	}

	public static void setLobbySpawn(Location loc) {
		FileConfiguration config = main.getConfigManager().getConfig();
		String world = loc.getWorld().getName();
		double x = loc.getX();
		double y = loc.getY();
		double z = loc.getZ();
		float yaw = loc.getYaw();
		float pitch = loc.getPitch();

		config.set("lobby.world", world);
		config.set("lobby.x", x);
		config.set("lobby.y", y);
		config.set("lobby.z", z);
		config.set("lobby.yaw", yaw);
		config.set("lobby.pitch", pitch);
		main.getConfigManager().saveConfig();
	}

	public static Location getLobbySpawn() {
		Location loc = null;
		FileConfiguration config = main.getConfigManager().getConfig();

		World world = Bukkit.getWorld(config.getString("lobby.world"));
		if (world != null) {
			double x = config.getDouble("lobby.x");
			double y = config.getDouble("lobby.y");
			double z = config.getDouble("lobby.z");
			float yaw = (float) config.getDouble("lobby.yaw");
			float pitch = (float) config.getDouble("lobby.pitch");
			loc = new Location(world, x, y, z, yaw, pitch);
		}
		else {
			main.getUtilLogger().logLevel(LogLevel.WARNING, "GameManager> The lobby world name for game lobby is invalid!");
		}
		return loc;
	}

	public static void writeLocationtoGames(String path, Location loc) {
		FileConfiguration gamesconf = main.getConfigManager().getGames();
		String world = loc.getWorld().getName();
		double x = loc.getX();
		double y = loc.getY();
		double z = loc.getZ();
		float yaw = loc.getYaw();
		float pitch = loc.getPitch();

		gamesconf.set(path + ".world", world);
		gamesconf.set(path + ".x", x);
		gamesconf.set(path + ".y", y);
		gamesconf.set(path + ".z", z);
		gamesconf.set(path + ".yaw", yaw);
		gamesconf.set(path + ".pitch", pitch);
		main.getConfigManager().saveGames();
	}

	public static Location getLocationfromGames(String path) {
		Location loc = null;
		FileConfiguration gamesconf = main.getConfigManager().getGames();

		World world = Bukkit.getWorld(gamesconf.getString(path + ".world"));
		if (world != null) {
			double x = gamesconf.getDouble(path + ".x");
			double y = gamesconf.getDouble(path + ".y");
			double z = gamesconf.getDouble(path + ".z");
			float yaw = (float) gamesconf.getDouble(path + ".yaw");
			float pitch = (float) gamesconf.getDouble(path + ".pitch");
			loc = new Location(world, x, y, z, yaw, pitch);
		}
		else {
			main.getUtilLogger().logLevel(LogLevel.WARNING, "GameManager> A worldname that was pulled from games config is invalid!");
		}
		return loc;
	}
}
