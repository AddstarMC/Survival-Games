package org.mcsg.survivalgames.commands;

import java.util.ArrayList;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mcsg.survivalgames.Game;
import org.mcsg.survivalgames.GameManager;
import org.mcsg.survivalgames.MessageManager;
import org.mcsg.survivalgames.SettingsManager;
import org.mcsg.survivalgames.MessageManager.PrefixType;

public class ListPlayers implements SubCommand{

	@Override
	public boolean onCommand(CommandSender sender, String[] args) {
		Player player = null;
		if (sender instanceof Player) {
	        if(!sender.hasPermission(permission()) && !sender.isOp()){
	            MessageManager.getInstance().sendFMessage(MessageManager.PrefixType.ERROR, "error.nopermission", sender);
	            return false;
	        }
	        player = (Player) sender;
    	}

		try {
			GameManager gameManager = GameManager.getInstance();
			Game game = null;
			
			if (args.length == 0) {
				// No game ID specified, check if player is in game
				if (player != null) {
					int gid = gameManager.getPlayerGameId(player);
					if (gid > 0) {
						game = gameManager.getGame(gid);
					}
				}
			} else {
				// Game ID specified, check if it's valid
				game = gameManager.getGame(Integer.parseInt(args[0]));
				if (game == null) {
					MessageManager.getInstance().sendMessage(PrefixType.ERROR, "The game you specified is not valid.", sender);
					return false;
				}
			}
			
			if (game != null) {
				// list players in arena
				sender.sendMessage(game.getID() + " - " + Game.GetColorPrefix(game.getGameMode()) + game.getName() + " - " + game.getGameMode() + " - Players (" + game.getActivePlayers() + "/" + SettingsManager.getInstance().getSpawnCount(game.getID()) + ")");
				if (game.getActivePlayers() > 0) {
					String gameString = gameManager.getStringList(game.getID());
					sender.sendMessage(gameString.split("\n"));
				}
				return false;
			}
			else {
				// list all arenas
				ArrayList<Game> games = gameManager.getGames();
				if (games.isEmpty()) {
		    		sender.sendMessage(SettingsManager.getInstance().getMessageConfig().getString("messages.words.noarenas", "No arenas exist"));
		        	return false;
		    	}
				
				for (Game g : games) {
					sender.sendMessage(g.getID() + " - " + Game.GetColorPrefix(g.getGameMode()) + g.getName() + " - " + g.getGameMode() + " - Players (" + g.getActivePlayers() + "/" + SettingsManager.getInstance().getSpawnCount(g.getID()) + ")");
					if (g.getActivePlayers() > 0) {
						String playerlist = gameManager.getStringList(g.getID());
						sender.sendMessage(playerlist.split("\n"));
					}
				}
				return false;
			}
        } catch (NumberFormatException ex) {
            MessageManager.getInstance().sendFMessage(MessageManager.PrefixType.ERROR, "error.notanumber", sender, "input-Arena");
        }
		return false;
	}

	@Override
	public String help(CommandSender s) {
        return "/sg list [id]- " + SettingsManager.getInstance().getMessageConfig().getString("messages.help.listplayers","List all players in the arena you are playing in, or lists all arenas if you are not in a game.");
	}

	@Override
	public String permission() {
		return "sg.admin.list";
	}

}