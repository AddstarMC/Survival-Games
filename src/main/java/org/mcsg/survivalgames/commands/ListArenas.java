package org.mcsg.survivalgames.commands;

import java.util.ArrayList;

import org.bukkit.entity.Player;
import org.mcsg.survivalgames.Game;
import org.mcsg.survivalgames.GameManager;
import org.mcsg.survivalgames.SettingsManager;

public class ListArenas implements SubCommand{
	
    public boolean onCommand(Player player, String[] args) {
    	
    	GameManager gameManager = GameManager.getInstance();
    	
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
        
    @Override
    public String help(Player p) {
        return "/sg listarenas - " + SettingsManager.getInstance().getMessageConfig().getString("messages.help.listarenas", "List all available arenas");
    }

	@Override
	public String permission() {
		return "";
	}
}