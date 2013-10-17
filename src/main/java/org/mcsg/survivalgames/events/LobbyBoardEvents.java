package org.mcsg.survivalgames.events;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.mcsg.survivalgames.GameManager;
import org.mcsg.survivalgames.SurvivalGames;
import org.mcsg.survivalgames.lobbysigns.LobbySign;
import org.mcsg.survivalgames.lobbysigns.LobbySignJoin;
import org.mcsg.survivalgames.lobbysigns.LobbySignManager;
import org.mcsg.survivalgames.lobbysigns.LobbySignPlayerList;
import org.mcsg.survivalgames.lobbysigns.LobbySignPlayers;
import org.mcsg.survivalgames.lobbysigns.LobbySignState;
import org.mcsg.survivalgames.lobbysigns.LobbySignWinner;

public class LobbyBoardEvents implements Listener {
	
	@EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
		
		// We only care about clicking blocks
		final Action action = event.getAction();
		if (!(action == Action.RIGHT_CLICK_BLOCK || action == Action.LEFT_CLICK_BLOCK))
			return;
		
		Block block = event.getClickedBlock();
		
		// We only care about signs
		final Material blockType = block.getType();
		if (!(blockType == Material.SIGN || blockType == Material.SIGN_POST || blockType == Material.WALL_SIGN || blockType == Material.SKULL))
			return;
		
		// See if a lobby sign at the blocks location exists
		LobbySign sign = ((SurvivalGames)GameManager.getInstance().getPlugin()).getLobbySignManager().getSign(block.getLocation());
		if (sign == null)
			return;
		
		sign.execute(event.getPlayer());		
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onSignChange(SignChangeEvent event) {
		final Player player = event.getPlayer();

		if (!player.hasPermission("sg.lobby.createsign")) {
			return;
		}		

		Sign sign = (Sign)event.getBlock().getState();
		String createLine = event.getLine(1);
		
		int gameId = -1;
		try {
			gameId = Integer.parseInt(event.getLine(2));
		} catch(Exception ex) {
			gameId = -1;
		}
		
		LobbySign newLobbySign = null;
		boolean validSign = false;
		boolean validGameId = !(gameId == -1 || GameManager.getInstance().getGame(gameId) == null);
		
		// Join Sign
		if (createLine.equalsIgnoreCase("[sg-join]")) {
			validSign = true;
			if (validGameId) {
				newLobbySign = new LobbySignJoin(sign, gameId);
			}
		}
		// State sign
		else if (createLine.equalsIgnoreCase("[sg-state]")) {
			validSign = true;
			if (validGameId) {
				newLobbySign = new LobbySignState(sign, gameId);
			}
		}
		// Player sign
		else if (createLine.equalsIgnoreCase("[sg-players]")) {
			validSign = true;
			if (validGameId) {
				newLobbySign = new LobbySignPlayers(sign, gameId);
			}
		}
		// Player list sign
		else if (createLine.equalsIgnoreCase("[sg-playerlist]")) {
			validSign = true;
			if (validGameId) {
				newLobbySign = new LobbySignPlayerList(sign, gameId);
			}
		}
		// Player winner sign
		else if (createLine.equalsIgnoreCase("[sg-winner]")) {
			validSign = true;
			if (validGameId) {
				newLobbySign = new LobbySignWinner(sign, gameId);
			}
		}
		
		
		if (validSign && !validGameId) {
			player.sendMessage(ChatColor.DARK_RED + "Could not create sign for arena " + gameId + ".");
			return;
		}
		
		if (newLobbySign == null) {
			return;
		}
		
		String[] signContent = newLobbySign.setSignContent(event.getLines());		
		for (int line = 0; line < 4; ++line) {
			event.setLine(line, signContent[line]);
		}
		
		((SurvivalGames)GameManager.getInstance().getPlugin()).getLobbySignManager().addSign(newLobbySign);
		player.sendMessage("New " + newLobbySign.getType() + " for " + newLobbySign.getGame().getName() + " created.");
		
		newLobbySign.postCreationFixup();
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
		
		Block block = event.getBlock();
		LobbySignManager signManager = ((SurvivalGames)GameManager.getInstance().getPlugin()).getLobbySignManager();
		
		LobbySign sign = signManager.getSign(block.getLocation());
		if (sign == null)
			return;
		
		signManager.removeSign(sign);
		event.getPlayer().sendMessage("Removed " + sign.getType() + " lobby sign for " + sign.getGame().getName());
	}

}
