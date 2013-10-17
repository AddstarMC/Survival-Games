package org.mcsg.survivalgames.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
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

	@EventHandler
    public void PlayerJoin(PlayerJoinEvent e) {
        final Player p = e.getPlayer();
        
        if(GameManager.getInstance().getBlockGameId(p.getLocation()) != -1){
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){
                public void run(){
                    p.teleport(SettingsManager.getInstance().getLobbySpawn());

                }
            }, 5L);
        }
    }
    
}
