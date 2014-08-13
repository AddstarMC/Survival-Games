package org.mcsg.survivalgames.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mcsg.survivalgames.MessageManager;
import org.mcsg.survivalgames.SettingsManager;

public class ResetSpawns implements SubCommand{

    public boolean onCommand(CommandSender sender, String[] args) {
		if (sender instanceof Player) {
	        if(!sender.hasPermission(permission()) && !sender.isOp()){
	            MessageManager.getInstance().sendFMessage(MessageManager.PrefixType.ERROR, "error.nopermission", sender);
	            return true;
	        }
    	}

		try{
        SettingsManager.getInstance().getSpawns().set("spawns."+Integer.parseInt(args[0]), null);
        return true;
                } catch (NumberFormatException e) {
                    MessageManager.getInstance().sendFMessage(MessageManager.PrefixType.ERROR, "error.notanumber", sender, "input-Arena");
                } catch (NullPointerException e) {
                    MessageManager.getInstance().sendMessage(MessageManager.PrefixType.ERROR, "error.gamenoexist", sender);
                }
        return true;
    }   

    @Override
    public String help(CommandSender s) {
        return "/sg resetspawns <id> - " + SettingsManager.getInstance().getMessageConfig().getString("messages.help.resetspawns", "Resets spawns for Arena <id>");
    }

	@Override
	public String permission() {
		return "sg.admin.resetspawns";
	}
}