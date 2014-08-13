package org.mcsg.survivalgames.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mcsg.survivalgames.Game;
import org.mcsg.survivalgames.MessageManager;
import org.mcsg.survivalgames.Game.GameMode;
import org.mcsg.survivalgames.GameManager;
import org.mcsg.survivalgames.SettingsManager;

public class Enable implements SubCommand{

	@Override
	public boolean onCommand(CommandSender sender, String[] args) {        
    	if (sender instanceof Player) {
	        if(!sender.hasPermission(permission()) && !sender.isOp()){
	            MessageManager.getInstance().sendFMessage(MessageManager.PrefixType.ERROR, "error.nopermission", sender);
	            return true;
	        }
    	}
		try{
			if(args.length == 0){
				for(Game g:GameManager.getInstance().getGames()){
					if(g.getMode() == GameMode.DISABLED)
						g.enable();
				}
				MessageManager.getInstance().sendFMessage(MessageManager.PrefixType.INFO, "game.all", sender, "input-enabled");
			}
			else{
				GameManager.getInstance().enableGame(Integer.parseInt(args[0]));
				MessageManager.getInstance().sendFMessage(MessageManager.PrefixType.INFO, "game.state", sender, "arena-" + args[0], "input-enabled");
			}
		} catch (NumberFormatException e) {
			MessageManager.getInstance().sendFMessage(MessageManager.PrefixType.ERROR, "error.notanumber", sender, "input-Arena");
		} catch (NullPointerException e) {
			MessageManager.getInstance().sendFMessage(MessageManager.PrefixType.ERROR, "error.gamedoesntexist", sender, "arena-" + args[0]);
		}
		return true;

	}


	@Override
	public String help(CommandSender s) {
		return "/sg enable <id> - " + SettingsManager.getInstance().getMessageConfig().getString("messages.help.enable", "Enables arena <id>");
	}

	@Override
	public String permission() {
		return "sg.arena.enable";
	}
}