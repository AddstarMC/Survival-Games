package org.mcsg.survivalgames.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mcsg.survivalgames.GameManager;
import org.mcsg.survivalgames.MessageManager;
import org.mcsg.survivalgames.SettingsManager;

public class CreateArena implements SubCommand{

    public boolean onCommand(CommandSender sender, String[] args) {
    	// Only players can use this command
    	if (!(sender instanceof Player)) {
            MessageManager.getInstance().sendFMessage(MessageManager.PrefixType.ERROR, "error.notingame", sender);
            return true;
    	}
    	Player player = (Player) sender;
    	if(!player.hasPermission(permission()) && !player.isOp()){
            MessageManager.getInstance().sendFMessage(MessageManager.PrefixType.ERROR, "error.nopermission", player);
            return true;
        }
        GameManager.getInstance().createArenaFromSelection(player);
        return true;
    }

    @Override
    public String help(CommandSender p) {
        return "/sg createarena - " + SettingsManager.getInstance().getMessageConfig().getString("messages.help.createarena", "Create a new arena with the current WorldEdit selection");
    }

	@Override
	public String permission() {
		return "sg.admin.createarena";
	}
}