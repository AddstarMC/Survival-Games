package org.mcsg.survivalgames;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.mcsg.survivalgames.MessageManager.PrefixType;
import org.mcsg.survivalgames.commands.CreateArena;
import org.mcsg.survivalgames.commands.DelArena;
import org.mcsg.survivalgames.commands.Disable;
import org.mcsg.survivalgames.commands.Enable;
import org.mcsg.survivalgames.commands.Flag;
import org.mcsg.survivalgames.commands.ForceStart;
import org.mcsg.survivalgames.commands.Join;
import org.mcsg.survivalgames.commands.Kit;
import org.mcsg.survivalgames.commands.Leave;
import org.mcsg.survivalgames.commands.LeaveQueue;
import org.mcsg.survivalgames.commands.ListArenas;
import org.mcsg.survivalgames.commands.ListPlayers;
import org.mcsg.survivalgames.commands.Reload;
import org.mcsg.survivalgames.commands.ResetDMSpawns;
import org.mcsg.survivalgames.commands.ResetSpawns;
import org.mcsg.survivalgames.commands.SetDMSpawn;
import org.mcsg.survivalgames.commands.SetLobbySpawn;
import org.mcsg.survivalgames.commands.SetSpawn;
import org.mcsg.survivalgames.commands.Spectate;
import org.mcsg.survivalgames.commands.SubCommand;
import org.mcsg.survivalgames.commands.Teleport;
import org.mcsg.survivalgames.commands.Test;
import org.mcsg.survivalgames.commands.Vote;

public class CommandHandler implements CommandExecutor {
	
	private enum CommandGroup {
		Player,
		Staff,
		Admin
	}
	
	private Plugin plugin;
	private HashMap<String, SubCommand> commands;
	private HashMap<String, CommandGroup> helpinfo;
	private MessageManager msgmgr = MessageManager.getInstance();
	
	public CommandHandler(Plugin plugin) {
		this.plugin = plugin;
		commands = new HashMap<String, SubCommand>();
		helpinfo = new HashMap<String, CommandGroup>();
		loadCommands();
		loadHelpInfo();
	}

	private void loadCommands() {
				
		// player commands
		commands.put("join", new Join());
		commands.put("vote", new Vote());
		commands.put("leave", new Leave());
		commands.put("spectate", new Spectate());
		commands.put("leavequeue", new LeaveQueue());
		commands.put("list", new ListPlayers());
		commands.put("listarenas", new ListArenas());
		
		// staff commands
		commands.put("disable", new Disable());
		commands.put("forcestart", new ForceStart());
		commands.put("enable", new Enable());
		
		// admin commands	
		commands.put("createarena", new CreateArena());
		commands.put("setspawn", new SetSpawn());
		commands.put("setdmspawn", new SetDMSpawn());
		commands.put("setlobbyspawn", new SetLobbySpawn());
		commands.put("resetspawns", new ResetSpawns());
		commands.put("resetdmspawns", new ResetDMSpawns());
		commands.put("delarena", new DelArena());
		commands.put("flag", new Flag());
		commands.put("tp", new Teleport());
		commands.put("reload", new Reload());
		commands.put("kit", new Kit());
		commands.put("test", new Test());
	}

