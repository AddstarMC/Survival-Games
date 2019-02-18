package org.mcsg.survivalgames;

import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.mcsg.survivalgames.util.MessageUtil;



public class MessageManager {

	public static MessageManager instance = new MessageManager();
	public String pre = ChatColor.BLUE + "" + ChatColor.BOLD + "[" + ChatColor.GOLD + "" + ChatColor.BOLD + "SG" + ChatColor.BLUE + "" + ChatColor.BOLD + "] " + ChatColor.RESET;
	private final HashMap<PrefixType, String> prefix = new HashMap<>();

	/**
	 * SendMessage
	 * <p>
	 * Loads a Message from messages.yml, converts its colors and replaces vars in the form of {$var} with its correct values,
	 * then sends to the player, adding the correct prefix
	 *
	 * @param type The PrefixType
	 * @param input The message id from the message.yml file
	 * @param player the player to send
	 * @param args the strings to use to format the message
	 */
	public void sendFMessage(final PrefixType type, final String input, final Player player, final String... args) {
        this.sendFMessage(type, input, (CommandSender) player, args);
	}
	
	public static MessageManager getInstance() {
		return instance;
	}
	
	public void setup() {
		final FileConfiguration f = SettingsManager.getInstance().getMessageConfig();
        this.prefix.put(PrefixType.MAIN, MessageUtil.replaceColors(f.getString("prefix.main")));
        this.prefix.put(PrefixType.INFO, MessageUtil.replaceColors(f.getString("prefix.states.info")));
        this.prefix.put(PrefixType.WARNING, MessageUtil.replaceColors(f.getString("prefix.states.warning")));
        this.prefix.put(PrefixType.ERROR, MessageUtil.replaceColors(f.getString("prefix.states.error")));

	}

	/**
	 * SendMessage
	 *
	 * Sends a pre formated message from the plugin to a player, adding correct prefix first
	 *
	 * @param type The PrefixType
	 * @param msg The message
	 * @param sender the player to send
	 */

	public void sendMessage(final PrefixType type, final String msg, final CommandSender sender) {
		sender.sendMessage(this.prefix.get(PrefixType.MAIN)+ " "+ this.prefix.get(type)+ msg );
	}
	
	public void sendFMessage(final PrefixType type, final String input, final CommandSender sender, final String ... args) {
		String msg = SettingsManager.getInstance().getMessageConfig().getString("messages."+input);
		final boolean enabled = SettingsManager.getInstance().getMessageConfig().getBoolean("messages."+input+"_enabled", true);
		if(msg == null){sender.sendMessage(ChatColor.RED+"Failed to load message for messages."+input); return;}
		if(!enabled)return;
		if(args != null && args.length != 0){msg = MessageUtil.replaceVars(msg, args);}
		msg = MessageUtil.replaceColors(msg);
		sender.sendMessage(this.prefix.get(PrefixType.MAIN)+ " " + this.prefix.get(type) + msg);
	}

	public enum PrefixType {

		MAIN, INFO, WARNING, ERROR

	}
	
	public void logMessage(final PrefixType type, final String msg) {
		final Logger logger = Bukkit.getServer().getLogger();
		switch (type) {
		case INFO:  logger.info(this.prefix.get(type)+ msg); break;
		case WARNING: logger.warning(this.prefix.get(type) + msg); break;
		case ERROR: logger.severe(this.prefix.get(type) + msg); break;
		default:
			break;
		}
	}
	
	public String getFMessage(final PrefixType type, final String input, final String ...args ) {
		String msg = SettingsManager.getInstance().getMessageConfig().getString("messages."+input);
		if(msg == null){
			Bukkit.broadcastMessage(ChatColor.RED+"Failed to load message for messages." + input);
			return "";
		}
		if (args != null && args.length != 0) {
			msg = MessageUtil.replaceVars(msg, args);
		}
		msg = this.prefix.get(PrefixType.MAIN) + this.prefix.get(type) + " " + MessageUtil.replaceColors(msg);
		return msg;
	}

	public void broadcastFMessage(final PrefixType type, final String input, final String ...args ) {
		if (SettingsManager.getInstance().getMessageConfig().getBoolean("messages."+input+"_enabled", true)) {
			final String msg = this.getFMessage(type, input, args);
			Bukkit.broadcastMessage(msg);
		}
	}
	
	public void broadcastMessage(final PrefixType type, final String msg, final Player player){
		Bukkit.broadcastMessage(this.prefix.get(PrefixType.MAIN)+ " "+ this.prefix.get(type)+ " "+msg );
	}

	public void sendTitleMessage(final PrefixType type, final CommandSender sender, final String message, final String... args) {
		if (sender instanceof Player) {
			((Player) sender).sendTitle(this.getFMessage(type, message, args), null);
		}
	}

}