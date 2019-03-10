package org.mcsg.survivalgames.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.mcsg.survivalgames.Game;
import org.mcsg.survivalgames.MessageManager;
import org.mcsg.survivalgames.SurvivalGames;
import org.mcsg.survivalgames.util.Kit;

import java.util.List;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 6/03/2019.
 */
public class ListKits implements SubCommand {
    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        Game game = null;
        if (sender instanceof Player) {
            if (!sender.hasPermission(permission()) && !sender.isOp()) {
                MessageManager.getInstance().sendFMessage(MessageManager.PrefixType.ERROR, "error.nopermission", sender);
                return false;
            }
            game = SurvivalGames.plugin.getGameManager().getGame((Player) sender);
        }
        List<Kit> kits = SurvivalGames.plugin.getGameManager().getKits(game);
        if (game != null) sender.sendMessage("Showing Kits for:" + game.getName());
        else sender.sendMessage("Showing All Kits");
        for (Kit k : kits) {
            sender.sendMessage("---------------");
            sender.sendMessage(k.getName());
            sender.sendMessage(k.getIcon().toString());
            sender.sendMessage("Cost:" + k.getCost());
            sender.sendMessage("............");
            for (ItemStack i : k.getContents()) {
                if (i != null)
                    sender.sendMessage(i.toString());
            }
            sender.sendMessage("---------------");
        }
        return true;
    }

    @Override
    public String help(CommandSender p) {
        return "Lists the kits in the plugin";
    }

    @Override
    public String permission() {
        return "sg.admin.list.kits";
    }
}
