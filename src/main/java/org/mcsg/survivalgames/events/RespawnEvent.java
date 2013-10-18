package org.mcsg.survivalgames.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.mcsg.survivalgames.GameManager;
import org.mcsg.survivalgames.SettingsManager;

public class RespawnEvent {
	@EventHandler(ignoreCancelled=true)
	public void onRespawn(PlayerRespawnEvent event) {
		Player p = event.getPlayer();
		if (GameManager.getInstance().getBlockGameId(p.getLocation()) != -1) {
            event.setRespawnLocation(SettingsManager.getInstance().getLobbySpawn());
		}
	}
}
