package org.mcsg.survivalgames.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.mcsg.survivalgames.SurvivalGames;

public class MessageUtil {

	private static HashMap<String, String>varcache = new HashMap<String, String>();


	public static String replaceColors(String s){
		if(s == null){
			return null;
		}
		return ChatColor.translateAlternateColorCodes('&', s);
	}

	public static String replaceVars(String msg, HashMap<String, String>vars){
		boolean error = false;
		for(String s:vars.keySet()){
			try{
				msg.replace("{$"+s+"}", vars.get(s));
			}catch(Exception e){
				SurvivalGames.$(0, Level.WARNING, "Failed to replace string vars. Error on "+s);
				error = true;
			}
		}
		if(error){
			SurvivalGames.$(0, Level.SEVERE, "Error replacing vars in message: "+msg);
			SurvivalGames.$(0, Level.SEVERE, "Vars: "+vars.toString());
			SurvivalGames.$(0, Level.SEVERE, "Vars Cache: "+varcache.toString());
		}
		return msg;
	}

	public static String replaceVars(String msg, String[] vars){
		for(String str: vars){
			String[] s = str.split("-");
			varcache.put(s[0], s[1]);
		}
		boolean error = false;
		for(String str: varcache.keySet()){
			try{
				msg = msg.replace("{$"+str+"}", varcache.get(str));
			}catch(Exception e){
				SurvivalGames.$(0, Level.WARNING,"Failed to replace string vars. Error on "+str);
				error = true;
			}
		}
		if(error){
			SurvivalGames.$(0, Level.SEVERE, "Error replacing vars in message: "+msg);
			SurvivalGames.$(0, Level.SEVERE, "Vars: "+Arrays.toString(vars));
			SurvivalGames.$(0, Level.SEVERE, "Vars Cache: "+varcache.toString());
		}

		return msg;
	}
}