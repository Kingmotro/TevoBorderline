package com.tevonetwork.tevoborderline.Game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Difficulty;
import org.bukkit.Effect;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import com.tevonetwork.tevoapi.TevoAPI;
import com.tevonetwork.tevoapi.API.Math.TimeUtils;
import com.tevonetwork.tevoapi.API.Scoreboards.ScoreboardManager;
import com.tevonetwork.tevoapi.API.Titles.ActionBar;
import com.tevonetwork.tevoapi.API.Titles.Title;
import com.tevonetwork.tevoapi.API.Util.CC;
import com.tevonetwork.tevoapi.API.Util.ItemStackFactory;
import com.tevonetwork.tevoapi.API.Util.UtilPlayer;
import com.tevonetwork.tevoapi.API.Util.UtilPlayer.playerSounds;
import com.tevonetwork.tevoapi.Core.Category;
import com.tevonetwork.tevoapi.Core.Gamemodes;
import com.tevonetwork.tevoapi.Core.Items;
import com.tevonetwork.tevoapi.Core.Rank;
import com.tevonetwork.tevoapi.Core.Travel.SendtoLocation;
import com.tevonetwork.tevoapi.Core.common.patch.FixWorldBorder;
import com.tevonetwork.tevoapi.Economy.EconomyManager;
import com.tevonetwork.tevoborderline.TevoBorderline;
import com.tevonetwork.tevoborderline.Signs.SignManager;
import com.tevonetwork.tevoborderline.Stats.StatsManager;

public class Game {

	private TevoBorderline main = TevoBorderline.getInstance();
	private long seconds_Remaining;
	private long game_seconds = 600;
	private long lobby_wait = 30;
	private long lobby_wait_default = 30;
	private BukkitTask scoreboard_Animation;
	private BukkitTask prestart_Task;
	private BukkitTask countdown_Task;
	private BukkitTask bordercheck_Task;
	private int scoreboard_Animation_frame = 0;
	private int ID;
	private int game_Min_Players = 6;
	private int game_Max_Players = 20;
	private int reward_Kill = 5;
	private int reward_Assist = 1;
	private int reward_Win = 20;
	private int reward_Participation = 10;
	private double game_Border_size = 324;
	private String map_Author;
	private String map_Name;
	private Scoreboard game_board;
	private Objective game_objective;
	private Score lobby_status;
	private Score lobby_players;
	private Score game_Players_Remaining;
	private Score game_Time;
	private Score game_map_head;
	private Score game_map;
	private Team game_Players_team;
	private Team game_Spectators_team;
	private GameState state;
	private Location map_Center;
	private Location map_Lobby;
	private List<Location> map_Spawnpoints = new ArrayList<Location>();
	private ArrayList<Player> game_Players = new ArrayList<Player>();
	private ArrayList<Player> game_Spectators = new ArrayList<Player>();
	private HashMap<Player, Integer> earnings_Assists = new HashMap<Player, Integer>();
	private HashMap<Player, Integer> earnings_Kills = new HashMap<Player, Integer>();
	private Player game_Winner;
	private WorldBorder arena_border;

	// Game Mechanics
	public Game(int id, String mapname, String mapauthor, Location mapcenter) {
		this.ID = id;
		this.map_Name = mapname;
		this.map_Author = mapauthor;
		this.map_Center = mapcenter;

		this.game_board = Bukkit.getServer().getScoreboardManager().getNewScoreboard();

		this.game_objective = this.game_board.registerNewObjective("Borderline", "dummy");
		this.game_objective.setDisplayName(CC.tnValue + CC.fBold + "Borderline");
		this.game_objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		this.game_objective.getScore("     ");
		this.game_objective.getScore("     ").setScore(1);
		this.game_objective.getScore("tevonetwork.com");
		this.game_objective.getScore("tevonetwork.com").setScore(0);
		lobbySetup();
		this.game_Players_team = this.game_board.registerNewTeam("Players");
		this.game_Spectators_team = this.game_board.registerNewTeam("Spectators");
		this.game_Players_team.setAllowFriendlyFire(true);
		this.game_Players_team.setCanSeeFriendlyInvisibles(false);
		this.game_Players_team.setPrefix(CC.cYellow);
		this.game_Spectators_team.setCanSeeFriendlyInvisibles(true);
		this.game_Spectators_team.setAllowFriendlyFire(true);
		this.game_Spectators_team.setPrefix(CC.cD_Gray);

		this.arena_border = this.map_Center.getWorld().getWorldBorder();
		this.arena_border.setCenter(this.map_Center);
		this.arena_border.setSize(this.game_Border_size);
		new FixWorldBorder(this.map_Center.getWorld());
		this.map_Center.getWorld().setDifficulty(Difficulty.NORMAL);
		this.scoreboard_Animation = new BukkitRunnable() {

			@Override
			public void run() {
				game_objective.setDisplayName(scoreboard_frames.get(scoreboard_Animation_frame));
				scoreboard_Animation_frame++;
				if (scoreboard_Animation_frame >= scoreboard_frames.size()) {
					scoreboard_Animation_frame = 0;
				}
			}
		}.runTaskTimer(main, 0L, 2L);

		this.state = GameState.STARTUP;
	}

