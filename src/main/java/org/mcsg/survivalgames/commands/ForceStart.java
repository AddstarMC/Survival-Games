package org.mcsg.survivalgames.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mcsg.survivalgames.Game;
import org.mcsg.survivalgames.GameManager;
import org.mcsg.survivalgames.MessageManager;
import org.mcsg.survivalgames.MessageManager.PrefixType;
import org.mcsg.survivalgames.SettingsManager;

public class ForceStart implements SubCommand {

	MessageManager msgmgr = MessageManager.getInstance();

	public boolean onCommand(CommandSender sender, String[] args) {
		Player player = null;
		if (sender instanceof Player) {
	        if(!sender.hasPermission(permission()) && !sender.isOp()){
	            MessageManager.getInstance().sendFMessage(MessageManager.PrefixType.ERROR, "error.nopermission", sender);
	            return true;
	        }
	        player = (Player) sender;
    	}

		if (!sender.hasPermission(permission()) && !sender.isOp()) {
			MessageManager.getInstance().sendFMessage(PrefixType.ERROR, "error.nopermission", sender);
			return true;
		}
		int game = -1;
		int seconds = 10;
		if(args.length == 2){
			seconds = Integer.parseInt(args[1]);
		}
		if(args.length >= 1){
			game = Integer.parseInt(args[0]);
		} else {
			if (player == null) {
	            MessageManager.getInstance().sendFMessage(MessageManager.PrefixType.ERROR, "error.notingame", sender);
	            return true;
			} else {
				game  = GameManager.getInstance().getPlayerGameId(player);
			}
		}
		
		if(game == -1){
			MessageManager.getInstance().sendMessage(PrefixType.ERROR, "No valid game found.", sender);
			return true;
		}
		
		Game g = GameManager.getInstance().getGame(game);
		if(g.getActivePlayers() < 2){
			MessageManager.getInstance().sendFMessage(PrefixType.ERROR, "error.notenoughtplayers", sender);
			return true;
		}

		if (g.getMode() != Game.GameMode.WAITING && !sender.hasPermission("sg.arena.restart")) {
			MessageManager.getInstance().sendFMessage(PrefixType.ERROR, "error.alreadyingame", sender);
			return true;
		}
		for (Player pl : g.getAllPlayers()) {
			g.getScoreboard().playerLiving(pl);
		}
		g.countdown(seconds);

		msgmgr.sendFMessage(PrefixType.INFO, "game.started", sender, "arena-" + game);

		return true;
	}

	@Override
	public String help(CommandSender s) {
		return "/sg forcestart [id] [time] - " + SettingsManager.getInstance().getMessageConfig().getString("messages.help.forcestart", "Forces the game to start");
	}

	@Override
	public String permission() {
		return "sg.arena.start";
	}
}