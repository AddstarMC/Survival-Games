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
import org.mcsg.survivalgames.commands.*;

public class CommandHandler implements CommandExecutor {
	
	private enum CommandGroup {
		Player,
		Staff,
		Admin
	}
	
	private final Plugin plugin;
	private final HashMap<String, SubCommand> commands;
	private final HashMap<String, CommandGroup> helpinfo;
	private final MessageManager msgmgr = MessageManager.getInstance();
	
	public CommandHandler(final Plugin plugin) {
		this.plugin = plugin;
        this.commands = new HashMap<>();
        this.helpinfo = new HashMap<>();
        this.loadCommands();
        this.loadHelpInfo();
	}

	private void loadCommands() {
				
		// player commands
        this.commands.put("join", new Join());
        this.commands.put("vote", new Vote());
        this.commands.put("leave", new Leave());
        this.commands.put("spectate", new Spectate());
        this.commands.put("leavequeue", new LeaveQueue());
        this.commands.put("list", new ListPlayers());
        this.commands.put("listarenas", new ListArenas());
		
		// staff commands
        this.commands.put("disable", new Disable());
        this.commands.put("forcestart", new ForceStart());
        this.commands.put("enable", new Enable());
		
		// admin commands	
        this.commands.put("createarena", new CreateArena());
        this.commands.put("createkit", new CreateKit());
        this.commands.put("setspawn", new SetSpawn());
        this.commands.put("setdmspawn", new SetDMSpawn());
        this.commands.put("setlobbyspawn", new SetLobbySpawn());
        this.commands.put("resetspawns", new ResetSpawns());
        this.commands.put("resetdmspawns", new ResetDMSpawns());
        this.commands.put("delarena", new DelArena());
        this.commands.put("flag", new Flag());
        this.commands.put("tp", new Teleport());
        this.commands.put("reload", new Reload());
        this.commands.put("kit", new Kit());
        this.commands.put("test", new Test());
        this.commands.put("listkit", new ListKits());
		this.commands.put("queue", new Queue());

	}

	private void loadHelpInfo() {
		// player commands
        this.helpinfo.put("join", CommandGroup.Player);
        this.helpinfo.put("vote", CommandGroup.Player);
        this.helpinfo.put("leave", CommandGroup.Player);
        this.helpinfo.put("spectate", CommandGroup.Player);
        this.helpinfo.put("leavequeue", CommandGroup.Player);
        this.helpinfo.put("list", CommandGroup.Player);
        this.helpinfo.put("listarenas", CommandGroup.Player);
        this.helpinfo.put("kit", CommandGroup.Player);
		
		// staff commands
        this.helpinfo.put("disable", CommandGroup.Staff);
        this.helpinfo.put("forcestart", CommandGroup.Staff);
        this.helpinfo.put("enable", CommandGroup.Staff);
		
		// admin commands	
        this.helpinfo.put("createarena", CommandGroup.Admin);
        this.helpinfo.put("createkit", CommandGroup.Admin);
        this.helpinfo.put("setspawn", CommandGroup.Admin);
        this.helpinfo.put("setdmspawn", CommandGroup.Admin);
        this.helpinfo.put("setlobbyspawn", CommandGroup.Admin);
        this.helpinfo.put("resetspawns", CommandGroup.Admin);
        this.helpinfo.put("resetdmspawns", CommandGroup.Admin);
        this.helpinfo.put("delarena", CommandGroup.Admin);
        this.helpinfo.put("flag", CommandGroup.Admin);
        this.helpinfo.put("tp", CommandGroup.Admin);
        this.helpinfo.put("reload", CommandGroup.Admin);
        this.helpinfo.put("test", CommandGroup.Admin);
        this.helpinfo.put("listkit", CommandGroup.Admin);
		this.helpinfo.put("queue", CommandGroup.Admin);

	}

	@Override
	public boolean onCommand(final CommandSender sender, final Command cmd1, final String commandLabel, String[] args) {
		final PluginDescriptionFile pdfFile = this.plugin.getDescription();

		if (!SurvivalGames.config_todate) {
            this.msgmgr.sendMessage(PrefixType.WARNING, "The config file is out of date. Please tell an administrator to reset the config.", sender);
			return true;
		}

		if (!SurvivalGames.dbcon) {
            this.msgmgr.sendMessage(PrefixType.WARNING, "Could not connect to server. Plugin disabled.", sender);
			return true;
		}

		if (cmd1.getName().equalsIgnoreCase("survivalgames")) {
			if (args == null || args.length < 1) {
                this.msgmgr.sendMessage(PrefixType.INFO, "Version " + pdfFile.getVersion(), sender);
                this.msgmgr.sendMessage(PrefixType.INFO, "Type /sg help <player | staff | admin> for command information", sender);
				return true;
			}
			if (args[0].equalsIgnoreCase("help")) {
				if (args.length == 1) {
                    this.help(sender, CommandGroup.Player);
				}
				else {
					if (args[1].toLowerCase().startsWith("player")) {
                        this.help(sender, CommandGroup.Player);
						return true;
					}
					if (args[1].toLowerCase().startsWith("staff")) {
                        this.help(sender, CommandGroup.Staff);
						return true;
					}
					if (args[1].toLowerCase().startsWith("admin")) {
                        this.help(sender, CommandGroup.Admin);
						return true;
					}
					else {
                        this.msgmgr.sendMessage(PrefixType.INFO, "Type /sg help <player | staff | admin> for command information", sender);
					}
				}
				return true;
			}
			final String sub = args[0];
			final Vector<String> l = new Vector<>(Arrays.asList(args));
			l.remove(0);
			args = l.toArray(new String[0]);
			if (!this.commands.containsKey(sub)) {
                this.msgmgr.sendMessage(PrefixType.WARNING, "Command doesn't exist.", sender);
                this.msgmgr.sendMessage(PrefixType.INFO, "Type /sg help for command information", sender);
				return true;
			}
			try {
                this.commands.get(sub).onCommand(sender, args);
			} catch (final Exception e) {
				e.printStackTrace();
                this.msgmgr.sendFMessage(PrefixType.ERROR, "error.command", sender, "command-["+sub+"] "+Arrays.toString(args));
                this.msgmgr.sendMessage(PrefixType.INFO, "Type /sg help for command information", sender);
			}
			return true;
		}
		return false;
	}

	public void help (final CommandSender s, final CommandGroup group) {
		if (group == CommandGroup.Player) {
			s.sendMessage(ChatColor.BLUE + "------------ " + this.msgmgr.pre + ChatColor.DARK_AQUA + " Player Commands" + ChatColor.BLUE + " ------------");
		}
		if (group == CommandGroup.Staff) {
			s.sendMessage(ChatColor.BLUE + "------------ " + this.msgmgr.pre + ChatColor.DARK_AQUA + " Staff Commands" + ChatColor.BLUE + " ------------");
		}
		if (group == CommandGroup.Admin) {
			s.sendMessage(ChatColor.BLUE + "------------ " + this.msgmgr.pre + ChatColor.DARK_AQUA + " Admin Commands" + ChatColor.BLUE + " ------------");
		}

		for (final String command : this.commands.keySet()) {
			try{
				if (this.helpinfo.get(command) == group) {
                    this.msgmgr.sendMessage(PrefixType.INFO, this.commands.get(command).help(s), s);
				}
			} catch (final Exception ignored) {
			}
		}
	}
}
