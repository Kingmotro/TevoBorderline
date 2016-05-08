package com.tevonetwork.tevoborderline.Game.spectate;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;

import com.tevonetwork.tevoapi.API.Math.MathUtils;
import com.tevonetwork.tevoapi.API.Util.CC;
import com.tevonetwork.tevoapi.API.Util.ItemStackFactory;
import com.tevonetwork.tevoborderline.Game.GameManager;

public class MenuUpdater implements Runnable {

	@Override
	public void run() {
		for (Player players : Bukkit.getOnlinePlayers()) {
			if (players.getOpenInventory().getTitle().equalsIgnoreCase("Spectate")) {
				if (GameManager.isSpectating(players)) {
					InventoryView menu = players.getOpenInventory();
					ArrayList<Player> gplayers = GameManager.getPlayerGame(players).getPlayerList();
					ItemStackFactory isf = new ItemStackFactory();
					int index = 0;
					for (Player gameplayer : gplayers) {
						menu.setItem(index,
								isf.createItemStackPlayerHeadwithLore(CC.tnPlayer + gameplayer.getName(), gameplayer.getName(),
										new String[] { CC.tnInfo + "Health: " + CC.tnValue + MathUtils.trim(2, gameplayer.getHealth()), CC.tnInfo + "Kills: " + CC.tnValue + gameplayer.getLevel(),
												CC.tnUse + "(Click to teleport)" }));
						index++;
					}
				}
			}
		}
	}

}
