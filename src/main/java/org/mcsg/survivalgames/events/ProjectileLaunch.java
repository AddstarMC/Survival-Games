package org.mcsg.survivalgames.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.mcsg.survivalgames.Game;
import org.mcsg.survivalgames.GameManager;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 13/04/2019.
 */
public class ProjectileLaunch implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        final ProjectileSource source = event.getEntity().getShooter();
        if (source instanceof Player) {
            Game.GameMode mo3 = GameManager.getInstance().getGameMode(GameManager.getInstance().getPlayerGameId((Player) source));
            if (GameManager.getInstance().isPlayerActive((Player) source) && mo3 != Game.GameMode.INGAME) {
                event.setCancelled(true);
            }
        }
    }
}
