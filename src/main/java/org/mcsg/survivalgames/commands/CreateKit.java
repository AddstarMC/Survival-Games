package org.mcsg.survivalgames.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mcsg.survivalgames.*;
import org.mcsg.survivalgames.util.Kit;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 11/04/2019.
 */
public class CreateKit implements SubCommand {
    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            MessageManager.getInstance().sendFMessage(MessageManager.PrefixType.ERROR, "error.notingame", sender);
            return true;
        }
        Player player = (Player) sender;
        if (!player.hasPermission(permission()) && !player.isOp()) {
            MessageManager.getInstance().sendFMessage(MessageManager.PrefixType.ERROR, "error.nopermission", player);
            return true;
        }
        if (args.length != 1) {
            MessageManager.getInstance().sendFMessage(MessageManager.PrefixType.ERROR, "error.notspecified", player, "input-Kit");
            return true;
        }
        String name = args[0];
        GameManager.getInstance().createKit(name, player.getInventory());
        for (Kit k : GameManager.getInstance().getKits(player)) {
            ListKits.listKit(sender, k);
        }
        return true;
    }

    @Override
    public String help(CommandSender p) {
        return "/sg createKit - " + SettingsManager.getInstance().getMessageConfig().getString("messages.help.createKit", "Create a kit");
    }

    @Override
    public String permission() {
        return "sg.admin.createkit";
    }
}
