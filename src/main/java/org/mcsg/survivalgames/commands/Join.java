package org.mcsg.survivalgames.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mcsg.survivalgames.GameManager;
import org.mcsg.survivalgames.MessageManager;
import org.mcsg.survivalgames.SettingsManager;
import org.mcsg.survivalgames.MessageManager.PrefixType;

public class Join implements SubCommand{

	public boolean onCommand(CommandSender sender, String[] args) {
    	// Only players can use this command
    	if (!(sender instanceof Player)) {
            MessageManager.getInstance().sendFMessage(MessageManager.PrefixType.ERROR, "error.notingame", sender);
            return true;
    	}
    	Player player = (Player) sender;
		if(args.length >= 1){
			if(player.hasPermission(permission())){
				try {
					int a = Integer.parseInt(args[0]);
					GameManager.getInstance().addPlayer(player, a);
				} catch (NumberFormatException e) {
					String gameName = "";
					for (String arg : args) {
						gameName += arg + " ";
					}
					gameName = gameName.trim();
					
					if (gameName.equalsIgnoreCase("random")) {
						GameManager.getInstance().autoAddPlayer(player);
					} else {					
						int gameId = GameManager.getInstance().getIdFromName(gameName);
						if (gameId != -1) {
							GameManager.getInstance().addPlayer(player, gameId);
						} else {				
							MessageManager.getInstance().sendFMessage(PrefixType.ERROR, "error.notanumber", player, "input-" + args[0]);
						}
					}
				}
			}
			else{
				MessageManager.getInstance().sendFMessage(PrefixType.WARNING, "error.nopermission", player);
			}
		}
		else{
			if(player.hasPermission("sg.lobby.join")){
				if(GameManager.getInstance().getPlayerGameId(player)!=-1){
					MessageManager.getInstance().sendMessage(PrefixType.ERROR, "error.alreadyingame", player);
					return true;
				}
				player.teleport(SettingsManager.getInstance().getLobbySpawn());
				return true;
			}
			else{
				MessageManager.getInstance().sendFMessage(PrefixType.WARNING, "error.nopermission", player);
			}
		}
		return true;
	}

	@Override
	public String help(CommandSender s) {
		return "/sg join - " + SettingsManager.getInstance().getMessageConfig().getString("messages.help.join", "Join the lobby");
	}

	@Override
	public String permission() {
		return "sg.arena.join";
	}
}

