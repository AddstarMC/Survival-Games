package org.mcsg.survivalgames.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.mcsg.survivalgames.GameManager;
import org.mcsg.survivalgames.SettingsManager;


public class JoinEvent implements Listener {
    
    Plugin plugin;
    
    public JoinEvent(Plugin plugin){
        this.plugin = plugin;
    }

	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void PlayerJoin(PlayerJoinEvent e) {
        final Player p = e.getPlayer();

        if (GameManager.getInstance().getBlockGameId(p.getLocation()) != -1) {
        	// Send to lobby if the player re-logs inside the arena
        	p.teleport(SettingsManager.getInstance().getLobbySpawn());

            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                if ((p.isOnline()) && (GameManager.getInstance().getBlockGameId(p.getLocation()) != -1)) {
                    // Safe guard in case previous teleport didn't work (it sometimes leaves players in the arena after relogging)
                    p.teleport(SettingsManager.getInstance().getLobbySpawn());
                }
            }, 15L);
        }
    }
    
}