	public void preStart() {
		this.prestart_Task = new BukkitRunnable() {

			@Override
			public void run() {
				if (lobby_wait >= lobby_wait_default) {
					for (Player players : game_Players) {
						UtilPlayer.message(Category.GAME, players, CC.tnInfo + "The game starts in " + CC.tnValue + lobby_wait_default + " seconds" + CC.end);
					}
				}
				if (lobby_wait == 10) {
					state = GameState.STARTING;
					for (Player players : game_Players) {
						UtilPlayer.message(Category.GAME, players, CC.tnInfo + "The game starts in " + CC.tnValue + "10 seconds" + CC.end);
					}
				}
				if ((lobby_wait <= 5) && (lobby_wait > 0)) {
					state = GameState.STARTING;
					for (Player players : game_Players) {
						UtilPlayer.message(Category.GAME, players, CC.tnInfo + "The game starts in " + CC.tnValue + lobby_wait + " seconds" + CC.end);
						UtilPlayer.sound(players, playerSounds.COUNTDOWN);
					}
				}
				if (lobby_wait <= 0) {
					start();
					cancel();
				}
				updateLobbyStatus(CC.tnInfo + "Starting in " + CC.tnValue + lobby_wait + " seconds");
				lobby_wait--;
			}
		}.runTaskTimer(main, 0L, 20L);
	}

	private void cancelPreStart() {
		if (this.prestart_Task != null) {
			this.prestart_Task.cancel();
			updateLobbyStatus(CC.tnInfo + "Waiting for players...");
		}
	}

	private void cancelCountdown() {
		if (this.countdown_Task != null) {
			this.countdown_Task.cancel();
		}
	}

	private void cancelBorderCheckTask() {
		if (this.bordercheck_Task != null) {
			this.bordercheck_Task.cancel();
		}
	}

	public void forceStart(String actorname) {
		if (actorname == null) {
			for (Player players : game_Players) {
				UtilPlayer.message(Category.GAME, players, CC.tnInfo + CC.fBold + "The game has been started!");
				UtilPlayer.sound(players, playerSounds.COUNTDOWN);
			}
		}
		else {
			for (Player players : game_Players) {
				UtilPlayer.message(Category.GAME, players, CC.tnInfo + CC.fBold + "The game has been started by " + actorname + CC.tnInfo + CC.fBold + " !");
				UtilPlayer.sound(players, playerSounds.COUNTDOWN);
			}
		}
		this.state = GameState.STARTING;
		this.lobby_wait = 10;
		preStart();
	}

	public void start() {
		this.lobby_wait = this.lobby_wait_default; // Reset Lobby Time
		this.seconds_Remaining = this.game_seconds; // Reset game time
		gameSetup(); // Setup Scoreboard
		startWorldBorder(); // Start World border
		teleportPlayers(); // Teleport players to spawnpoints
		ItemStackFactory isf = new ItemStackFactory(); // Equip weapon
		ItemStack rifle = isf.createItemStack(Items.STICK, CC.tnAbility + "Rifle");
		ItemStack leatherhelmet = new ItemStack(Material.LEATHER_HELMET);
		ItemStack leatherchestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
		ItemStack leatherleggings = new ItemStack(Material.LEATHER_LEGGINGS);
		ItemStack leatherboots = new ItemStack(Material.LEATHER_BOOTS);
		for (Player players : game_Players) {
			this.earnings_Kills.put(players, 0);
			this.earnings_Assists.put(players, 0);
			players.setGameMode(GameMode.SURVIVAL);
			players.playSound(players.getLocation(), Sound.WITHER_DEATH, 2F, 0.5F);
			UtilPlayer.message(Category.GAME, players, CC.tnInfo + CC.fBold + "The game has started!");
			sendGameInfo(players);
			UtilPlayer.message(Category.KIT, players, CC.tnInfo + "You " + CC.tnEnable + "equipped " + CC.tnValue + "Borderline Start Kit" + CC.end);
			UtilPlayer.message(Category.GAME, players, CC.tnInfo + "When " + CC.tnValue + "2 minutes " + CC.tnInfo + "have elapsed you will be equipped with a random weapon.");
			UtilPlayer.clearInv(players);
			players.getInventory().setItem(0, rifle);
			players.getInventory().setHelmet(leatherhelmet);
			players.getInventory().setChestplate(leatherchestplate);
			players.getInventory().setLeggings(leatherleggings);
			players.getInventory().setBoots(leatherboots);
			players.updateInventory();
			UtilPlayer.message(Category.GAME, players, CC.tnInfo + CC.fBold + "Your exp level shows how many kills you have gained.");
			Title title = new Title();
			title.setTitle("");
			title.setSubtitle(CC.cRED + CC.fBold + "Keep away from the border!");
			title.setfadeIn(10);
			title.setStay(40);
			title.setfadeOut(10);
			title.send(players);
			EconomyManager.addTokens(players, this.reward_Participation * UtilPlayer.multiplier(players));
			StatsManager.addGame(players);
		}
		this.state = GameState.INGAME;
		countdown();
		SignManager.updateSign(this.ID);
		main.getUtilLogger().logNormal("Game> " + this.ID + " has started!");
	}