	private void loadHelpInfo() {
		// player commands
		helpinfo.put("join", CommandGroup.Player);
		helpinfo.put("vote", CommandGroup.Player);
		helpinfo.put("leave", CommandGroup.Player);
		helpinfo.put("spectate", CommandGroup.Player);
		helpinfo.put("leavequeue", CommandGroup.Player);
		helpinfo.put("list", CommandGroup.Player);
		helpinfo.put("listarenas", CommandGroup.Player);
		helpinfo.put("kit", CommandGroup.Player);
		
		// staff commands
		helpinfo.put("disable", CommandGroup.Staff);
		helpinfo.put("forcestart", CommandGroup.Staff);
		helpinfo.put("enable", CommandGroup.Staff);
		
		// admin commands	
		helpinfo.put("createarena", CommandGroup.Admin);
		helpinfo.put("setspawn", CommandGroup.Admin);
		helpinfo.put("setdmspawn", CommandGroup.Admin);
		helpinfo.put("setlobbyspawn", CommandGroup.Admin);
		helpinfo.put("resetspawns", CommandGroup.Admin);
		helpinfo.put("resetdmspawns", CommandGroup.Admin);
		helpinfo.put("delarena", CommandGroup.Admin);
		helpinfo.put("flag", CommandGroup.Admin);
		helpinfo.put("tp", CommandGroup.Admin);
		helpinfo.put("reload", CommandGroup.Admin);
		helpinfo.put("test", CommandGroup.Admin);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd1, String commandLabel, String[] args) {
		PluginDescriptionFile pdfFile = plugin.getDescription();

		if (SurvivalGames.config_todate == false) {
			msgmgr.sendMessage(PrefixType.WARNING, "The config file is out of date. Please tell an administrator to reset the config.", sender);
			return true;
		}

		if (SurvivalGames.dbcon == false) {
			msgmgr.sendMessage(PrefixType.WARNING, "Could not connect to server. Plugin disabled.", sender);
			return true;
		}

		if (cmd1.getName().equalsIgnoreCase("survivalgames")) {
			if (args == null || args.length < 1) {
				msgmgr.sendMessage(PrefixType.INFO, "Version " + pdfFile.getVersion(), sender);
				msgmgr.sendMessage(PrefixType.INFO, "Type /sg help <player | staff | admin> for command information", sender);
				return true;
			}
			if (args[0].equalsIgnoreCase("help")) {
				if (args.length == 1) {
					help(sender, CommandGroup.Player);
				}
				else {
					if (args[1].toLowerCase().startsWith("player")) {
						help(sender, CommandGroup.Player);
						return true;
					}
					if (args[1].toLowerCase().startsWith("staff")) {
						help(sender, CommandGroup.Staff);
						return true;
					}
					if (args[1].toLowerCase().startsWith("admin")) {
						help(sender, CommandGroup.Admin);
						return true;
					}
					else {
						msgmgr.sendMessage(PrefixType.INFO, "Type /sg help <player | staff | admin> for command information", sender);
					}
				}
				return true;
			}
			String sub = args[0];
			Vector < String > l = new Vector < String > ();
			l.addAll(Arrays.asList(args));
			l.remove(0);
			args = (String[]) l.toArray(new String[0]);
			if (!commands.containsKey(sub)) {
				msgmgr.sendMessage(PrefixType.WARNING, "Command doesn't exist.", sender);
				msgmgr.sendMessage(PrefixType.INFO, "Type /sg help for command information", sender);
				return true;
			}
			try {
				commands.get(sub).onCommand(sender, args);
			} catch (Exception e) {
				e.printStackTrace();
				msgmgr.sendFMessage(PrefixType.ERROR, "error.command", sender, "command-["+sub+"] "+Arrays.toString(args));
				msgmgr.sendMessage(PrefixType.INFO, "Type /sg help for command information", sender);
			}
			return true;
		}
		return false;
	}

	public void help (CommandSender s, CommandGroup group) {
		if (group == CommandGroup.Player) {
			s.sendMessage(ChatColor.BLUE + "------------ " + msgmgr.pre + ChatColor.DARK_AQUA + " Player Commands" + ChatColor.BLUE + " ------------");
		}
		if (group == CommandGroup.Staff) {
			s.sendMessage(ChatColor.BLUE + "------------ " + msgmgr.pre + ChatColor.DARK_AQUA + " Staff Commands" + ChatColor.BLUE + " ------------");
		}
		if (group == CommandGroup.Admin) {
			s.sendMessage(ChatColor.BLUE + "------------ " + msgmgr.pre + ChatColor.DARK_AQUA + " Admin Commands" + ChatColor.BLUE + " ------------");
		}

		for (String command : commands.keySet()) {
			try{
				if (helpinfo.get(command) == group) {
					msgmgr.sendMessage(PrefixType.INFO, commands.get(command).help(s), s);
				}
			}catch(Exception e){}
		}
	}
}
