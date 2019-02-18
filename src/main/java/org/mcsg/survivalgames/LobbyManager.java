package org.mcsg.survivalgames;

import java.util.HashSet;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.mcsg.survivalgames.lobbysigns.LobbySign;
import org.mcsg.survivalgames.lobbysigns.LobbySignManager;
import org.mcsg.survivalgames.lobbysigns.LobbySignType;
import org.mcsg.survivalgames.lobbysigns.LobbySignWinner;
import org.mcsg.survivalgames.lobbysigns.LobbySignWinnerSign;

public class LobbyManager {

	private static LobbyManager instance;
    public static HashSet<Chunk> lobbychunks = new HashSet<>();
	LobbySignManager signManager;
	
	private LobbyManager(final LobbySignManager signManager) {
		this.signManager = signManager;
		signManager.loadSigns();
        this.updateAll();
	}
	
	public static void createInstance(final LobbySignManager signManager) {
		instance = new LobbyManager(signManager);
	}

	public static LobbyManager getInstance() {
		return instance;
	}

	public void updateAll() {
        this.signManager.updateSigns();
	}

	public void updateWall(final int gameId) {
        this.signManager.updateSigns(gameId);
	}

	public void removeSignsForArena(final int arena) {
        this.signManager.removeArena(arena);
	}
	
	public void gameEnd(final int gameID, final Player winner) {
		final Location loc = winner.getLocation();
		launchEndFireworks(loc);
				
		final List<LobbySign> winnerSign = this.signManager.getSignsByType(gameID, LobbySignType.Winner);
		for (final LobbySign sign : winnerSign) {
			
			if (!(sign instanceof LobbySignWinner))
				continue;
			
			((LobbySignWinner)sign).setWinner(winner.getDisplayName());
			sign.update();
		}

        final List<LobbySign> winnerSigns = this.signManager.getSignsByType(gameID, LobbySignType.WinnerSign);
        for (final LobbySign sign : winnerSigns) {
            ((LobbySignWinnerSign)sign).setWinner(winner.getDisplayName());
            sign.update();
        }
		
	}
	
	public static void launchEndFireworks(final Location loc) {
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(GameManager.getInstance().getPlugin(), () -> {
            FireworkFactory.LaunchFirework(loc, FireworkEffect.Type.BALL_LARGE, 0, new Color[]{Color.WHITE, Color.BLUE, Color.SILVER}, false, false, 1, 3);
            FireworkFactory.LaunchFirework(loc, FireworkEffect.Type.BURST, 2, new Color[]{Color.ORANGE, Color.RED, Color.WHITE}, true, true, 10, 0);
            FireworkFactory.LaunchFirework(loc, FireworkEffect.Type.STAR, 1, new Color[]{Color.RED, Color.YELLOW}, true, true, 10, 0);
            FireworkFactory.LaunchFirework(loc, FireworkEffect.Type.BALL_LARGE, 1, new Color[]{Color.SILVER, Color.RED}, true, true, 10, 0);
            FireworkFactory.LaunchFirework(loc, FireworkEffect.Type.BALL_LARGE, 0, new Color[]{Color.YELLOW, Color.SILVER}, true, true, 20, 0);
            //FireworkFactory.LaunchFirework(loc, FireworkEffect.Type.BURST, 1, Color.FUCHSIA);
		}, 15);
	}
}