	public void end() {
		main.getUtilLogger().logNormal("Game> " + this.ID + " is ending!");
		cancelCountdown();
		cancelBorderCheckTask();
		this.state = GameState.FINISHING;
		SignManager.updateSign(this.ID);
		this.game_objective.setDisplaySlot(null);
		for (Player players : game_Players) {
			UtilPlayer.clearInv(players);
			players.setHealth(20.0);
		}
		int highkills = 0;
		for (Player players : earnings_Kills.keySet()) {
			if (earnings_Kills.get(players) > highkills) {
				highkills = earnings_Kills.get(players);
				this.game_Winner = players;
			}
		}
		if (this.game_Players.size() == 1) {
			this.game_Winner = this.game_Players.get(0);
		}
		if (this.game_Winner == null) {
			for (Player players : game_Players) {
				UtilPlayer.message(Category.GAME, players, CC.tnInfo + CC.fBold + "Nobody won the game.");
			}
			for (Player players : game_Spectators) {
				UtilPlayer.message(Category.GAME, players, CC.tnInfo + CC.fBold + "Nobody won the game.");
			}
		}
		else {
			for (Player players : game_Players) {
				UtilPlayer.message(Category.GAME, players, this.game_Winner.getDisplayName() + CC.tnInfo + CC.fBold + " won the game!");
			}
			for (Player players : game_Spectators) {
				UtilPlayer.message(Category.GAME, players, this.game_Winner.getDisplayName() + CC.tnInfo + CC.fBold + " won the game!");
			}
			new BukkitRunnable() {
				int runs = 0;

				@Override
				public void run() {
					Firework firework = (Firework) game_Winner.getWorld().spawnEntity(game_Winner.getLocation(), EntityType.FIREWORK);
					FireworkMeta fm = firework.getFireworkMeta();
					FireworkEffect effect = FireworkEffect.builder().with(Type.BALL).withColor(Color.GREEN).flicker(true).build();
					fm.addEffect(effect);
					fm.setPower(2);
					firework.setFireworkMeta(fm);
					if (runs >= 5) {
						cancel();
					}
					runs++;
				}
			}.runTaskTimer(main, 0L, 15L);
			EconomyManager.addTokens(this.game_Winner, this.reward_Win * UtilPlayer.multiplier(this.game_Winner));
			if (UtilPlayer.multiplier(this.game_Winner) > 0) {
				UtilPlayer.transaction(this.game_Winner, this.reward_Win * UtilPlayer.multiplier(this.game_Winner), "winning the game",
						new String[] { Rank.getRankPrefix(UtilPlayer.getRank(this.game_Winner)) + CC.tnValue + " Rank" });
			}
			else {
				UtilPlayer.transaction(this.game_Winner, this.reward_Win, "winning the game", null);
			}
			UtilPlayer.sound(this.game_Winner, playerSounds.TRANSACTIONSUCCESS);
			StatsManager.addWin(this.game_Winner);
		}

		for (Player players : earnings_Kills.keySet()) {
			sendEarnings(players);
		}

		new BukkitRunnable() {

			@Override
			public void run() {
				reset();
			}
		}.runTaskLater(main, 200L);
	}

	public void reset() {
		main.getUtilLogger().logNormal("Game> " + this.ID + " is resetting!");
		for (Player players : game_Players) {
			players.teleport(GameManager.getLobbySpawn());
			players.setLevel(0);
			players.setHealth(20.0);
			players.setAllowFlight(false);
			players.setFlying(false);
			players.setGameMode(GameMode.ADVENTURE);
			players.removePotionEffect(PotionEffectType.INVISIBILITY);
			players.removePotionEffect(PotionEffectType.WITHER);
			players.removePotionEffect(PotionEffectType.NIGHT_VISION);
			ScoreboardManager.setupScoreboard(players, Gamemodes.BORDERLINE);
			for (Player online : Bukkit.getOnlinePlayers()) {
				players.showPlayer(online);
			}
		}
		for (Player players : game_Spectators) {
			players.teleport(GameManager.getLobbySpawn());
			players.setLevel(0);
			players.setHealth(20.0);
			players.setAllowFlight(false);
			players.setFlying(false);
			players.setGameMode(GameMode.ADVENTURE);
			players.removePotionEffect(PotionEffectType.INVISIBILITY);
			players.removePotionEffect(PotionEffectType.WITHER);
			players.removePotionEffect(PotionEffectType.NIGHT_VISION);
			ScoreboardManager.setupScoreboard(players, Gamemodes.BORDERLINE);
			for (Player online : Bukkit.getOnlinePlayers()) {
				players.showPlayer(online);
			}
		}
		this.game_Players.clear();
		this.earnings_Kills.clear();
		this.earnings_Assists.clear();
		this.game_Spectators.clear();
		this.game_Winner = null;
		this.prestart_Task = null;
		this.bordercheck_Task = null;
		int drops = 0;
		for (Entity entities : this.map_Center.getWorld().getEntities()) {
			if (entities instanceof Item) {
				entities.remove();
				drops++;
			}
		}
		main.getUtilLogger().logNormal("Game> Drops in game " + this.ID + " have been cleared! (" + drops + ")");
		this.lobby_wait = this.lobby_wait_default;
		this.seconds_Remaining = this.game_seconds;
		lobbySetup();
		this.game_objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		this.arena_border.setSize(this.game_Border_size);
		this.arena_border.setCenter(this.map_Center);
		new FixWorldBorder(this.map_Center.getWorld());
		this.state = GameState.WAITING;
		SignManager.updateSign(this.ID);
		main.getUtilLogger().logNormal("Game> " + this.ID + " has been reset!");
	}

