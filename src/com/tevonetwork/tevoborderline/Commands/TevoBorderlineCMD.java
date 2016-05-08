package com.tevonetwork.tevoborderline.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.tevonetwork.tevoapi.Core.Messages.AuthorMSG;
import com.tevonetwork.tevoborderline.TevoBorderline;

public class TevoBorderlineCMD implements CommandExecutor{

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		AuthorMSG.sendAuthorStamp("Borderline", TevoBorderline.getInstance().getDescription().getVersion(), sender);
		return true;
	}

	
	
}
