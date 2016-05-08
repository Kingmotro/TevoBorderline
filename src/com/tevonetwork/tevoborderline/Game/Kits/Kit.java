package com.tevonetwork.tevoborderline.Game.Kits;

import java.util.Collection;
import java.util.HashMap;
import org.bukkit.inventory.ItemStack;

public abstract class Kit {

	private ItemStack helmet;
	private ItemStack chestplate;
	private ItemStack leggings;
	private ItemStack boots;
	private ItemStack primary_Weapon;
	private ItemStack secondary_Weapon;
	private HashMap<Integer, ItemStack> extra_Items = new HashMap<Integer, ItemStack>();

	
	public ItemStack getPrimary_Weapon()
	{
		return this.primary_Weapon;
	}

	
	public void setPrimary_Weapon(ItemStack primary_Weapon)
	{
		this.primary_Weapon = primary_Weapon;
	}

	
	public ItemStack getSecondary_Weapon()
	{
		return this.secondary_Weapon;
	}

	
	public void setSecondary_Weapon(ItemStack secondary_Weapon)
	{
		this.secondary_Weapon = secondary_Weapon;
	}

	
	public ItemStack getHelmet()
	{
		return this.helmet;
	}

	
	public ItemStack getChestplate()
	{
		return this.chestplate;
	}

	
	public ItemStack getLeggings()
	{
		return this.leggings;
	}

	
	public ItemStack getBoots()
	{
		return this.boots;
	}
	
	public Collection<ItemStack> getExtra_Items()
	{
		return this.extra_Items.values();
	}
	
	public void setHelmet(ItemStack itemstack)
	{
		this.helmet = itemstack;
	}
	
	public void setChestplate(ItemStack itemstack)
	{
		this.chestplate = itemstack;
	}
	
	public void setLeggings(ItemStack itemstack)
	{
		this.leggings = itemstack;
	}
	
	public void setBoots(ItemStack itemstack)
	{
		this.boots = itemstack;
	}
	
	public void setprimary_Weapon(ItemStack itemstack)
	{
		this.primary_Weapon = itemstack;
	}
	
	public void setsecondary_Weapon(ItemStack itemstack)
	{
		this.secondary_Weapon = itemstack;
	}
	
	
}
