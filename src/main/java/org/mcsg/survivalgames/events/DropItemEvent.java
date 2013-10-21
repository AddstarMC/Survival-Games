package org.mcsg.survivalgames.events;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.mcsg.survivalgames.Game;
import org.mcsg.survivalgames.GameManager;
import org.mcsg.survivalgames.Game.GameMode;

public class DropItemEvent implements Listener {
	@EventHandler(ignoreCancelled=true)
	public void onPlayerDrop(PlayerDropItemEvent event) {
		Player player = event.getPlayer();
		int gameid = GameManager.getInstance().getPlayerGameId(player);
		if (gameid==-1) return;
		if (!GameManager.getInstance().isPlayerActive(player)) return;
		Game game = GameManager.getInstance().getGame(gameid);

		if (game.getMode() == GameMode.WAITING || game.getMode() == GameMode.STARTING) {
			player.sendMessage(ChatColor.RED + "You cannot drop items before the game has started!");
			event.setCancelled(true);
		}
	}
}
