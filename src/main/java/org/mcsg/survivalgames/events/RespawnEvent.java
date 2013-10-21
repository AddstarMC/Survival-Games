package org.mcsg.survivalgames.events;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.mcsg.survivalgames.GameManager;
import org.mcsg.survivalgames.SettingsManager;

public class RespawnEvent implements Listener {
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled=true)
	public void onRespawn(PlayerRespawnEvent event) {
		Player p = event.getPlayer();
		if (GameManager.getInstance().getBlockGameId(p.getLocation()) != -1) {
			Location loc = SettingsManager.getInstance().getLobbySpawn();
            event.setRespawnLocation(loc);
		}
	}
}
