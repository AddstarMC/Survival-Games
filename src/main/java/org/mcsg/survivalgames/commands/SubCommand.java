package org.mcsg.survivalgames.commands;

import org.bukkit.command.CommandSender;

public interface SubCommand {

    public boolean onCommand(CommandSender sender, String[] args);

    public String help(CommandSender p);
    
    public String permission();
    
}