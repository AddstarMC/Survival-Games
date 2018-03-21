package org.mcsg.survivalgames.commands;

import org.bukkit.command.CommandSender;

public interface SubCommand {

    boolean onCommand(CommandSender sender, String[] args);

    String help(CommandSender p);

    String permission();
    
}