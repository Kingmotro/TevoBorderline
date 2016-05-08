package com.tevonetwork.tevoborderline.Listeners;

import org.bukkit.Effect;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.tevonetwork.tevoborderline.Game.GameManager;
import com.tevonetwork.tevoborderline.Game.GameState;
import com.tevonetwork.tevoborderline.Game.Kits.KitManager;

public class EntityListeners implements Listener{

	@EventHandler
	public void onProjHit(ProjectileHitEvent e)
	{
		KitManager.handleEvent(e);
	}
	
	@EventHandler
	public void onExplode(EntityExplodeEvent e)
	{
		if (e.getEntity() instanceof WitherSkull)
		{
			if (e.getEntity().hasMetadata("Ability"))
			{
				if (e.getEntity().getMetadata("Ability").get(0).asString().equalsIgnoreCase("Wither Launcher"))
				{
					e.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler
	public void onDamage(EntityDamageEvent e)
	{
		if (e.getCause() == DamageCause.SUICIDE)
		{
			return;
		}
		if (e.getEntity() instanceof Player)
		{
			Player p = (Player)e.getEntity();
			if (GameManager.isSpectating(p))
			{
				e.setCancelled(true);
			}
			if (GameManager.isPlaying(p))
			{
				if (GameManager.getPlayerGame(p).getGameState() != GameState.INGAME)
				{
					e.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler
	public void onDamagebyEntity(EntityDamageByEntityEvent e)
	{
		if (!(e.getEntity() instanceof Player))
		{
			return;
		}
		if (e.getDamager() instanceof Snowball)
		{
			Snowball knife = (Snowball)e.getDamager();
			if (knife.hasMetadata("Ability"))
			{
				if (knife.getMetadata("Ability").get(0).asString().equalsIgnoreCase("Throwing Axe"))
				{
					e.setDamage(4.0);
					e.getEntity().getWorld().spigot().playEffect(e.getEntity().getLocation(), Effect.STEP_SOUND, 152, 0, 0.5F, 1.2F, 0.5F, 0, 4, 30);
					return;
				}
			}
		}
		if (!(e.getDamager() instanceof Player))
		{
			return;
		}
		Player damager = (Player)e.getDamager();
		Player p = (Player)e.getEntity();
		if ((GameManager.isSpectating(damager)) || (GameManager.isSpectating(p)))
		{
			e.setCancelled(true);
		}
		if ((GameManager.isPlaying(damager)) || (GameManager.isPlaying(p)))
		{
			if ((GameManager.getPlayerGame(p).getGameState() != GameState.INGAME) || (GameManager.getPlayerGame(damager).getGameState() != GameState.INGAME))
			{
				e.setCancelled(true);
			}
		}
	}
	
}