	private void countdown() {
		this.countdown_Task = new BukkitRunnable() {

			@Override
			public void run() {
				updateGameCountdown();
				if ((seconds_Remaining <= 0) || (game_Players.size() <= 1)) {
					end();
				}
				if (seconds_Remaining == (game_seconds - 120)) {
					ItemStackFactory isf = new ItemStackFactory();
					ItemStack grenadelauncher = isf.createItemStack(Items.BLAZEROD, CC.tnAbility + "Grenade Launcher");
					ItemStack witherheadgun = isf.createItemStack(Items.TRIPWIREHOOK, CC.tnAbility + "Wither Launcher");
					ItemStack ballisticknife = isf.createItemStack(Items.IRONAXE, CC.tnAbility + "Throwing Axe");
					for (Player players : game_Players) {
						double random = Math.random();
						if (random < 0.5) {
							players.getInventory().setItem(1, ballisticknife);
							UtilPlayer.message(Category.GAME, players, CC.tnInfo + "You got the throwing axe! (" + CC.tnValue + "50%" + CC.tnInfo + "Chance)");
							UtilPlayer.message(Category.ABILITY, players, CC.tnUse + "Right Click " + CC.tnAbility + "Axe" + CC.tnInfo + " to use " + CC.tnAbility + "Throwing Axe" + CC.end);
							if (UtilPlayer.hasRank(players, Rank.CRYSTAL)) {
								UtilPlayer.message(Category.GAME, players, Rank.getRankPrefix(Rank.CRYSTAL) + CC.tnInfo + " rank means nobody else can pickup your throwing axe!");
							}
							UtilPlayer.sound(players, playerSounds.EQUIP);
						}
						else if (random < 0.85) {
							players.getInventory().setItem(1, grenadelauncher);
							UtilPlayer.message(Category.GAME, players, CC.tnInfo + "You were lucky and got the grenade launcher! (" + CC.tnValue + "35%" + CC.tnInfo + "Chance)");
							UtilPlayer.message(Category.ABILITY, players, CC.tnUse + "Right Click " + CC.tnAbility + "Blaze Rod" + CC.tnInfo + " to use " + CC.tnAbility + "Throwing Axe" + CC.end);
							UtilPlayer.sound(players, playerSounds.EQUIP);
						}
						else {
							players.getInventory().setItem(1, witherheadgun);
							UtilPlayer.message(Category.GAME, players, CC.tnInfo + "You were super lucky and got the wither launcher! (" + CC.tnValue + "15%" + CC.tnInfo + "Chance)");
							UtilPlayer.message(Category.ABILITY, players,
									CC.tnUse + "Right Click " + CC.tnAbility + "Tripwire Hook" + CC.tnInfo + " to charge " + CC.tnAbility + " Wither Launcher" + CC.end);
							UtilPlayer.sound(players, playerSounds.EQUIP);
						}
					}
				}
				seconds_Remaining--;
			}
		}.runTaskTimer(main, 0L, 20L);
	}

	private void startWorldBorder() {
		this.bordercheck_Task = new BukkitRunnable() {

			@Override
			public void run() {
				for (Player players : game_Players) {
					double bcx = arena_border.getCenter().getX();
					double bcz = arena_border.getCenter().getZ();

					double px = players.getLocation().getX();
					double pz = players.getLocation().getZ();

					double bradius = arena_border.getSize() / 2;
					double threshold = bradius - 6;

					double minx = bcx - threshold;
					double minz = bcz - threshold;

					double maxx = bcx + threshold;
					double maxz = bcz + threshold;

					if ((px > maxx) || (pz > maxz) || (pz < minz) || (px < minx)) {
						players.playSound(players.getLocation(), Sound.FIZZ, 2F, 0.8F);
						players.getWorld().spigot().playEffect(players.getLocation(), Effect.POTION_SWIRL, 0, 0, 1F, 1F, 1F, 0, 20, 20);
						players.damage(3.0);
						UtilPlayer.setLastDamageCause(players, "Border");
						ActionBar barmsg = new ActionBar(CC.tnBError + "STAY BACK FROM THE BORDER!");
						barmsg.send(players);
					}
				}
			}
		}.runTaskTimer(main, 20L, 3L);

		this.arena_border.setCenter(this.map_Center);
		this.arena_border.setSize(30, 600);
		new FixWorldBorder(map_Center.getWorld());

	}

	private void teleportPlayers() {
		randomizeSpawnpoints();
		Iterator<Location> itr = map_Spawnpoints.iterator();
		for (Player players : game_Players) {
			if (!itr.hasNext()) {
				removePlayer(players, false);
			}
			players.teleport(itr.next());
		}
	}

	private void randomizeSpawnpoints() {
		Collections.shuffle(this.map_Spawnpoints);
	}

	// Scoreboard
	private void lobbySetup() {
		if (this.game_Time != null) {
			this.game_board.resetScores(this.game_Time.getEntry());
			this.game_board.resetScores(this.game_Players_Remaining.getEntry());
			this.game_board.resetScores(this.game_objective.getScore("   ").getEntry());
			this.game_board.resetScores(this.game_map.getEntry());
			this.game_board.resetScores(this.game_map_head.getEntry());
		}
		this.game_objective.getScore(" ");
		this.game_objective.getScore(" ").setScore(15);
		;
		this.lobby_status = this.game_objective.getScore(CC.tnInfo + "Waiting for players...");
		this.lobby_status.setScore(14);
		this.game_objective.getScore("  ");
		this.game_objective.getScore("  ").setScore(13);
		this.lobby_players = this.game_objective.getScore(CC.tnInfo + "Players: " + CC.tnValue + game_Players.size() + CC.tnInfo + "/" + CC.tnValue + "20");
		this.lobby_players.setScore(12);
	}

