package org.mcsg.survivalgames.events;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;
import org.mcsg.survivalgames.Game;
import org.mcsg.survivalgames.GameManager;
import org.mcsg.survivalgames.Game.GameMode;

public class MoveEvent implements Listener{
    HashMap<UUID, Vector> playerpos = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled=true)
    public void frozenSpawnHandler(PlayerMoveEvent e) {
        if(GameManager.getInstance().getPlayerGameId(e.getPlayer()) == -1){
            playerpos.remove(e.getPlayer().getUniqueId());
            return;
        }
        if(GameManager.getInstance().getGame(GameManager.getInstance().getPlayerGameId(e.getPlayer())).getMode() == Game.GameMode.INGAME)
            return;
        GameMode mo3 = GameManager.getInstance().getGameMode(GameManager.getInstance().getPlayerGameId(e.getPlayer()));
        if(GameManager.getInstance().isPlayerActive(e.getPlayer()) && mo3 != Game.GameMode.INGAME){
            if(playerpos.get(e.getPlayer().getUniqueId()) == null){
                playerpos.put(e.getPlayer().getUniqueId(), e.getPlayer().getLocation().toVector());
                return;
            }
            Location l = e.getPlayer().getLocation();
            Vector v = playerpos.get(e.getPlayer().getUniqueId());
            if(l.getBlockX() != v.getBlockX()  || l.getBlockZ() != v.getBlockZ()){
                l.setX(v.getBlockX() + .5);
                l.setZ(v.getBlockZ() + .5);
                l.setYaw(e.getPlayer().getLocation().getYaw());
                l.setPitch(e.getPlayer().getLocation().getPitch());
                e.getPlayer().teleport(l);
            }
        }
    }
}
