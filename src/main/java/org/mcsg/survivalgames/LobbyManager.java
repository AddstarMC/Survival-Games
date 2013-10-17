package org.mcsg.survivalgames;

import java.util.HashSet;
import java.util.List;

import org.bukkit.Chunk;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.mcsg.survivalgames.lobbysigns.LobbySign;
import org.mcsg.survivalgames.lobbysigns.LobbySignManager;
import org.mcsg.survivalgames.lobbysigns.LobbySignType;
import org.mcsg.survivalgames.lobbysigns.LobbySignWinner;

public class LobbyManager {

	private static LobbyManager instance = null;
	public static HashSet < Chunk > lobbychunks = new HashSet < Chunk > ();
	LobbySignManager signManager = null;
	
	private LobbyManager(LobbySignManager signManager) {
		this.signManager = signManager;
		signManager.loadSigns();
		updateAll();
	}
	
	public static void createInstance(LobbySignManager signManager) {
		instance = new LobbyManager(signManager);
	}

	public static LobbyManager getInstance() {
		return instance;
	}

	public void updateAll() {
		signManager.updateSigns();
	}

	public void updateWall(int gameId) {
		signManager.updateSigns(gameId);
	}

	public void removeSignsForArena(int arena) {
		signManager.removeArena(arena);
	}
	
	public void gameEnd(int gameID, Player winner) {
		
		List<LobbySign> joinSigns = signManager.getSignsByType(gameID, LobbySignType.Join);
		for (LobbySign sign : joinSigns) {
			Location location = new Location(sign.getLocation().getWorld(), sign.getLocation().getX(), sign.getLocation().getY() + 2, sign.getLocation().getZ());				
			FireworkFactory.LaunchFirework(location, FireworkEffect.Type.BALL_LARGE, 1, Color.GREEN);
		}
		
		List<LobbySign> winnerSign = signManager.getSignsByType(gameID, LobbySignType.Winner);
		for (LobbySign sign : winnerSign) {
			((LobbySignWinner)sign).setWinner(winner.getName());
			sign.update();
		}
		
	}
}