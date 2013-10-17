package org.mcsg.survivalgames.commands;

import java.util.ArrayList;

import org.bukkit.entity.Player;
import org.mcsg.survivalgames.Game;
import org.mcsg.survivalgames.GameManager;
import org.mcsg.survivalgames.MessageManager;
import org.mcsg.survivalgames.SettingsManager;

public class ListPlayers implements SubCommand{

	@Override
	public boolean onCommand(Player player, String[] args) {
		int gid = 0;
		try{
			GameManager gameManager = GameManager.getInstance();
			if(args.length == 0){
				gid =gameManager.getPlayerGameId(player);
			}
			else{
				gid =  Integer.parseInt(args[0]);
			}

			String gameString = gameManager.getStringList(gid);
			if (gameString != null) {
				// list players in arena
				String[] msg = gameString.split("\n");
				player.sendMessage(msg);
				return false;
			}
			else {
				// list all arenas
				ArrayList<Game> games = gameManager.getGames();
				if (games.isEmpty()) {
		    		player.sendMessage(SettingsManager.getInstance().getMessageConfig().getString("messages.words.noarenas", "No arenas exist"));
		        	return false;
		    	}
				
				for (Game game : games) {
					player.sendMessage(game.getID() + " - " + Game.GetColorPrefix(game.getGameMode()) + game.getName() + " - " + game.getGameMode() + " - Players (" + game.getActivePlayers() + "/" + SettingsManager.getInstance().getSpawnCount(game.getID()) + ")");
				}
				return false;
			}
        } catch (NumberFormatException ex) {
            MessageManager.getInstance().sendFMessage(MessageManager.PrefixType.ERROR, "error.notanumber", player, "input-Arena");
        }
		return false;
	}

	@Override
	public String help(Player p) {
        return "/sg list [id]- " + SettingsManager.getInstance().getMessageConfig().getString("messages.help.listplayers","List all players in the arena you are playing in, or lists all arenas if you are not in a game.");
	}

	@Override
	public String permission() {
		return "";
	}

}