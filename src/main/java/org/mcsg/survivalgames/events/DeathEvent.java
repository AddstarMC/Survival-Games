package org.mcsg.survivalgames.events;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.mcsg.survivalgames.Game;
import org.mcsg.survivalgames.Game.GameMode;
import org.mcsg.survivalgames.GameManager;
import org.mcsg.survivalgames.SurvivalGames;

public class DeathEvent implements Listener {
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled=true)
	public void onPlayerDamage(EntityDamageEvent event) {
		if (!(event.getEntity() instanceof Player)) return;

		Player player = (Player)event.getEntity();
		int gameid = GameManager.getInstance().getPlayerGameId(player);
		if (gameid==-1) return;
		if (!GameManager.getInstance().isPlayerActive(player)) return;

		Game game = GameManager.getInstance().getGame(gameid);

		// Only allow players to be damaged while the game is in progress
		if (game.isProtectionOn() || game.getMode() != GameMode.INGAME) {
			event.setCancelled(true);
			return;
		}
	}
	
	@EventHandler(ignoreCancelled=true)
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = (Player)event.getEntity();
		GameManager gm = GameManager.getInstance();
		int gameid = gm.getPlayerGameId(player);
		if (gameid == -1) return;
		if (!gm.isPlayerActive(player)) return;

		SurvivalGames.$(gameid, "Player died: " + player.getName() + " (" + event.getDeathMessage() + ")");

		// Show alive/dead player lists, for troubleshooting/informational purposes
		ArrayList <String> alive = new ArrayList <String>();
		ArrayList <String> dead = new ArrayList <String>();
		for (Player p : gm.getGame(gameid).getAllPlayers()) {
			if (gm.isPlayerActive(p)) {
				alive.add(p.getName());
			} else {
				dead.add(p.getName());
			}
		}
		SurvivalGames.$(gameid, "Players alive: " + StringUtils.join(alive.toArray(), ", "));
		SurvivalGames.$(gameid, "Players dead : " + StringUtils.join(dead.toArray(), ", "));
		
		event.setDeathMessage(null);
		gm.getGame(gameid).playerDeath(event);
	}

}