	private void gameSetup() {
		this.game_board.resetScores(this.lobby_status.getEntry());
		this.game_board.resetScores(this.lobby_players.getEntry());
		this.game_Time = this.game_objective.getScore(CC.tnInfo + "Time: " + CC.tnValue + TimeUtils.fromSecondstoMS(this.seconds_Remaining));
		this.game_Time.setScore(14);
		this.game_Players_Remaining = this.game_objective.getScore(CC.tnInfo + "Players Left: " + CC.tnValue + String.valueOf(this.game_Players.size()));
		this.game_Players_Remaining.setScore(12);
		this.game_objective.getScore("   ");
		this.game_objective.getScore("   ").setScore(11);
		this.game_map_head = this.game_objective.getScore(CC.tnInfo + "Map:");
		this.game_map_head.setScore(10);
		this.game_map = this.game_objective.getScore(CC.tnValue + this.map_Name);
		this.game_map.setScore(9);
	}

	private void updateGamePlayers() {
		if (this.state != GameState.INGAME) {
			return;
		}
		this.game_board.resetScores(this.game_Players_Remaining.getEntry());
		this.game_Players_Remaining = this.game_objective.getScore(CC.tnInfo + "Players Left: " + CC.tnValue + String.valueOf(this.game_Players.size()));
		this.game_Players_Remaining.setScore(12);
	}

	private void updateGameCountdown() {
		if (this.state != GameState.INGAME) {
			return;
		}
		this.game_board.resetScores(this.game_Time.getEntry());
		this.game_Time = this.game_objective.getScore(CC.tnInfo + "Time: " + CC.tnValue + TimeUtils.fromSecondstoMS(this.seconds_Remaining));
		this.game_Time.setScore(14);
	}

	private void updateLobbyStatus(String status) {
		if (lobby_status == null) {
			this.state = GameState.ERROR;
			return;
		}
		if ((this.state != GameState.WAITING) && (this.state != GameState.STARTING)) {
			return;
		}
		this.game_board.resetScores(this.lobby_status.getEntry());
		this.lobby_status = this.game_objective.getScore(status);
		this.lobby_status.setScore(14);
	}

	private void updateLobbyPlayers() {
		if (this.lobby_players == null) {
			this.state = GameState.ERROR;
			return;
		}
		if ((this.state != GameState.WAITING) && (this.state != GameState.STARTING)) {
			return;
		}
		this.game_board.resetScores(this.lobby_players.getEntry());
		this.lobby_players = this.game_objective.getScore(CC.tnInfo + "Players: " + CC.tnValue + game_Players.size() + CC.tnInfo + "/" + CC.tnValue + this.game_Max_Players);
		this.lobby_players.setScore(12);
	}

	// Player Methods (Join, Quit, Spectator, Messaging)
	public void addPlayer(Player p) {
		if (this.map_Lobby == null) {
			this.state = GameState.ERROR;
			SignManager.updateSign(this.ID);
			return;
		}
		if ((this.state == GameState.ERROR) || (this.state == GameState.STARTUP)) {
			return;
		}
		if (this.state == GameState.STARTING) {
			UtilPlayer.message(Category.GAME, p, CC.tnError + "This game is already starting, please try another one.");
			return;
		}
		if (this.state == GameState.FINISHING) {
			UtilPlayer.message(Category.GAME, p, CC.tnError + "This game is finishing, please try again when the status changes to waiting.");
		}
		if (this.state == GameState.INGAME) {
			if (UtilPlayer.hasRank(p, Rank.MODERATOR)) {
				addSpectator(p);
				main.getUtilLogger().logNormal("Game> Staff: " + p.getName() + " is spectating game " + this.ID + "!");
				return;
			}
			else {
				UtilPlayer.message(Category.GAME, p, CC.tnError + "Game in progress, please try another one.");
				return;
			}
		}
		if (this.game_Players.size() >= this.game_Max_Players) {
			UtilPlayer.message(Category.GAME, p, CC.tnError + "Game is full, please try another one.");
			return;
		}
		p.teleport(this.map_Lobby);
		this.game_Players.add(p);
		UtilPlayer.clearInv(p);
		updateLobbyPlayers();
		p.setScoreboard(this.game_board);
		p.setLevel(0);
		p.setExp(0F);
		p.setGameMode(GameMode.ADVENTURE);
		this.game_Players_team.addEntry(p.getName());
		for (Player ingame : game_Players) {
			UtilPlayer.message(Category.GAME, ingame, p.getDisplayName() + CC.tnInfo + " has joined the game!");
		}
		UtilPlayer.message(Category.GAME, p, CC.tnInfo + "Do " + CC.tnUse + "/leave" + CC.tnInfo + " to back out of this game!");
		if (game_Players.size() >= this.game_Min_Players) {
			preStart();
		}
		else {
			int required = 8 - game_Players.size();
			for (Player ingame : game_Players) {
				UtilPlayer.message(Category.GAME, ingame, CC.tnValue + required + CC.tnInfo + " more players required to start!");
			}
		}

		if (this.game_Players.size() >= this.game_Max_Players) {
			this.state = GameState.FULL;
		}

		for (Player players : Bukkit.getOnlinePlayers()) {
			if (!game_Players.contains(players)) {
				p.hidePlayer(players);
			}
		}

		for (Player players : game_Players) {
			players.showPlayer(p);
		}
		SignManager.updateSign(this.ID);
	}

