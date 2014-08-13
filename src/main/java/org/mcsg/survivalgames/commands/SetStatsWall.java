package org.mcsg.survivalgames.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mcsg.survivalgames.MessageManager;
import org.mcsg.survivalgames.SettingsManager;



public class SetStatsWall implements SubCommand {

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
    	// Only players can use this command
    	if (!(sender instanceof Player)) {
            MessageManager.getInstance().sendFMessage(MessageManager.PrefixType.ERROR, "error.notingame", sender);
            return true;
    	}
        //StatsWallManager.getInstance().setStatsSignsFromSelection(player);
        return false;
    }

    
    public String help(CommandSender s){
        return "/sg setstatswall - "+ SettingsManager.getInstance().getMessageConfig().getString("messages.help.setstatswall", "Sets the stats wall");
    }

	@Override
	public String permission() {
		return null;
	}
}