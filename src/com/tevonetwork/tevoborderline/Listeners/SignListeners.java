package com.tevonetwork.tevoborderline.Listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import com.tevonetwork.tevoapi.API.Util.CC;
import com.tevonetwork.tevoapi.API.Util.UtilPlayer;
import com.tevonetwork.tevoapi.Core.Category;
import com.tevonetwork.tevoapi.Core.Rank;
import com.tevonetwork.tevoborderline.Game.GameManager;
import com.tevonetwork.tevoborderline.Signs.SignManager;

public class SignListeners implements Listener{

	
	@EventHandler
	public void signInteract(PlayerInteractEvent e)
	{
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
		{
			return;
		}
		if ((e.getClickedBlock().getType() != Material.WALL_SIGN) && (e.getClickedBlock().getType() != Material.SIGN_POST))
		{
			return;
		}
		if (e.getClickedBlock().hasMetadata("GameID"))
		{
			GameManager.joinPlayer(e.getPlayer(), e.getClickedBlock().getMetadata("GameID").get(0).asInt());
		}
	}
	
	
	@EventHandler
	public void onSignChange(SignChangeEvent e)
	{
		Player p = e.getPlayer();
		if (e.getLines().length > 0)
		{
			if (e.getLine(0).equalsIgnoreCase("[Borderline]"))
			{
				if (UtilPlayer.hasRank(p, Rank.DEVELOPER))
				{
					if (e.getLines().length > 1)
					{
						if (GameManager.doesGameExist(Integer.valueOf(e.getLine(1))))
						{
							if (SignManager.newSign(Integer.valueOf(e.getLine(1)), e.getBlock().getLocation()))
							{
								UtilPlayer.message(Category.GAME, p, CC.tnInfo + "Successfully created game sign for game " + CC.tnValue + e.getLine(1) + CC.end);
							}
							else
							{
								e.getBlock().setType(Material.AIR);
								UtilPlayer.message(Category.GAME, p, CC.tnError + "Failed to create game sign, check console for errors!");
							}
						}
						else
						{
							e.getBlock().setType(Material.AIR);
							UtilPlayer.message(Category.GAME, p, CC.tnError + "Invalid Game! Sign removed.");
						}
					}
					else
					{
						e.getBlock().setType(Material.AIR);
						UtilPlayer.message(Category.GAME, p, CC.tnError + "Specifiy a Game ID! Sign removed.");
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent e)
	{
		if ((e.getBlock().getType() != Material.WALL_SIGN) && (e.getBlock().getType() != Material.SIGN_POST))
		{
			return;
		}
		if (e.getBlock().hasMetadata("GameID"))
		{
			if (UtilPlayer.hasRank(e.getPlayer(), Rank.DEVELOPER))
			{
				if (SignManager.removeSign(e.getBlock().getMetadata("GameID").get(0).asInt()))
				{
					UtilPlayer.message(Category.GAME, e.getPlayer(), CC.tnInfo + "Removed game sign for " + CC.tnValue + e.getBlock().getMetadata("GameID").get(0).asInt());
				}
				else
				{
					UtilPlayer.message(Category.GAME, e.getPlayer(), CC.tnError + "Error removign sign, check console!");
				}
			}
		}
	}
	
}
