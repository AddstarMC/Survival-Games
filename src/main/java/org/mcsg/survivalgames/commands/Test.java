package org.mcsg.survivalgames.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mcsg.survivalgames.LobbyManager;
import org.mcsg.survivalgames.MessageManager;
import org.mcsg.survivalgames.MessageManager.PrefixType;
import org.mcsg.survivalgames.SettingsManager;

public class Test implements SubCommand {

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
    	if (sender instanceof Player) {
	        if(!sender.hasPermission(permission()) && !sender.isOp()){
	            MessageManager.getInstance().sendFMessage(MessageManager.PrefixType.ERROR, "error.nopermission", sender);
	            return true;
	        }
    	}
        
        if(args.length < 1){
            sender.sendMessage(help(sender));
            return true;
        }

        String action = args[0];
        if (action.equals("endfireworks")) {
            MessageManager.getInstance().sendMessage(PrefixType.INFO, "Launching end of game fireworks at your location", sender);
        	Player p = (Player) sender;
        	LobbyManager.launchEndFireworks(p.getLocation());
        } else {
            MessageManager.getInstance().sendMessage(PrefixType.ERROR, ChatColor.RED + "Unknown test action \"" + action + "\"!", sender);
        }
        return true;
    }

    @Override
    public String help(CommandSender s) {
        return "/sg test <action> - " + SettingsManager.getInstance().getMessageConfig().getString("messages.help.test", "Performs a test for troubleshooting");
    }

	@Override
	public String permission() {
		return "sg.admin.test";
	}
}
