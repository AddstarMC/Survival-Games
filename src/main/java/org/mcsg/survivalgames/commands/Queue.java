package org.mcsg.survivalgames.commands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.mcsg.survivalgames.*;
import org.mcsg.survivalgames.MessageManager.PrefixType;
import org.mcsg.survivalgames.logging.QueueManager;
import org.mcsg.survivalgames.logging.SgBlockData;
import org.mcsg.survivalgames.stats.StatsManager;
import org.mcsg.survivalgames.util.Kit;

import java.util.ArrayList;
import java.util.List;

public class Queue implements SubCommand {

    @Override
    public boolean onCommand(final CommandSender sender, final String[] args) {
        if (sender instanceof Player) {
            if (!sender.hasPermission(this.permission()) && !sender.isOp()) {
                MessageManager.getInstance().sendFMessage(PrefixType.ERROR, "error.nopermission", sender);
                return true;
            }
        }
        
        if(args.length < 1){
            sender.sendMessage(this.help(sender));
            return true;
        }
    
        final String action = args[0];
        QueueManager qm = QueueManager.getInstance();
        switch (action) {
            case "stats":
                MessageManager.getInstance().sendMessage(PrefixType.INFO, "Arena block queue stats:", sender);
                for (Game g : GameManager.getInstance().getGames()) {
                    int id = g.getID();
                    int count = 0;
                    ArrayList<SgBlockData> q = qm.getQueue(id);
                    if (q != null)
                        count = q.size();
                    sender.sendMessage(ChatColor.YELLOW + "Arena " + id + ":" + ChatColor.AQUA + " " + count);
                }
                break;
            case "list":
                if (args.length < 2) {
                    MessageManager.getInstance().sendMessage(PrefixType.ERROR, ChatColor.RED + "Must specify an arena ID!", sender);
                    return true;
                }

                // Fetch the queue for the specified arena
                int id = Integer.parseInt(args[1]);
                if (GameManager.getInstance().getGame(id) == null) {
                    MessageManager.getInstance().sendMessage(PrefixType.ERROR, ChatColor.RED + "Invalid arena ID: " + id + "!", sender);
                    return true;
                }

                // Show info about each item in the list
                MessageManager.getInstance().sendMessage(PrefixType.INFO, "Queue contents for arena " + id + ":", sender);
                ArrayList<SgBlockData> q = qm.getQueue(id);
                if ((q == null) || q.size() == 0) {
                    sender.sendMessage(ChatColor.YELLOW + " - Queue is empty");
                    return true;
                }

                for (int x = 0; x < q.size(); x++) {
                    SgBlockData bd = q.get(x);
                    Material newmat = bd.getNewBlockData().getMaterial();
                    Material oldmat = bd.getPrevBlockData().getMaterial();
                    int itemcount = bd.getItems() == null ? 0 : bd.getItems().length;

                    sender.sendMessage(ChatColor.YELLOW + " " + String.format("%03d", x) + ": "
                            + ChatColor.AQUA + newmat + "(" + itemcount + ")"
                            + " / " + ChatColor.RED + oldmat);
                }
                break;
        default:
            MessageManager.getInstance().sendMessage(PrefixType.ERROR, ChatColor.RED + "Unknown queue action \"" + action + "\"!", sender);
            break;
        }
        return true;
    }

    @Override
    public String help(final CommandSender s) {
        return "/sg queue <stats|list> [arena] - " + SettingsManager.getInstance().getMessageConfig().getString("messages.help.test", "Shows logging queue info");
    }
    
    @Override
    public String permission() {
        return "sg.admin.queue";
    }
}
