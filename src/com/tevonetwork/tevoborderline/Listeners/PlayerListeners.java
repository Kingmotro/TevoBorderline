package com.tevonetwork.tevoborderline.Listeners;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;

import com.tevonetwork.tevoapi.API.Events.KillEvent;
import com.tevonetwork.tevoapi.API.Math.MathUtils;
import com.tevonetwork.tevoapi.API.Util.CC;
import com.tevonetwork.tevoapi.API.Util.ItemStackFactory;
import com.tevonetwork.tevoapi.API.Util.UtilPlayer;
import com.tevonetwork.tevoapi.Core.Category;
import com.tevonetwork.tevoapi.Core.Travel.SendtoLocation;
import com.tevonetwork.tevoborderline.Game.GameManager;
import com.tevonetwork.tevoborderline.Game.Kits.KitManager;

public class PlayerListeners implements Listener {
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e)
	{
		Player p = e.getPlayer();
		GameManager.handleJoin(p);
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e)
	{
		Player p = e.getPlayer();
		GameManager.removePlayer(p, true);
	}
	
	@EventHandler
	public void onDropItem(PlayerDropItemEvent e)
	{
		if ((GameManager.isPlaying(e.getPlayer())) || (GameManager.isSpectating(e.getPlayer())))
		{
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPickupItem(PlayerPickupItemEvent e)
	{
		if ((GameManager.isPlaying(e.getPlayer())) || (GameManager.isSpectating(e.getPlayer())))
		{
			if ((e.getItem().getItemStack().getType() == Material.IRON_AXE) && (GameManager.isPlaying(e.getPlayer())))
			{
				if (e.getItem().hasMetadata("AbilityAttribute"))
				{
					if (!e.getPlayer().getName().equalsIgnoreCase(e.getItem().getMetadata("AbilityOwner").get(0).asString()))
					{
						e.setCancelled(true);
					}
				}
				e.getPlayer().updateInventory();
			}
			else
			{
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onKill(KillEvent e)
	{
		Player victim = e.getVictim();
		Player killer = e.getKiller();
		if (GameManager.isPlaying(victim))
		{
			for (int x = 0; x < 2; x++)
			{
				Firework firework = (Firework)victim.getWorld().spawnEntity(victim.getLocation(), EntityType.FIREWORK);
				FireworkMeta meta = firework.getFireworkMeta();
				FireworkEffect effect = FireworkEffect.builder().with(Type.BALL).withColor(Color.RED).flicker(true).build();
				meta.addEffect(effect);
				meta.setPower(2);
				firework.setFireworkMeta(meta);
			}
			GameManager.killPlayer(victim);
			if (killer != null)
			{
				GameManager.addKill(killer);
			}
			for (Player assists : e.getAssists())
			{
				GameManager.addAssist(assists);
			}
		}
	}
	
	@EventHandler
	public void onDeath(PlayerDeathEvent e)
	{
		if ((GameManager.isPlaying(e.getEntity())) || (GameManager.isSpectating(e.getEntity())))
		{
			e.getDrops().clear();
			e.setDroppedExp(0);
		}
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent e)
	{
		Player p = e.getPlayer();
		if ((GameManager.isPlaying(p)) || (GameManager.isSpectating(p)))
		{
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onBlockBreak(BlockPlaceEvent e)
	{
		Player p = e.getPlayer();
		if ((GameManager.isPlaying(p)) || (GameManager.isSpectating(p)))
		{
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent e)
	{
		if (GameManager.isPlaying(e.getPlayer()))
		{
			KitManager.handleEvent(e);
			if (e.getAction() == Action.RIGHT_CLICK_BLOCK)
			{
				if (e.getClickedBlock() != null)
				{
					if (e.getClickedBlock().getType() == Material.TRAP_DOOR)
					{
						e.setCancelled(true);
					}
				}
			}
		}
		if (GameManager.isSpectating(e.getPlayer()))
		{
			Player p = e.getPlayer();
			if ((e.getAction() != Action.RIGHT_CLICK_AIR) && (e.getAction() != Action.RIGHT_CLICK_BLOCK))
			{
				return;
			}
			if (p.getItemInHand().hasItemMeta())
			{
				if (p.getItemInHand().getItemMeta().getDisplayName().startsWith(CC.tnValue + "Spectate"))
				{
					Inventory menu = Bukkit.createInventory(null, 27, "Spectate");
					ArrayList<Player> players = GameManager.getPlayerGame(p).getPlayerList();
					ItemStackFactory isf = new ItemStackFactory();
					int index = 0;
					for (Player gameplayer : players)
					{
						menu.setItem(index, isf.createItemStackPlayerHeadwithLore(CC.tnPlayer + gameplayer.getName(), gameplayer.getName(), new String[] {CC.tnInfo + "Health: " + CC.tnValue + MathUtils.trim(2, gameplayer.getHealth()), CC.tnInfo + "Kills: " + CC.tnValue + gameplayer.getLevel(), CC.tnUse + "(Click to teleport)"}));
						index++;
					}
					p.openInventory(menu);
				}
				if (p.getItemInHand().getItemMeta().getDisplayName().startsWith(CC.tnValue + "Back"))
				{
					GameManager.removePlayer(p, false);
				}
			}
		}
	}
	
	@EventHandler
	public void onInvClick(InventoryClickEvent e)
	{
		if (e.getInventory().getTitle().equalsIgnoreCase("Spectate"))
		{
			if (!(e.getWhoClicked() instanceof Player))
			{
				return;
			}
			Player p = (Player)e.getWhoClicked();
			ItemStack clicked = e.getCurrentItem();
			e.setCancelled(true);
			if ((clicked == null) || (!clicked.hasItemMeta()))
			{
				return;
			}
			String playername = clicked.getItemMeta().getDisplayName().substring(2, clicked.getItemMeta().getDisplayName().length());
			Player spectatee = Bukkit.getPlayerExact(playername);
			if ((spectatee != null) && (GameManager.isPlaying(spectatee)))
			{
				new SendtoLocation(p, spectatee.getLocation());
				UtilPlayer.message(Category.TRAVEL, p, CC.tnInfo + "Teleported " + CC.tnPlayer + "You" + CC.tnInfo + " to " + CC.tnPlayer + spectatee.getName());
				p.closeInventory();
			}
			else
			{
				UtilPlayer.message(Category.GAME, p, CC.tnError + "Invalid Player!");
				p.closeInventory();
			}
		}
	}
	
}
