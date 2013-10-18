package org.mcsg.survivalgames.events;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.mcsg.survivalgames.GameManager;
import org.mcsg.survivalgames.SettingsManager;
import org.mcsg.survivalgames.SurvivalGames;

public class RespawnEvent {
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled=true)
	public void onRespawn(PlayerRespawnEvent event) {
		Player p = event.getPlayer();
		SurvivalGames.$("RESPAWN EVENT!");
		if (GameManager.getInstance().getBlockGameId(p.getLocation()) != -1) {
			SurvivalGames.$(" >>>RESPAWN WITHIN SG ARENA");
			Location loc = SettingsManager.getInstance().getLobbySpawn();
			SurvivalGames.$(" >>>Lobby: " + loc);
            event.setRespawnLocation(loc);
		}
	}
}
