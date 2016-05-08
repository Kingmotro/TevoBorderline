package com.tevonetwork.tevoborderline.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.tevonetwork.tevoapi.Core.Category;
import com.tevonetwork.tevoapi.Core.Messages.CategoryMSG;
import com.tevonetwork.tevoborderline.Game.GameManager;

public class LeaveCMD implements CommandExecutor{

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if (sender instanceof Player)
		{
			Player p = (Player)sender;
			if ((GameManager.isPlaying(p)) || (GameManager.isSpectating(p)))
			{
				GameManager.removePlayer(p, false);
			}
		}
		else
		{
			CategoryMSG.senderMessagePlayersOnly(sender, Category.GAME);
		}
		return false;
	}

}
