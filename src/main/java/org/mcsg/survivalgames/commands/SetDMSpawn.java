package org.mcsg.survivalgames.commands;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mcsg.survivalgames.Game;
import org.mcsg.survivalgames.GameManager;
import org.mcsg.survivalgames.MessageManager;
import org.mcsg.survivalgames.SettingsManager;
import org.mcsg.survivalgames.SurvivalGames;

public class SetDMSpawn implements SubCommand{

    HashMap<Integer, Integer> next = new HashMap<>();

    public SetDMSpawn() {
    	
    }

    public void loadDMNextSpawn(){
        for(Game g:GameManager.getInstance().getGames().toArray(new Game[0])){ //Avoid Concurrency problems
            next.put(g.getID(), SettingsManager.getInstance().getDMSpawnCount(g.getID())+1);
        }
    }
    
    public boolean onCommand(CommandSender sender, String[] args) {
    	// Only players can use this command
    	if (!(sender instanceof Player)) {
            MessageManager.getInstance().sendFMessage(MessageManager.PrefixType.ERROR, "error.notingame", sender);
            return true;
    	}
    	Player player = (Player) sender;
        if (!player.hasPermission(permission()) && !player.isOp()) {
            MessageManager.getInstance().sendFMessage(MessageManager.PrefixType.ERROR, "error.nopermission", player);
            return true;
        }

        loadDMNextSpawn();
        Location l = player.getLocation();
        int game = GameManager.getInstance().getBlockGameId(l);
        SurvivalGames.info(0, "Setting DM spawn for arena " + game + " (" + next.get(game) + ")");
        if(game == -1){
            MessageManager.getInstance().sendMessage(MessageManager.PrefixType.ERROR, "error.notinarena", player);
            return true;
        }

        int i = 0;
        if(args[0].equalsIgnoreCase("next")){
            i = next.get(game);
            next.put(game, next.get(game)+1);
        }
        else{
            try{
            i = Integer.parseInt(args[0]);
            if(i>next.get(game)+1 || i<1){
                    MessageManager.getInstance().sendFMessage(MessageManager.PrefixType.ERROR, "error.between", player, "num-" + next.get(game));
                return true;
            }
            if(i == next.get(game)){
                next.put(game, next.get(game)+1);
            }
            }catch(Exception e){
                MessageManager.getInstance().sendMessage(MessageManager.PrefixType.ERROR, "error.badinput", player);
                return false;
            }
        }
        if(i == -1){
            MessageManager.getInstance().sendMessage(MessageManager.PrefixType.ERROR, "error.notinside", player);
            return true;
        }
        SettingsManager.getInstance().setDMSpawn(game, i, l);
        MessageManager.getInstance().sendFMessage(MessageManager.PrefixType.INFO, "info.dmspawnset", player, "num-" + i, "arena-" + game);
        return true;
    }
    
    @Override
    public String help(CommandSender s) {
        return "/sg setdmspawn next - " + SettingsManager.getInstance().getMessageConfig().getString("messages.help.setdmspawn", "Sets a DM spawn for the arena you are located in");
    }

	@Override
	public String permission() {
		return "sg.admin.setarenaspawns";
	}
}