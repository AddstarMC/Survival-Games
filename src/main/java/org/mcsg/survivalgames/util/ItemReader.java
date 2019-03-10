package org.mcsg.survivalgames.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.mcsg.survivalgames.SurvivalGames;

public class ItemReader {

	
	private static HashMap<String, Enchantment>encids;
	private static HashMap<String, PotionEffectType> potionEffects;



	private static void loadIds(){
        encids = new HashMap<>();
		potionEffects = new HashMap<>();
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
		for (Enchantment val : Enchantment.values()) {
			encids.put(val.getKey().getKey(), val);
		}
		for (PotionEffectType type : PotionEffectType.values()) {
			potionEffects.put(type.getName().toLowerCase(), type);
		}
	}

	public static ItemStack read(String str) {
		if (encids == null) {
			loadIds();
		}
		String[] split = str.split(",");
		for (int a = 0; a < split.length; a++) {
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
				SurvivalGames.info(0, "ERROR: Unable to match item \"" + split[0] + "\" to Material");
				return null;
			}
		} catch (Exception e) {
			SurvivalGames.info(0, "ERROR: Unknown item named \"" + split[0] + "\"");
			return null;
		}

		// Grab item qty
		if (split.length >= 2)
			size = Integer.parseInt(split[1]);

		// Create the item
		i = new ItemStack(mat, size);
		ItemMeta im = i.getItemMeta();
		SurvivalGames.info(0, "Item: " + i);

		// Set item display name
		if (split.length >= 4) {
			String name = MessageUtil.replaceColors(split[3]);
			im.setDisplayName(name);
			SurvivalGames.info(0, "  Name: " + name);
		}

		// Set item lore
		if (split.length == 5) {
			String[] l = split[4].split("\\|", -1);
			for (int a = 0; a < l.length; a++) {
				SurvivalGames.info(0, "  Lore " + (a + 1) + ": " + l[a]);
			}
			List<String> lore = new ArrayList<>(Arrays.asList(l));
			im.setLore(lore);
		}
		i.setItemMeta(im);
		//Check its not a potion
		decodeEchantsPotionData(i, split);
		return i;
	}

	private static void decodeEchantsPotionData(ItemStack i, String[] split) {
		if (i.getItemMeta() instanceof PotionMeta) {
			PotionMeta pMeta = (PotionMeta) i.getItemMeta();
			if (split.length >= 3) {
				String[] potionTypes = split[2].split(" ");
				int num = 0;
				for (String type : potionTypes) {
					if ((!type.isEmpty()) && (!type.equalsIgnoreCase("null"))) {
						String[] e = type.toLowerCase().split(":");
						PotionEffectType potType = potionEffects.get(e[0].toLowerCase());
						if (potType == null) {
							SurvivalGames.info(0, "  INVALID PotionData: " + e[0]);
						} else {
							int duration = Integer.parseInt(e[1]);
							int amp = Integer.parseInt(e[2]);
							if (num == 0) {
								PotionType pType = PotionType.getByEffect(potType);
								boolean upgraded = pType.isUpgradeable() & amp > 1;
								boolean extended = pType.isExtendable() & duration > 30;
								PotionData data = new PotionData(pType, extended, upgraded);
								pMeta.setBasePotionData(data);
							} else {
								PotionEffect effect = new PotionEffect(potType, duration, amp);
								pMeta.addCustomEffect(effect, false);
							}
						}
					}
					num++;
				}
				SurvivalGames.debug(0, "Potion data: " + pMeta.toString());
			} else {
				// Set enchantments
				if (split.length >= 3) {
					String[] encs = split[2].split(" ");
					for (String enc : encs) {
						if ((!enc.isEmpty()) && (!enc.equalsIgnoreCase("null"))) {
							String[] e = enc.toLowerCase().split(":");
							Enchantment enchant = encids.get(e[0]);
							if (enchant == null) {
								SurvivalGames.info(0, "  INVALID ENCHANT: " + e[0]);
							} else {
								int level = Integer.parseInt(e[1]);
								i.addUnsafeEnchantment(enchant, level);
							}
						}
					}
				}
				for (Enchantment e : i.getEnchantments().keySet()) {
					SurvivalGames.info(0, "  Enchant: " + e.getKey().getKey() + " = " + e.getStartLevel());
				}
			}
		}
	}

	public static String getFriendlyItemName(Material m){
		String str = m.toString();
		str = str.replace('_',' ');
		str = str.substring(0, 1).toUpperCase() +
				str.substring(1).toLowerCase();
		return str;
	}
	
	
}
