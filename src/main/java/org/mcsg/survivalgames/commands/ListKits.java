package org.mcsg.survivalgames.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.mcsg.survivalgames.Game;
import org.mcsg.survivalgames.MessageManager;
import org.mcsg.survivalgames.SurvivalGames;
import org.mcsg.survivalgames.util.Kit;
import org.mcsg.survivalgames.util.KitInventory;

import java.util.List;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 6/03/2019.
 */
public class ListKits implements SubCommand {
    public static void listKit(CommandSender sender, Kit k) {
        sender.sendMessage("---------------");
        sender.sendMessage(k.getName());
        sender.sendMessage("Icon: " + k.getIcon());
        sender.sendMessage("Cost:" + k.getCost());
        sender.sendMessage("............");
        KitInventory kInv = k.getKitInventory();
        sender.sendMessage("Equipment");
        sender.sendMessage("Head: " + kInv.getHead());
        sender.sendMessage("Chest: " + kInv.getChest());
        sender.sendMessage("Legs: " + kInv.getLegs());
        sender.sendMessage("Boots: " + kInv.getFeet());
        sender.sendMessage("Main Hand: " + kInv.getMainHand());
        sender.sendMessage("Off Hand: " + kInv.getOffHand());
        sender.sendMessage("Other Gear: ");
        for (ItemStack i : kInv.getContents()) {
            if (i != null)
                sender.sendMessage(i.toString());
        }
        sender.sendMessage("---------------");
    }

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
            listKit(sender, k);
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
