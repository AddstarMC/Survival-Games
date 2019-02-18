package org.mcsg.survivalgames.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mcsg.survivalgames.GameManager;
import org.mcsg.survivalgames.MessageManager;
import org.mcsg.survivalgames.SettingsManager;

public class Spectate implements SubCommand{

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
    	// Only players can use this command
    	if (!(sender instanceof Player)) {
            MessageManager.getInstance().sendFMessage(MessageManager.PrefixType.ERROR, "error.notingame", sender);
            return true;
    	}
    	Player player = (Player) sender;
        if(args.length == 0){
            return spectatorExit(player);
        } else {
        	if (args[0].equalsIgnoreCase("leave")) {
                return spectatorExit(player);
            } else {
	        	if(SettingsManager.getInstance().getSpawnCount(Integer.parseInt(args[0])) == 0){
		            MessageManager.getInstance().sendMessage(MessageManager.PrefixType.ERROR, "error.nospawns", player);
		            return true;
		        }
		        if(GameManager.getInstance().isPlayerActive(player)){
		            MessageManager.getInstance().sendMessage(MessageManager.PrefixType.ERROR, "error.specingame", player);
		            return true;
		        }
		        GameManager.getInstance().getGame(Integer.parseInt(args[0])).addSpectator(player);
        	}
        }
        return true;
    }

    private boolean spectatorExit(Player player) {
        if (GameManager.getInstance().isSpectator(player)) {
            GameManager.getInstance().removeSpectator(player);
            return true;
        } else {
            MessageManager.getInstance().sendFMessage(MessageManager.PrefixType.ERROR, "error.notspecified", player, "input-Game ID");
            return true;
        }
    }

    @Override
    public String help(CommandSender s) {
        return "/sg spectate <id> - " + SettingsManager.getInstance().getMessageConfig().getString("messages.help.spectate", "Spectate a running arena");
    }

	@Override
	public String permission() {
		return "sg.arena.spectate";
	}

}
