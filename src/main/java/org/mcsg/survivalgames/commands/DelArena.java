package org.mcsg.survivalgames.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.mcsg.survivalgames.Game;
import org.mcsg.survivalgames.GameManager;
import org.mcsg.survivalgames.LobbyManager;
import org.mcsg.survivalgames.MessageManager;
import org.mcsg.survivalgames.SettingsManager;
import org.mcsg.survivalgames.MessageManager.PrefixType;



public class DelArena implements SubCommand{

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
    	// Only players can use this command
    	if (!(sender instanceof Player)) {
            MessageManager.getInstance().sendFMessage(MessageManager.PrefixType.ERROR, "error.notingame", sender);
            return true;
    	}
    	Player player = (Player) sender;
        if (!player.hasPermission(permission()) && !player.isOp()){
            MessageManager.getInstance().sendFMessage(PrefixType.ERROR, "error.nopermission", player);
            return true;
        }
        
        if(args.length != 1){
            MessageManager.getInstance().sendFMessage(PrefixType.ERROR, "error.notspecified", player, "input-Arena");
            return true;
        }
        
        FileConfiguration s = SettingsManager.getInstance().getSystemConfig();
        //FileConfiguration spawn = SettingsManager.getInstance().getSpawns();
        int arena = Integer.parseInt(args[0]);
        Game g = GameManager.getInstance().getGame(arena);
        
        if(g == null){
            MessageManager.getInstance().sendFMessage(PrefixType.ERROR, "error.gamedoesntexist", player, "arena-" + arena);
            return true;
        }
        
        g.disable();
        s.set("sg-system.arenas."+arena+".enabled", false);
        s.set("sg-system.arenano", s.getInt("sg-system.arenano") - 1);
        //spawn.set("spawns."+arena, null);
        MessageManager.getInstance().sendFMessage(PrefixType.INFO, "info.deleted", player, "input-Arena");
        SettingsManager.getInstance().saveSystemConfig();
        GameManager.getInstance().hotRemoveArena(arena);
        //LobbyManager.getInstance().clearAllSigns();
        LobbyManager.getInstance().removeSignsForArena(arena);
        return true;
    }

    @Override
    public String help (CommandSender s) {
        return "/sg delarena <id> - " + SettingsManager.getInstance().getMessageConfig().getString("messages.help.delarena", "Delete an arena");
    }

	@Override
	public String permission() {
		return "sg.admin.deletearena";
	}   
}