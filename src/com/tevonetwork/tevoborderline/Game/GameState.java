package com.tevonetwork.tevoborderline.Game;

public enum GameState
{
	STARTING("§aSTARTING"), WAITING("§eWAITING"), FINISHING("§8FINISHING"), FULL("§6FULL"), INGAME("§7IN-GAME"), ERROR("§cERROR"), STARTUP("§5STARTUP");
	
	private final String string;
	
	private GameState(String string)
	{
		this.string = string;
	}
	
	public static String getStateString(GameState state)
	{
		return state.string;
	}
}
