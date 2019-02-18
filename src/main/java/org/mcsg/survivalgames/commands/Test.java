package org.mcsg.survivalgames.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.mcsg.survivalgames.*;
import org.mcsg.survivalgames.MessageManager.PrefixType;
import org.mcsg.survivalgames.stats.StatsManager;
import org.mcsg.survivalgames.util.Kit;

public class Test implements SubCommand {

    @Override
    public boolean onCommand(final CommandSender sender, final String[] args) {
        if (sender instanceof Player) {
            if (!sender.hasPermission(this.permission()) && !sender.isOp()) {
                MessageManager.getInstance().sendFMessage(MessageManager.PrefixType.ERROR, "error.nopermission", sender);
                return true;
            }
        }
        
        if(args.length < 1){
            sender.sendMessage(this.help(sender));
            return true;
        }
    
        final String action = args[0];
        switch (action) {
            case "endfireworks":
                MessageManager.getInstance().sendMessage(PrefixType.INFO, "Launching end of game fireworks at your location", sender);
                if (sender instanceof Player) {
                    final Player p = (Player) sender;
                    LobbyManager.launchEndFireworks(p.getLocation());
                } else {
                    sender.sendMessage("Error: Must be player");
                }
                break;
            case "statslist":
                StatsManager.getInstance().outputStatsDebug(sender);
                break;
            case "showkits":
                final Game g;
                if (args.length > 2) {
                    final String gameID = args[1];
                    g = GameManager.getInstance().getGame(Integer.parseInt(gameID));
                } else {
                    g = null;
                }
                final List<Kit> kits = GameManager.getInstance().getKits(g);
                sender.sendMessage(kits.size() + " kits to show.");
                for (final Kit k : kits) {
                    sender.sendMessage(k.getName());
                    sender.sendMessage("  Items: ");
                    sender.sendMessage("  ------- ");
                    for (final ItemStack i : k.getContents()) {
                        sender.sendMessage(i.toString());
                    }
                    sender.sendMessage("  ------- ");
                }
                break;
        default:
            MessageManager.getInstance().sendMessage(PrefixType.ERROR, ChatColor.RED + "Unknown test action \"" + action + "\"!", sender);
            break;
        }
        return true;
    }

    @Override
    public String help(final CommandSender s) {
        return "/sg test <action> - " + SettingsManager.getInstance().getMessageConfig().getString("messages.help.test", "Performs a test for troubleshooting");
    }
    
    @Override
    public String permission() {
        return "sg.admin.test";
    }
}
