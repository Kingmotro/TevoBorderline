package com.tevonetwork.tevoborderline.Game.Kits;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MiscDisguise;

import org.bukkit.Effect;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import com.tevonetwork.tevoapi.API.Cooldown.Cooldown;
import com.tevonetwork.tevoapi.API.Cooldown.SilentCooldown;
import com.tevonetwork.tevoapi.API.Math.TimeUtils;
import com.tevonetwork.tevoapi.API.Util.CC;
import com.tevonetwork.tevoapi.API.Util.ItemStackFactory;
import com.tevonetwork.tevoapi.API.Util.UtilPlayer;
import com.tevonetwork.tevoapi.Core.Category;
import com.tevonetwork.tevoapi.Core.Items;
import com.tevonetwork.tevoapi.Core.Rank;
import com.tevonetwork.tevoborderline.TevoBorderline;

public class KitManager implements Runnable{

	private static TevoBorderline main = TevoBorderline.getInstance();
	private static HashMap<Player, Integer> witherlauncher_Charges = new HashMap<Player, Integer>();
	private static HashMap<Player, Long> witherlauncher_LastCharge = new HashMap<Player, Long>();
	
	public static void handleEvent(Event event)
	{
		if (event instanceof PlayerInteractEvent)
		{
			PlayerInteractEvent e = (PlayerInteractEvent)event;
			if ((e.getAction() != Action.RIGHT_CLICK_AIR) && (e.getAction() != Action.RIGHT_CLICK_BLOCK))
			{
				return;
			}
			Player p = e.getPlayer();
			if (!p.getItemInHand().hasItemMeta())
			{
				return;
			}
			if (p.getItemInHand().getItemMeta().getDisplayName().equalsIgnoreCase(CC.tnAbility + "Rifle"))
			{
				if (SilentCooldown.isPlayeronSilentCooldown(p, "Rifle"))
				{
					return;
				}
				final Arrow launched = p.launchProjectile(Arrow.class, p.getLocation().getDirection().multiply(6.5D));
				p.getWorld().playSound(p.getLocation(), Sound.FIREWORK_BLAST, 2F, 0.5F);
				launched.setMetadata("Ability", new FixedMetadataValue(main, "Rifle"));
				launched.setMetadata("AbilityOwner", new FixedMetadataValue(main, p.getName()));
				new BukkitRunnable() {
					
					@Override
					public void run()
					{
						launched.remove();
					}
				}.runTaskLater(main, 100L);
				SilentCooldown.addSilentCooldown(p, "Rifle", 2);
			}
			if (p.getItemInHand().getItemMeta().getDisplayName().equalsIgnoreCase(CC.tnAbility + "Grenade Launcher"))
			{
				if (Cooldown.isPlayeronCooldown(p, "Grenade Launcher"))
				{
					UtilPlayer.onc(p, "Grenade Launcher");
					return;
				}
				final Fireball launched = p.launchProjectile(Fireball.class, p.getLocation().getDirection().multiply(2.0D));
				launched.setIsIncendiary(false);
				launched.setMetadata("Ability", new FixedMetadataValue(main, "Grenade Launcher"));
				launched.setMetadata("AbilityOwner", new FixedMetadataValue(main, p.getName()));
				p.getWorld().playSound(p.getLocation(), Sound.GHAST_FIREBALL, 2F, 1.2F);
				Cooldown.addCooldown(p, "Grenade Launcher", 6);
				UtilPlayer.message(Category.ABILITY, p, CC.tnInfo + "You used " + CC.tnAbility + "Grenade Launcher" + CC.end);
				new BukkitRunnable() {
					@Override
					public void run()
					{
						launched.remove();
					}
				}.runTaskLater(main, 200L);
				
			}
			if (p.getItemInHand().getItemMeta().getDisplayName().equalsIgnoreCase(CC.tnAbility + "Wither Launcher"))
			{
				if (Cooldown.isPlayeronCooldown(p, "Wither Launcher"))
				{
					UtilPlayer.onc(p, "Wither Launcher");
					return;
				}
				if (!witherlauncher_LastCharge.containsKey(p))
				{
					witherlauncher_LastCharge.put(p, Long.valueOf(System.currentTimeMillis()));
					witherlauncher_Charges.put(p, 1);
					p.playSound(p.getLocation(), Sound.CLICK, 2F, 1.1F);
				}
				else
				{
					if (TimeUtils.hasElapsed(witherlauncher_LastCharge.get(p), 600L))
					{
						if (witherlauncher_Charges.containsKey(p))
						{
							if (witherlauncher_Charges.get(p) >= 3)
							{
								witherlauncher_Charges.remove(p);
								witherlauncher_LastCharge.remove(p);
								Cooldown.addCooldown(p, "Wither Launcher", 10);
								WitherSkull launched = p.launchProjectile(WitherSkull.class, p.getLocation().getDirection().multiply(1.5D));
								launched.setMetadata("Ability", new FixedMetadataValue(main, "Wither Launcher"));
								launched.setMetadata("AbilityOwner", new FixedMetadataValue(main, p.getName()));
								launched.setIsIncendiary(false);
								p.getWorld().playSound(p.getLocation(), Sound.WITHER_SHOOT, 2F, 1.2F);
								p.setExp(0.0F);
								UtilPlayer.message(Category.ABILITY, p, CC.tnInfo + "You used " + CC.tnAbility + "Wither Launcher" + CC.end);
							}
							else
							{
								int currentcharges = witherlauncher_Charges.get(p);
								p.playSound(p.getLocation(), Sound.CLICK, 2F, 1.0F + 0.2F * currentcharges);
								if (currentcharges == 1)
								{
									p.setExp(0.33F);
								}
								else if (currentcharges == 2)
								{
									p.setExp(0.66F);
								}
								else
								{
									p.setExp(0.99F);
								}
								witherlauncher_Charges.put(p, currentcharges + 1);
								witherlauncher_LastCharge.put(p, Long.valueOf(System.currentTimeMillis()));
							}
						}
					}
				}
			}
			else
			{
				if (witherlauncher_LastCharge.containsKey(p))
				{
					witherlauncher_LastCharge.remove(p);
					witherlauncher_Charges.remove(p);
				}
			}
			if (p.getItemInHand().getItemMeta().getDisplayName().equalsIgnoreCase(CC.tnAbility + "Throwing Axe"))
			{
				p.setItemInHand(null);
				p.updateInventory();
				Snowball knife = p.launchProjectile(Snowball.class);
				MiscDisguise dis = new MiscDisguise(DisguiseType.DROPPED_ITEM, 258);
				DisguiseAPI.disguiseToAll(knife, dis);
				knife.setMetadata("Ability", new FixedMetadataValue(main, "Throwing Axe"));
				knife.setMetadata("AbilityOwner", new FixedMetadataValue(main, p.getName()));
				if (UtilPlayer.hasRank(p, Rank.CRYSTAL))
				{
					knife.setMetadata("AbilityAttribute", new FixedMetadataValue(main, "AuthorisePickup"));
				}
				UtilPlayer.message(Category.ABILITY, p, CC.tnInfo + "You used " + CC.tnAbility + "Throwing Axe" + CC.end);
			}
		}
		if (event instanceof ProjectileHitEvent)
		{
			ProjectileHitEvent e = (ProjectileHitEvent)event;
			if (e.getEntity() instanceof Fireball)
			{
				Fireball fireball = (Fireball)e.getEntity();
				if (fireball.hasMetadata("Ability"))
				{
					if (fireball.getMetadata("Ability").get(0).asString().equalsIgnoreCase("Grenade Launcher"))
					{
						if (fireball.getShooter() instanceof Player)
						{
							Player shooter = (Player)fireball.getShooter();
							fireball.getLocation().getWorld().playSound(fireball.getLocation(), Sound.EXPLODE, 3F, 1.6F);
							fireball.getLocation().getWorld().spigot().playEffect(fireball.getLocation(), Effect.LAVA_POP, 0, 0, 5.0F, 3.0F, 5.0F, 1, 50, 30);
							fireball.getLocation().getWorld().spigot().playEffect(fireball.getLocation(), Effect.FLAME, 0, 0, 5.0F, 3.0F, 5.0F, 1, 50, 30);
							fireball.getLocation().getWorld().spigot().playEffect(fireball.getLocation(), Effect.SMALL_SMOKE, 0, 0, 5.0F, 3.0F, 5.0F, 1, 30, 30);
							fireball.getLocation().getWorld().spigot().playEffect(fireball.getLocation(), Effect.EXPLOSION_HUGE, 0, 0, 5.0F, 3.0F, 5.0F, 1, 2, 30);
							List<Player> damaged = new ArrayList<Player>();
							for (Entity entities : fireball.getNearbyEntities(2.0, 3.0, 2.0))
							{
								if (entities instanceof Player)
								{
									Player victim = (Player)entities;
									victim.damage(10, shooter);
									UtilPlayer.setLastDamageCause(victim, "Grenade Launcher");
									damaged.add(victim);
								}
							}
							for (Entity entities : fireball.getNearbyEntities(5.0, 3.0, 5.0))
							{
								if (entities instanceof Player)
								{
									Player victim = (Player)entities;
									if (!damaged.contains(victim))
									{
										victim.damage(5, shooter);
										UtilPlayer.setLastDamageCause(victim, "Grenade Launcher");
									}
								}
							}
						}
					}
				}
			}
			if (e.getEntity() instanceof WitherSkull)
			{
				WitherSkull skull = (WitherSkull)e.getEntity();
				if (skull.hasMetadata("Ability"))
				{
					if (skull.getMetadata("Ability").get(0).asString().equalsIgnoreCase("Wither Launcher"))
					{
						skull.getLocation().getWorld().playSound(skull.getLocation(), Sound.WITHER_IDLE, 2F, 1.2F);
						skull.getLocation().getWorld().spigot().playEffect(skull.getLocation(), Effect.POTION_SWIRL, 0, 0, 4.0F, 2.0F, 4.0F, 0, 50, 30);
						skull.getLocation().getWorld().spigot().playEffect(skull.getLocation(), Effect.EXPLOSION_LARGE, 0, 0, 4.0F, 2.0F, 4.0F, 0, 3, 30);
						for (Entity entities : skull.getNearbyEntities(4.0, 2.0, 4.0))
						{
							if (entities instanceof Player)
							{
								PotionEffect wither = new PotionEffect(PotionEffectType.WITHER, 60, 2);
								((Player)entities).addPotionEffect(wither);
							}
						}
					}
				}
			}
			if (e.getEntity() instanceof Snowball)
			{
				Snowball knife = (Snowball)e.getEntity();
				if (knife.hasMetadata("Ability"))
				{
					if (knife.getMetadata("Ability").get(0).asString().equalsIgnoreCase("Throwing Axe"))
					{
						ItemStack dropknife = new ItemStackFactory().createItemStack(Items.IRONAXE, CC.tnAbility + "Throwing Axe");
						Item drop = knife.getLocation().getWorld().dropItemNaturally(knife.getLocation(), dropknife);
						if ((knife.hasMetadata("AbilityAttribute")) && (knife.getMetadata("AbilityAttribute").get(0).asString().equalsIgnoreCase("AuthorisePickup")))
						{
							drop.setMetadata("AbilityAttribute", new FixedMetadataValue(main, "AuthorisePickup"));
							drop.setMetadata("AbilityOwner", new FixedMetadataValue(main, ((Player)knife.getShooter()).getName()));
						}
					}
				}
			}
		}
	}
	
	@Override
	public void run()
	{
		for (Player players : witherlauncher_LastCharge.keySet())
		{
			if (TimeUtils.hasElapsed(witherlauncher_LastCharge.get(players), 1000L))
			{
				players.setExp(0.0F);
				witherlauncher_Charges.remove(players);
				witherlauncher_LastCharge.remove(players);
			}
		}
	}
	
}
