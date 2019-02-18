package org.mcsg.survivalgames.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mcsg.survivalgames.Game;
import org.mcsg.survivalgames.GameManager;
import org.mcsg.survivalgames.MessageManager;
import org.mcsg.survivalgames.SettingsManager;

public class Disable implements SubCommand{

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
            for(Game g: GameManager.getInstance().getGames()){
                g.disable();
            }
                MessageManager.getInstance().sendFMessage(MessageManager.PrefixType.INFO, "game.all", sender, "input-disabled");

        }else{

            GameManager.getInstance().disableGame(Integer.parseInt(args[0]));
                MessageManager.getInstance().sendFMessage(MessageManager.PrefixType.INFO, "game.state", sender, "arena-" + args[0], "input-disabled");
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
        return "/sg disable <id> - " + SettingsManager.getInstance().getMessageConfig().getString("messages.help.disable", "Disables arena <id>");
    }
	@Override
	public String permission() {
		return "sg.arena.disable";
	}
}
