package org.mcsg.survivalgames.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.mcsg.survivalgames.GameManager;

public class LogoutEvent implements Listener{
    @EventHandler
    public void PlayerLoggout(PlayerQuitEvent e){
        Player p = e.getPlayer();
        GameManager.getInstance().removeFromOtherQueues(p, -1);
        int id = GameManager.getInstance().getPlayerGameId(p);
        if(GameManager.getInstance().isSpectator(p))
        	GameManager.getInstance().removeSpectator(p);

        if(id == -1) return;

        GameManager.getInstance().getGame(id).playerLeave(p, false);
    }
}