	private void addSpectator(final Player p) {
		for (Player players : Bukkit.getOnlinePlayers()) {
			if (!this.game_Players.contains(players)) {
				p.hidePlayer(players);
			}
		}
		for (Player players : game_Players) {
			players.hidePlayer(p);
		}
		this.game_Spectators.add(p);
		p.teleport(TevoAPI.getInstance().getWorldManager().getWorldSpawn(this.map_Center.getWorld().getName()));
		this.game_Spectators_team.addEntry(p.getName());
		p.setGameMode(GameMode.SURVIVAL);
		p.setAllowFlight(true);
		p.setFlying(true);
		p.setFlySpeed(0.1F);
		p.setLevel(0);
		p.removePotionEffect(PotionEffectType.WITHER);
		UtilPlayer.message(Category.GAME, p, CC.tnInfo + "You are now spectating game " + CC.tnValue + this.ID + CC.end);
		UtilPlayer.clearInv(p);
		p.updateInventory();
		new BukkitRunnable() {
			@Override
			public void run() {
				ItemStackFactory isf = new ItemStackFactory();
				ItemStack spectate = isf.createItemStack(Items.COMPASS, CC.tnValue + "Spectate" + CC.tnUse + " (Right Click)");
				ItemStack lobby = isf.createItemStack(Items.MAGMACREAM, CC.tnValue + "Back to Lobby" + CC.tnUse + " (Right Click)");
				p.getInventory().setItem(0, spectate);
				p.getInventory().setItem(8, lobby);
				PotionEffect invis = new PotionEffect(PotionEffectType.INVISIBILITY, 12000, 1, false, false);
				PotionEffect nightvision = new PotionEffect(PotionEffectType.NIGHT_VISION, 12000, 3, false, false);
				p.addPotionEffect(invis, true);
				p.addPotionEffect(nightvision, true);
				UtilPlayer.message(Category.PLAYER, p, CC.tnInfo + "You are now invisible.");
				UtilPlayer.message(Category.PLAYER, p, CC.tnInfo + "You now have night vision.");
			}
		}.runTaskLater(main, 10L);
	}

	public void removePlayer(Player p, boolean logout) {
		p.setFlying(false);
		p.setAllowFlight(false);
		p.setLevel(0);
		p.setHealth(20.0);
		p.removePotionEffect(PotionEffectType.INVISIBILITY);
		p.removePotionEffect(PotionEffectType.WITHER);
		p.removePotionEffect(PotionEffectType.NIGHT_VISION);
		p.setGameMode(GameMode.ADVENTURE);
		this.game_Players.remove(p);
		this.game_Spectators.remove(p);
		this.earnings_Kills.remove(p);
		this.earnings_Assists.remove(p);
		if (!logout) {
			new SendtoLocation(p, GameManager.getLobbySpawn());
			ScoreboardManager.setupScoreboard(p, Gamemodes.BORDERLINE);
			for (Player players : Bukkit.getOnlinePlayers()) {
				p.showPlayer(players);
			}
			for (Player players : game_Players) {
				players.hidePlayer(p);
			}
			for (Player players : game_Spectators) {
				players.hidePlayer(p);
			}
		}
		if (!this.game_Spectators.contains(p)) {
			for (Player players : game_Players) {
				UtilPlayer.message(Category.GAME, players, CC.tnPlayer + p.getDisplayName() + CC.tnInfo + " has quit.");
			}
			for (Player players : game_Spectators) {
				UtilPlayer.message(Category.GAME, players, CC.tnPlayer + p.getDisplayName() + CC.tnInfo + " has quit.");
			}
		}

		if (this.state == GameState.WAITING) {
			if (this.prestart_Task != null) {
				if (game_Players.size() < this.game_Min_Players) {
					cancelPreStart();
					for (Player players : game_Players) {
						UtilPlayer.message(Category.GAME, players, CC.tnError + "Start has been cancelled, there isn't enough players!");
					}
				}
			}
		}
		if ((this.game_Players.size() < 20) && (this.state == GameState.FULL) && (this.state != GameState.STARTING)) {
			this.state = GameState.WAITING;
		}
		updateLobbyPlayers();
		updateGamePlayers();
		SignManager.updateSign(this.ID);
	}

	public void killPlayer(Player p) {
		UtilPlayer.message(Category.GAME, p, CC.tnInfo + CC.fBold + "You are out of the game, but you can still spectate.");
		setSpectator(p);
		updateGamePlayers();
	}

	private void setSpectator(final Player p) {
		for (Player players : game_Players) {
			players.hidePlayer(p);
		}
		this.game_Players.remove(p);
		this.game_Players_team.removeEntry(p.getName());
		this.game_Spectators_team.addEntry(p.getName());
		this.game_Spectators.add(p);
		sendEarnings(p);
		p.setGameMode(GameMode.SURVIVAL);
		p.setAllowFlight(true);
		p.setFlying(true);
		p.setFlySpeed(0.1F);
		UtilPlayer.clearInv(p);
		p.updateInventory();
		new BukkitRunnable() {

			@Override
			public void run() {
				ItemStackFactory isf = new ItemStackFactory();
				ItemStack spectate = isf.createItemStack(Items.COMPASS, CC.tnValue + "Spectate" + CC.tnUse + " (Right Click)");
				ItemStack lobby = isf.createItemStack(Items.MAGMACREAM, CC.tnValue + "Back to Lobby" + CC.tnUse + " (Right Click)");
				p.getInventory().setItem(0, spectate);
				p.getInventory().setItem(8, lobby);
				PotionEffect invis = new PotionEffect(PotionEffectType.INVISIBILITY, 12000, 1, false, false);
				PotionEffect nightvision = new PotionEffect(PotionEffectType.NIGHT_VISION, 12000, 3, false, false);
				p.addPotionEffect(invis, true);
				p.addPotionEffect(nightvision, true);
				UtilPlayer.message(Category.PLAYER, p, CC.tnInfo + "You are now invisible.");
				UtilPlayer.message(Category.PLAYER, p, CC.tnInfo + "You now have night vision.");

			}
		}.runTaskLater(main, 10L);
		this.earnings_Assists.remove(p);
		this.earnings_Kills.remove(p);
		updateGamePlayers();
		SignManager.updateSign(this.ID);
	}

