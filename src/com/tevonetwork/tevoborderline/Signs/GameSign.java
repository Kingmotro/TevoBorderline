package com.tevonetwork.tevoborderline.Signs;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import com.tevonetwork.tevoapi.API.Util.CC;
import com.tevonetwork.tevoborderline.TevoBorderline;
import com.tevonetwork.tevoborderline.Game.Game;
import com.tevonetwork.tevoborderline.Game.GameManager;
import com.tevonetwork.tevoborderline.Game.GameState;

public class GameSign {

	private Location sign_Location;
	private Sign sign;
	private int gameID;
	private GameState current_state;
	private TevoBorderline main = TevoBorderline.getInstance();
	
	public GameSign(int gameID, Location location)
	{
		this.sign_Location = location;
		if ((location.getBlock().getType() != Material.WALL_SIGN) && (location.getBlock().getType() != Material.SIGN_POST))
		{
			return;
		}
		if (!GameManager.doesGameExist(gameID))
		{
			return;
		}
		this.gameID = gameID;
		this.sign = (Sign)location.getBlock().getState();
		this.sign.setLine(0, CC.tnHead + "Borderline");
		Game game = GameManager.getGame(this.gameID);
		this.sign.setLine(1, CC.tnValue + game.getMap_Name());
		this.sign.setLine(2, GameState.getStateString(game.getGameState()));
		this.sign.setLine(3, CC.tnValue + game.getPlayers() + CC.tnInfo + "/" + CC.tnValue + game.getMaxPlayers());
		this.sign.setMetadata("GameID", new FixedMetadataValue(main, this.gameID));
		this.sign.update(true);
		this.current_state = game.getGameState();
	}
	
	public Location getLocation()
	{
		return this.sign_Location;
	}
	
	public GameState getCurrentGameState()
	{
		return this.current_state;
	}
	
	public int getGameID()
	{
		return this.gameID;
	}
	
	private void clear()
	{
		this.sign = (Sign)this.sign_Location.getBlock().getState();
		for (int line = 0; line < 4; line++)
		{
			this.sign.setLine(line, "");
		}
		new BukkitRunnable() {
			
			@Override
			public void run()
			{
				sign.update(true);
			}
		}.runTask(main);
	}
	
	public void update()
	{
		clear();
		this.sign = (Sign)this.sign_Location.getBlock().getState();
		Game game = GameManager.getGame(this.gameID);
		this.sign.setLine(0, CC.tnHead + "Borderline");
		this.sign.setLine(1, CC.tnValue + game.getMap_Name());
		this.sign.setLine(2, GameState.getStateString(game.getGameState()));
		if (game.getGameState() != GameState.INGAME)
		{
			this.sign.setLine(3, CC.tnValue + game.getPlayers() + CC.tnInfo + "/" + CC.tnValue + game.getMaxPlayers());
		}
		else
		{
			this.sign.setLine(3, CC.tnInfo + "Players: " + CC.tnValue + game.getPlayers());
		}
		new BukkitRunnable() {
			
			@Override
			public void run()
			{
				sign.update(true);
			}
		}.runTask(main);
		this.current_state = game.getGameState();
	}
	
}
