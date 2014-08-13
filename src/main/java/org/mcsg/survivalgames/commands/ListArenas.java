package org.mcsg.survivalgames.commands;

import java.util.ArrayList;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mcsg.survivalgames.Game;
import org.mcsg.survivalgames.GameManager;
import org.mcsg.survivalgames.MessageManager;
import org.mcsg.survivalgames.SettingsManager;

public class ListArenas implements SubCommand{
	
    public boolean onCommand(CommandSender sender, String[] args) {
    	if (sender instanceof Player) {
	        if(!sender.hasPermission(permission()) && !sender.isOp()){
	            MessageManager.getInstance().sendFMessage(MessageManager.PrefixType.ERROR, "error.nopermission", sender);
	            return false;
	        }
    	}
    	
    	GameManager gameManager = GameManager.getInstance();
    	
    	// list all arenas
		ArrayList<Game> games = gameManager.getGames();
		if (games.isEmpty()) {
    		sender.sendMessage(SettingsManager.getInstance().getMessageConfig().getString("messages.words.noarenas", "No arenas exist"));
        	return false;
    	}
		
		for (Game game : games) {
			sender.sendMessage(game.getID() + " - " + Game.GetColorPrefix(game.getGameMode()) + game.getName() + " - " + game.getGameMode() + " - Players (" + game.getActivePlayers() + "/" + SettingsManager.getInstance().getSpawnCount(game.getID()) + ")");
		}
    	
        return false;
    }
        
    @Override
    public String help(CommandSender s) {
        return "/sg listarenas - " + SettingsManager.getInstance().getMessageConfig().getString("messages.help.listarenas", "List all available arenas");
    }

	@Override
	public String permission() {
		return "";
	}
}