	private void sendGameInfo(Player p) {
		UtilPlayer.messageFooter(p);
		p.sendMessage("                                  " + CC.tnAbility + CC.fBold + "Borderline");
		p.sendMessage("  " + CC.tnInfo + "Use your guns and other abilitys to slay your enemies");
		p.sendMessage("    " + CC.tnInfo + "in this action packed free-for-all battle! The last");
		p.sendMessage("     " + CC.tnInfo + "player standing or with most kills wins the game.");
		p.sendMessage("  ");
		p.sendMessage(CC.tnInfo + CC.fBold + "Map: " + CC.tnValue + this.map_Name + " " + CC.tnInfo + CC.fBold + "by " + CC.tnValue + this.map_Author);
		UtilPlayer.messageFooter(p);
	}

	private void sendEarnings(Player p) {
		int kills = 0;
		if (earnings_Kills.containsKey(p)) {
			kills = earnings_Kills.get(p);
		}
		int tokens_kills = kills * 5;
		int assists = 0;
		if (earnings_Assists.containsKey(p)) {
			assists = earnings_Assists.get(p);
		}
		int tokens_assists = assists * 1;
		int tokens_participation = 10;
		int tokens_win = 0;
		if (this.game_Winner == p) {
			tokens_win = 20;
		}
		int total_earnt = tokens_kills + tokens_participation + tokens_win + tokens_assists;
		UtilPlayer.messageHeader(Category.GAME, p, "Tokens for this game");
		UtilPlayer.messageNoCategory(p, " ");
		if (tokens_kills > 0) {
			UtilPlayer.messageNoCategory(p, CC.tnValue + "+" + tokens_kills + " Tevo Tokens" + CC.tnInfo + " for " + CC.tnValue + kills + CC.tnInfo + " Kills");
		}
		if (tokens_assists > 0) {
			UtilPlayer.messageNoCategory(p, CC.tnValue + "+" + tokens_assists + " Tevo Tokens" + CC.tnInfo + " for " + CC.tnValue + assists + CC.tnInfo + " Assists");
		}
		UtilPlayer.messageNoCategory(p, CC.tnValue + "+" + tokens_participation + " Tevo Tokens" + CC.tnInfo + " for Participation");
		if (tokens_win > 0) {
			UtilPlayer.messageNoCategory(p, CC.tnValue + "+" + tokens_win + " Tevo Tokens" + CC.tnInfo + " for Winning the game");
		}
		int extra = 0;
		if (UtilPlayer.multiplier(p) > 0) {
			extra = total_earnt * UtilPlayer.multiplier(p);
			UtilPlayer.messageNoCategory(p, CC.tnValue + "+" + extra + " Tevo Tokens" + CC.tnInfo + " for " + Rank.getRankPrefix(UtilPlayer.getRank(p)) + CC.tnInfo + " Rank (" + CC.tnValue + "x"
					+ UtilPlayer.multiplier(p) + CC.tnInfo + ")");
		}
		else {
			UtilPlayer.messageNoCategory(p, CC.tnInfo + "Purchase a rank at " + CC.tnValue + "store.tevonetwork.com " + CC.tnInfo + "to get a permanent token multiplier!");
		}
		UtilPlayer.messageNoCategory(p, " ");
		int final_total = extra + total_earnt;
		UtilPlayer.messageNoCategory(p, CC.tnInfo + CC.fBold + "Total Earnt: " + CC.tnValue + CC.fBold + final_total + " Tevo Tokens");
		UtilPlayer.messageNoCategory(p, CC.tnInfo + CC.fBold + "New Balance: " + CC.tnValue + CC.fBold + EconomyManager.getTokensBal(p) + " Tevo Tokens");
		UtilPlayer.messageFooter(p);
		UtilPlayer.sound(p, playerSounds.TRANSACTIONSUCCESS);
	}

	public void handleJoin(Player p) {
		for (Player players : game_Players) {
			players.hidePlayer(p);
		}
		for (Player players : game_Spectators) {
			players.hidePlayer(p);
		}
	}

	public void addKill(Player p) {
		if (earnings_Kills.containsKey(p)) {
			int newkills = earnings_Kills.get(p) + 1;
			earnings_Kills.put(p, newkills);
			p.setLevel(newkills);
			UtilPlayer.sound(p, playerSounds.KILL);
			EconomyManager.addTokens(p, this.reward_Kill * UtilPlayer.multiplier(p));
			if (UtilPlayer.multiplier(p) > 0) {
				UtilPlayer.transaction(p, this.reward_Kill * UtilPlayer.multiplier(p), "kill", new String[] { Rank.getRankPrefix(UtilPlayer.getRank(p)) + CC.tnValue + " Rank" });
			}
			else {
				UtilPlayer.transaction(p, this.reward_Kill, "kill", null);
			}
			StatsManager.addKill(p);
		}
	}

	public void addAssist(Player p) {
		if (earnings_Assists.containsKey(p)) {
			int newkills = earnings_Assists.get(p) + 1;
			earnings_Assists.put(p, newkills);
			UtilPlayer.sound(p, playerSounds.KILL);
			EconomyManager.addTokens(p, this.reward_Assist * UtilPlayer.multiplier(p));
			if (UtilPlayer.multiplier(p) > 0) {
				UtilPlayer.transaction(p, this.reward_Assist * UtilPlayer.multiplier(p), "assisted kill", new String[] { Rank.getRankPrefix(UtilPlayer.getRank(p)) + CC.tnValue + " Rank" });
			}
			else {
				UtilPlayer.transaction(p, this.reward_Assist, "assisted kill", null);
			}
		}
	}

