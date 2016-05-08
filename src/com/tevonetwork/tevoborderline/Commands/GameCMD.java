package com.tevonetwork.tevoborderline.Commands;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.tevonetwork.tevoapi.API.Permissions.PermissionsHandler;
import com.tevonetwork.tevoapi.API.Util.CC;
import com.tevonetwork.tevoapi.API.Util.UtilPlayer;
import com.tevonetwork.tevoapi.Core.Category;
import com.tevonetwork.tevoapi.Core.Rank;
import com.tevonetwork.tevoapi.Core.Messages.CategoryMSG;
import com.tevonetwork.tevoapi.Core.Messages.PermMSG;
import com.tevonetwork.tevoborderline.Game.Game;
import com.tevonetwork.tevoborderline.Game.GameManager;
import com.tevonetwork.tevoborderline.Game.GameState;
import com.tevonetwork.tevoborderline.Signs.SignManager;

public class GameCMD implements CommandExecutor{

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if (sender instanceof Player)
		{
			Player p = (Player)sender;
			if (UtilPlayer.hasRank(p, Rank.ADMIN))
			{
				if (args.length > 0)
				{
					if (args[0].equalsIgnoreCase("create"))
					{
						if (UtilPlayer.hasRank(p, Rank.DEVELOPER))
						{
							if (args.length > 2)
							{
								String mapname = args[1].replace("_", " ");
								String mapauthor = args[2].replace("_", " ");
								Location mapcenter = p.getLocation();
								if (GameManager.newGame(mapname, mapauthor, mapcenter))
								{
									UtilPlayer.message(Category.GAME, p, CC.tnInfo + "Successfully created a new game!");
								}
								else
								{
									UtilPlayer.message(Category.GAME, p, CC.tnError + "Failed to create game, check the console for errors!");
								}
							}
							else
							{
								CategoryMSG.senderArgsErr(sender, Category.GAME, "/game create <map_Name> <map_Author>");
							}
						}
						else
						{
							PermMSG.noPerm(sender, Rank.DEVELOPER);
						}
					}
					else if (args[0].equalsIgnoreCase("addspawnpoint"))
					{
						if (UtilPlayer.hasRank(p, Rank.DEVELOPER))
						{
							if (args.length > 1)
							{
								if (GameManager.addSpawnPoint(Integer.valueOf(args[1]), p.getLocation()))
								{
									UtilPlayer.message(Category.GAME, p, CC.tnInfo + "Successfully added spawnpoint to game " + CC.tnValue + args[1] + CC.end);
								}
								else
								{
									UtilPlayer.message(Category.GAME, p, CC.tnError + "Invalid Game!");
								}
							}
							else 
							{
								CategoryMSG.senderArgsErr(sender, Category.GAME, "/game addspawnpoint <gameID>");
							}
						}
						else
						{
							PermMSG.noPerm(sender, Rank.DEVELOPER);
						}
					}
					else if (args[0].equalsIgnoreCase("removespawnpoint"))
					{
						if (UtilPlayer.hasRank(p, Rank.DEVELOPER))
						{
							if (args.length > 1)
							{
								if (GameManager.removeLastSpawnPoint(Integer.valueOf(args[1])))
								{
									UtilPlayer.message(Category.GAME, p, CC.tnInfo + "Successfully removed the last spawnpoint from game " + CC.tnValue + args[1] + CC.end);
								}
								else
								{
									UtilPlayer.message(Category.GAME, p, CC.tnError + "Invalid Game!");
								}
							}
							else
							{
								CategoryMSG.senderArgsErr(sender, Category.GAME, "/game removespawnpoint <gameID>");
							}
						}
						else
						{
							PermMSG.noPerm(sender, Rank.DEVELOPER);
						}
					}
					else if (args[0].equalsIgnoreCase("setlobby"))
					{
						if (UtilPlayer.hasRank(p, Rank.DEVELOPER))
						{
							if (args.length > 1)
							{
								if (GameManager.setGameLobby(Integer.valueOf(args[1]), p.getLocation()))
								{
									UtilPlayer.message(Category.GAME, p, CC.tnInfo + "Successfully set the waiting lobby for game " + CC.tnValue + args[1] + CC.end);
								}
								else
								{
									UtilPlayer.message(Category.GAME, p, CC.tnError + "Invalid Game!");
								}
							}
							else
							{
								CategoryMSG.senderArgsErr(sender, Category.GAME, "/game setlobby <gameID>");
							}
						}
						else
						{
							PermMSG.noPerm(sender, Rank.DEVELOPER);
						}
					}
					else if (args[0].equalsIgnoreCase("delete"))
					{
						if (UtilPlayer.hasRank(p, Rank.DEVELOPER))
						{
							if (args.length > 1)
							{
								if (GameManager.deleteGame(Integer.valueOf(args[1])))
								{
									UtilPlayer.message(Category.GAME, p, CC.tnInfo + "Successfully deleted game " + CC.tnValue + args[1] + CC.end);
								}
								else
								{
									UtilPlayer.message(Category.GAME, p, CC.tnError + "Invalid Game!");
								}
							}
							else
							{
								CategoryMSG.senderArgsErr(sender, Category.GAME, "/game delete <gameID>");
							}
						}
						else
						{
							PermMSG.noPerm(sender, Rank.DEVELOPER);
						}
					}
					else if (args[0].equalsIgnoreCase("info"))
					{
						if (UtilPlayer.hasRank(p, Rank.DEVELOPER))
						{
							if (args.length > 1)
							{
								if (GameManager.doesGameExist(Integer.valueOf(args[1])))
								{
									Game game = GameManager.getGame(Integer.valueOf(args[1]));
									UtilPlayer.messageHeader(Category.GAME, p, "Game Info");
									UtilPlayer.messageNoCategory(p, CC.tnInfo + "Game ID: " + CC.tnValue + game.getID());
									UtilPlayer.messageNoCategory(p, CC.tnInfo + "Map Name: " + CC.tnValue + game.getMap_Name());
									UtilPlayer.messageNoCategory(p, CC.tnInfo + "Map Author: " + CC.tnValue + game.getMap_Author());
									UtilPlayer.messageNoCategory(p, CC.tnInfo + "Game Status: " + GameState.getStateString(game.getGameState()));
									if (SignManager.hasSign(game.getID()))
									{
										UtilPlayer.messageNoCategory(p, CC.tnInfo + "Sign: " + CC.tnEnable + "True");
									}
									else
									{
										UtilPlayer.messageNoCategory(p, CC.tnInfo + "Sign: " + CC.tnEnable + "False");
									}
									UtilPlayer.messageNoCategory(p, CC.tnInfo + "Playing: " + CC.tnValue + game.getPlayers());
									UtilPlayer.messageNoCategory(p, CC.tnInfo + "Spectators: " + CC.tnValue + game.getSpectators());
									UtilPlayer.messageFooter(p);
								}
								else
								{
									UtilPlayer.message(Category.GAME, p, CC.tnError + "Invalid Game!");
								}
							}
							else
							{
								if (GameManager.getPlayerGame(p) != null)
								{
									Game game = GameManager.getPlayerGame(p);
									UtilPlayer.messageHeader(Category.GAME, p, "Game Info");
									UtilPlayer.messageNoCategory(p, CC.tnInfo + "Game ID: " + CC.tnValue + game.getID());
									UtilPlayer.messageNoCategory(p, CC.tnInfo + "Map Name: " + CC.tnValue + game.getMap_Name());
									UtilPlayer.messageNoCategory(p, CC.tnInfo + "Map Author: " + CC.tnValue + game.getMap_Author());
									UtilPlayer.messageNoCategory(p, CC.tnInfo + "Game Status: " + GameState.getStateString(game.getGameState()));
									if (SignManager.hasSign(game.getID()))
									{
										UtilPlayer.messageNoCategory(p, CC.tnInfo + "Sign: " + CC.tnEnable + "True");
									}
									else
									{
										UtilPlayer.messageNoCategory(p, CC.tnInfo + "Sign: " + CC.tnEnable + "False");
									}
									UtilPlayer.messageNoCategory(p, CC.tnInfo + "Playing: " + CC.tnValue + game.getPlayers());
									UtilPlayer.messageNoCategory(p, CC.tnInfo + "Spectators: " + CC.tnValue + game.getSpectators());
									UtilPlayer.messageFooter(p);
								}
								else
								{
									CategoryMSG.senderArgsErr(sender, Category.GAME, "/game info <gameID>");
								}
							}
						}
						else
						{
							PermMSG.noPerm(sender, Rank.DEVELOPER);
						}
					}
					else if (args[0].equalsIgnoreCase("list"))
					{
						if (UtilPlayer.hasRank(p, Rank.DEVELOPER))
						{
							UtilPlayer.messageHeader(Category.GAME, p, "Game List");
							UtilPlayer.messageNoCategory(p, CC.tnInfo + "#ID [MapName] [State] [World] [Sign]");
							UtilPlayer.messageNoCategory(p, " ");
							for (Game games : GameManager.getLoadedGames())
							{
								if (SignManager.hasSign(games.getID()))
								{
									UtilPlayer.messageNoCategory(p, CC.tnValue + "#" + games.getID() + CC.tnInfo + " [" + CC.tnValue + games.getMap_Name() + CC.tnInfo + "] [" + GameState.getStateString(games.getGameState()) + CC.tnInfo + "] [" + CC.tnValue + games.getMapCenter().getWorld().getName() + CC.tnInfo + "] [" + CC.tnEnable + "True" + CC.tnInfo + "]");
								}
								else
								{
									UtilPlayer.messageNoCategory(p, CC.tnValue + "#" + games.getID() + CC.tnInfo + " [" + CC.tnValue + games.getMap_Name() + CC.tnInfo + "] [" + GameState.getStateString(games.getGameState()) + CC.tnInfo + "] [" + CC.tnValue + games.getMapCenter().getWorld().getName() + CC.tnInfo + "] [" + CC.tnDisable + "False" + CC.tnInfo + "]");
								}
								UtilPlayer.messageNoCategory(p, " ");
							}
							UtilPlayer.messageFooter(p);
						}
						else
						{
							PermMSG.noPerm(sender, Rank.DEVELOPER);
						}
					}
					else if (args[0].equalsIgnoreCase("join"))
					{
						if (UtilPlayer.hasRank(p, Rank.DEVELOPER))
						{
							if (args.length > 1)
							{
								if (GameManager.doesGameExist(Integer.valueOf(args[1])))
								{
									GameManager.joinPlayer(p, Integer.valueOf(args[1]));
								}
								else
								{
									UtilPlayer.message(Category.GAME, p, CC.tnError + "Invalid Game!");
								}
							}
							else
							{
								CategoryMSG.senderArgsErr(sender, Category.GAME, "/game join <gameID>");
							}
						}
						else
						{
							PermMSG.noPerm(sender, Rank.DEVELOPER);
						}
					}
					else if (args[0].equalsIgnoreCase("start"))
					{
						if (GameManager.getPlayerGame(p) != null)
						{
							Game game = GameManager.getPlayerGame(p);
							if (game.getGameState() == GameState.WAITING)
							{
								game.forceStart(p.getDisplayName());
							}
							else
							{
								UtilPlayer.message(Category.GAME, p, CC.tnError + "Cannot start game right now!");
							}
						}
						else
						{
							if (UtilPlayer.hasRank(p, Rank.DEVELOPER))
							{
								if (args.length > 1)
								{
									if (GameManager.doesGameExist(Integer.valueOf(args[1])))
									{
										GameManager.getGame(Integer.valueOf(args[1])).forceStart(p.getDisplayName());
									}
									else
									{
										UtilPlayer.message(Category.GAME, p, CC.tnError + "Invalid Game!");
									}
								}
								else
								{
									CategoryMSG.senderArgsErr(sender, Category.GAME, "/game start <gameID>");
								}
							}
							else
							{
								PermMSG.noPerm(sender, Rank.DEVELOPER);
							}
						}
					}
					else if (args[0].equalsIgnoreCase("setgamelobby"))
					{
						if (UtilPlayer.hasRank(p, Rank.DEVELOPER))
						{
							GameManager.setLobbySpawn(p.getLocation());
							UtilPlayer.message(Category.GAME, p, CC.tnInfo + "Successfully set the main game lobby!");
						}
						else
						{
							PermMSG.noPerm(sender, Rank.DEVELOPER);
						}
					}
					else 
					{
						showHelp(sender);
					}
				}
				else
				{
					showHelp(sender);
				}
			}
			else
			{
				PermMSG.noPerm(sender, Rank.ADMIN);
			}
		}
		else
		{
			if (PermissionsHandler.hasRankSender(sender, Rank.DEVELOPER))
			{
				if (args.length > 0)
				{
					if (args[0].equalsIgnoreCase("delete"))
					{
						if (args.length > 1)
						{
							if (GameManager.deleteGame(Integer.valueOf(args[1])))
							{
								CategoryMSG.senderMessage(sender, Category.GAME, CC.tnInfo + "Successfully deleted game " + CC.tnValue + args[1] + CC.end);
							}
							else
							{
								CategoryMSG.senderMessage(sender, Category.GAME, CC.tnError + "Invalid Game!");
							}
						}
						else
						{
							CategoryMSG.senderArgsErr(sender, Category.GAME, "/game delete <gameID>");
						}
					}
					else if (args[0].equalsIgnoreCase("info"))
					{
						if (args.length > 1)
						{
							if (GameManager.doesGameExist(Integer.valueOf(args[1])))
							{
								Game game = GameManager.getGame(Integer.valueOf(args[1]));
								CategoryMSG.senderHeader(sender, Category.GAME, "Game Info");
								CategoryMSG.senderMessageNoCategory(sender, CC.tnInfo + "Game ID: " + CC.tnValue + game.getID());
								CategoryMSG.senderMessageNoCategory(sender, CC.tnInfo + "Map Name: " + CC.tnValue + game.getMap_Name());
								CategoryMSG.senderMessageNoCategory(sender, CC.tnInfo + "Map Author: " + CC.tnValue + game.getMap_Author());
								CategoryMSG.senderMessageNoCategory(sender, CC.tnInfo + "Game Status: " + GameState.getStateString(game.getGameState()));
								if (SignManager.hasSign(game.getID()))
								{
									CategoryMSG.senderMessageNoCategory(sender, CC.tnInfo + "Sign: " + CC.tnEnable + "True");
								}
								else
								{
									CategoryMSG.senderMessageNoCategory(sender, CC.tnInfo + "Sign: " + CC.tnEnable + "False");
								}
								CategoryMSG.senderMessageNoCategory(sender, CC.tnInfo + "Playing: " + CC.tnValue + game.getPlayers());
								CategoryMSG.senderMessageNoCategory(sender, CC.tnInfo + "Spectators: " + CC.tnValue + game.getSpectators());
								CategoryMSG.senderFooter(sender);
							}
							else
							{
								CategoryMSG.senderMessage(sender, Category.GAME, CC.tnError + "Invalid Game!");
							}
						}
						else
						{
							CategoryMSG.senderArgsErr(sender, Category.GAME, "/game info <gameID>");
						}
					}
					else if (args[0].equalsIgnoreCase("list"))
					{
						CategoryMSG.senderHeader(sender, Category.GAME, "Game List");
						CategoryMSG.senderMessageNoCategory(sender, CC.tnInfo + "#ID [MapName] [State] [World] [Sign]");
						CategoryMSG.senderMessageNoCategory(sender, " ");
						for (Game games : GameManager.getLoadedGames())
						{
							if (SignManager.hasSign(games.getID()))
							{
								CategoryMSG.senderMessageNoCategory(sender, CC.tnValue + "#" + games.getID() + CC.tnInfo + " [" + CC.tnValue + games.getMap_Name() + CC.tnInfo + "] [" + GameState.getStateString(games.getGameState()) + CC.tnInfo + "] [" + CC.tnValue + games.getMapCenter().getWorld().getName() + CC.tnInfo + "] [" + CC.tnEnable + "True" + CC.tnInfo + "]");
							}
							else
							{
								CategoryMSG.senderMessageNoCategory(sender, CC.tnValue + "#" + games.getID() + CC.tnInfo + " [" + CC.tnValue + games.getMap_Name() + CC.tnInfo + "] [" + GameState.getStateString(games.getGameState()) + CC.tnInfo + "] [" + CC.tnValue + games.getMapCenter().getWorld().getName() + CC.tnInfo + "] [" + CC.tnDisable + "False" + CC.tnInfo + "]");
							}
							CategoryMSG.senderMessageNoCategory(sender, " ");
						}
						CategoryMSG.senderFooter(sender);
					}
					else if (args[0].equalsIgnoreCase("start"))
					{
						if (args.length > 1)
						{
							if (GameManager.doesGameExist(Integer.valueOf(args[1])))
							{
								GameManager.forceStartGame(Integer.valueOf(args[1]));
							}
							else
							{
								CategoryMSG.senderMessage(sender, Category.GAME, CC.tnError + "Invalid Game!");
							}
						}
						else
						{
							CategoryMSG.senderArgsErr(sender, Category.GAME, "/game start <gameID>");
						}
					}
					else
					{
						showHelp(sender);
					}
				}
				else
				{
					showHelp(sender);
				}
			}
			else
			{
				PermMSG.noPerm(sender, Rank.DEVELOPER);
			}
		}
		return false;
	}
	
	private void showHelp(CommandSender sender)
	{
		CategoryMSG.senderHeader(sender, Category.GAME, "Game Command Usage");
		if (sender instanceof Player)
		{
			CategoryMSG.senderMessageNoCategory(sender, CC.tnUse + "/game create <map_name> <map_author> " + CC.tnInfo + "Creates a new game using your location as the center point.");
			CategoryMSG.senderMessageNoCategory(sender, CC.tnUse + "/game addspawnpoint <gameID> " + CC.tnInfo + "Adds a spawnpoint to a game.");
			CategoryMSG.senderMessageNoCategory(sender, CC.tnUse + "/game removespawnpoint <gameID> " + CC.tnInfo + "Removes the last set spawnpoint for the specified game.");
			CategoryMSG.senderMessageNoCategory(sender, CC.tnUse + "/game setlobby <gameID> " + CC.tnInfo + "Sets the waiting lobby for the specified game.");
			CategoryMSG.senderMessageNoCategory(sender, CC.tnUse + "/game setgamelobby" + CC.tnInfo + "Sets the main game lobby.");
			CategoryMSG.senderMessageNoCategory(sender, CC.tnUse + "/game join <gameID> " + CC.tnInfo + "Join the specified game.");
		}
		CategoryMSG.senderMessageNoCategory(sender, CC.tnUse + "/game delete <gameID> " + CC.tnInfo + "Removes the game.");
		CategoryMSG.senderMessageNoCategory(sender, CC.tnUse + "/game info [gameID] " + CC.tnInfo + "Shows the information of specified or current game.");
		CategoryMSG.senderMessageNoCategory(sender, CC.tnUse + "/game list " + CC.tnInfo + "Shows the game list.");
		CategoryMSG.senderMessageNoCategory(sender, CC.tnUse + "/game start [gameID] " + CC.tnInfo + "Force start the game.");
		CategoryMSG.senderFooter(sender);
		
	}

}
