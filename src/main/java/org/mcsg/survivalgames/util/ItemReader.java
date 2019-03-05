package org.mcsg.survivalgames.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.mcsg.survivalgames.SurvivalGames;

public class ItemReader {

	
	private static HashMap<String, Enchantment>encids;
	

	
	private static void loadIds(){
        encids = new HashMap<>();
		
		for(Enchantment e:Enchantment.values()){
			String name = e.getKey().getKey().toLowerCase().replace("_", "");
			encids.put(name, e);
		}

        encids.put("protect", Enchantment.PROTECTION_ENVIRONMENTAL);
        encids.put("protection", Enchantment.PROTECTION_ENVIRONMENTAL);
		encids.put("protectionfire", Enchantment.PROTECTION_FIRE);
		encids.put("sharpness", Enchantment.DAMAGE_ALL);
		encids.put("dmg", Enchantment.DAMAGE_ALL);
		encids.put("fire", Enchantment.FIRE_ASPECT);
	}
	
	public static ItemStack read(String str){
		if(encids == null){
			loadIds();
		}
		String[] split = str.split(",");
		for(int a = 0; a < split.length; a++){
			split[a] = split[a].trim();
		}

		if (split.length == 0)
			return null;

		Material mat = null;
		ItemStack i = null;
		int size = 1;

		try {
			mat = Material.matchMaterial(split[0]);
			if (mat == null) {
				SurvivalGames.$(0, "ERROR: Unable to match item \"" + split[0] + "\" to Material");
				return null;
			}
		} catch(Exception e) {
			SurvivalGames.$(0, "ERROR: Unknown item named \"" + split[0] + "\"");
			return null;
		}

		// Grab item qty
		if (split.length >= 2)
			size = Integer.parseInt(split[1]);

		// Create the item
		i = new ItemStack(mat, size);
		ItemMeta im = i.getItemMeta();
		SurvivalGames.$(0, "Item: " + i);

		// Set item display name
		if (split.length >= 4) {
			String name = MessageUtil.replaceColors(split[3]);
			im.setDisplayName(name);
			SurvivalGames.$(0, "  Name: " + name);
		}

		// Set item lore
		if (split.length == 5) {
			String[] l = split[4].split("\\|", -1);
			for(int a = 0; a < l.length; a++){
				SurvivalGames.$(0, "  Lore "+(a+1)+": " + l[a]);
			}
			List<String> lore = new ArrayList<>(Arrays.asList(l));
			im.setLore(lore);
		}
		i.setItemMeta(im);

		// Set enchantments
		if (split.length >= 3) {
			String[] encs = split[2].split(" ");
			for(String enc: encs){
				if ((!enc.isEmpty()) && (!enc.equalsIgnoreCase("null"))) {
					String[] e = enc.toLowerCase().split(":");
					Enchantment enchant = encids.get(e[0]);
					if (enchant == null) {
						SurvivalGames.$(0, "  INVALID ENCHANT: " + e[0]);
					} else {
						int level = Integer.parseInt(e[1]);
						i.addUnsafeEnchantment(enchant, level);
					}
				}
			}
		}

		for (Enchantment e : i.getEnchantments().keySet()) {
			SurvivalGames.$(0, "  Enchant: " + e.getKey().getKey() + " = " + e.getStartLevel());
		}
		return i;
	}
	
	public static String getFriendlyItemName(Material m){
		String str = m.toString();
		str = str.replace('_',' ');
		str = str.substring(0, 1).toUpperCase() +
				str.substring(1).toLowerCase();
		return str;
	}
	
	
}