	// Game Settings
	public void removeLastSpawnpoint() {
		if (this.map_Spawnpoints.size() <= 0) {
			this.state = GameState.ERROR;
			return;
		}
		this.map_Spawnpoints.remove(this.map_Spawnpoints.size() - 1);
	}

	public void addSpawnpoint(Location loc) {
		if (this.map_Spawnpoints.size() >= 20) {
			return;
		}
		this.map_Spawnpoints.add(loc);
		if ((this.map_Spawnpoints.size() == 20) && (this.map_Lobby != null) && (this.map_Center != null)) {
			this.state = GameState.WAITING;
		}
	}

	// Checks
	public boolean containsPlayer(Player p) {
		return game_Players.contains(p);
	}

	public boolean containsSpectator(Player p) {
		return game_Spectators.contains(p);
	}

	// SETTERS
	public void setSpawnpoints(List<Location> spawnpoints) {
		this.map_Spawnpoints = spawnpoints;
	}

	public void setState(GameState state) {
		this.state = state;
	}

	public void setMapCenter(Location location) {
		this.map_Center = location;
		if ((this.map_Spawnpoints.size() == 20) && (this.map_Lobby != null) && (this.map_Center != null)) {
			this.state = GameState.WAITING;
		}
	}

	public void setMap_Author(String name) {
		this.map_Author = name;
	}

	public void setMap_Name(String name) {
		this.map_Name = name;
	}

	public void setMap_Lobby(Location loc) {
		this.map_Lobby = loc;
		if ((this.map_Spawnpoints.size() == 20) && (this.map_Lobby != null) && (this.map_Center != null)) {
			this.state = GameState.WAITING;
		}
	}

	// GETTERS
	public List<Location> getSpawnpoints() {
		return this.map_Spawnpoints;
	}

	public Location getMapCenter() {
		return this.map_Center;
	}

	public GameState getGameState() {
		return this.state;
	}

	public String getMap_Author() {
		return this.map_Author;
	}

	public String getMap_Name() {
		return this.map_Name;
	}

	public BukkitTask getPreStartTask() {
		return this.prestart_Task;
	}

	public BukkitTask getScoreboardAnimationTask() {
		return this.scoreboard_Animation;
	}

	public WorldBorder getWorldBorder() {
		return this.arena_border;
	}

	public Scoreboard getGameBoard() {
		return this.game_board;
	}

	public long getRemainingTime() {
		return this.seconds_Remaining;
	}

	public int getPlayers() {
		return this.game_Players.size();
	}

	public int getSpectators() {
		return this.game_Spectators.size();
	}

	public ArrayList<Player> getSpectatorList() {
		return this.game_Spectators;
	}

	public ArrayList<Player> getPlayerList() {
		return this.game_Players;
	}

	public int getMaxPlayers() {
		return this.game_Max_Players;
	}

	public int getMinPlayers() {
		return this.game_Min_Players;
	}

	public int getID() {
		return this.ID;
	}

	private List<String> scoreboard_frames = Arrays.asList("§a§lBorderline", "§a§lBorderline", "§a§lBorderline", "§a§lBorderline", "§a§lBorderline", "§a§lBorderline", "§a§lBorderline",
			"§a§lBorderline", "§a§lBorderline", "§a§lBorderline", "§a§lBorderline", "§a§lBorderline", "§a§lBorderline", "§a§lBorderline", "§a§lBorderline", "§a§lBorderline", "§a§lBorderline",
			"§a§lBorderline", "§a§lBorderline", "§a§lBorderline", "§a§lBorderline", "§a§lBorderline", "§a§lBorderline", "§a§lBorderline", "§a§lBorderline", "§a§lBorderline", "§a§lBorderline",
			"§a§lBorderline", "§a§lBorderline", "§a§lBorderline", "§a§lBorderline", "§a§lBorderline", "§a§lBorderline", "§a§lBorderline", "§a§lBorderline", "§a§lBorderline", "§a§lBorderline",
			"§a§lBorderline", "§2§lB§a§lorderline", "§f§lB§2§lo§a§lrderline", "§f§lBo§2§lr§a§lderline", "§f§lBor§2§ld§a§lerline", "§f§lBord§2§le§a§lrline", "§f§lBorde§2§lr§a§lline",
			"§f§lBorder§2§ll§a§line", "§f§lBorderl§2§li§a§lne", "§f§lBorderli§2§ln§a§le", "§f§lBorderlin§2§le", "§f§lBorderline", "§f§lBorderline", "§f§lBorderline", "§f§lBorderline",
			"§f§lBorderline", "§f§lBorderline", "§f§lBorderline", "§a§lB§f§lorderline", "§a§lBo§f§lrderline", "§a§lBor§f§lderline", "§a§lBord§f§lerline", "§a§lBorde§f§lrline", "§a§lBorder§f§lline",
			"§a§lBorderl§f§line", "§a§lBorderli§f§lne", "§a§lBorderlin§f§le", "§2§lBorderline", "§2§lBorderline", "§2§lBorderline", "§2§lBorderline", "§a§lBorderline", "§a§lBorderline",
			"§a§lBorderline", "§a§lBorderline", "§a§lBorderline");

}
