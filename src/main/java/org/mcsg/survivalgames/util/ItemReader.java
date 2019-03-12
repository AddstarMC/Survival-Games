package org.mcsg.survivalgames.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Color;
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


    private final static HashMap<String, Enchantment> encids = new HashMap<>();
    private final static HashMap<String, PotionEffectType> potionEffects = new HashMap<>();


    public static void loadIds() {
        encids.clear();
        potionEffects.clear();
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
		SurvivalGames.debug(0, "Available PotionEffects: " + potionEffects.keySet().toString());

	}

	public static ItemStack read(String str) {

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

		if (split.length >= 3) {
			if (i.getItemMeta() instanceof PotionMeta)
				decodePotionData(i, split[2]);
			else
				decodeEnchants(i, split[2]);
		}
		ItemMeta im = i.getItemMeta();
		// Set item display name
		if (split.length >= 4) {
			String name = MessageUtil.replaceColors(split[3]);
			im.setDisplayName(name);
			SurvivalGames.debug(0, "  Name: " + name);
		}

		// Set item lore
		if (split.length == 5) {
			String[] l = split[4].split("\\|", -1);
			for (int a = 0; a < l.length; a++) {
				SurvivalGames.debug(0, "  Lore " + (a + 1) + ": " + l[a]);
			}
			List<String> lore = new ArrayList<>(Arrays.asList(l));
			im.setLore(lore);
		}
		i.setItemMeta(im);
		SurvivalGames.debug(0, "Item: " + i);
		return i;
	}

	private static void decodeEnchants(ItemStack i, String data) {
		String[] encs = data.split(" ");
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
		for (Enchantment e : i.getEnchantments().keySet()) {
			SurvivalGames.debug(0, "  Enchant: " + e.getKey().getKey() + " = " + e.getStartLevel());
		}
	}

	private static void decodePotionData(ItemStack i, String data) {
			PotionMeta pMeta = (PotionMeta) i.getItemMeta();
		String[] potionTypes = data.split(" ");
		SurvivalGames.debug(0, "Decoding potion data: " + data);
		int num = 0;
		for (String type : potionTypes) {
			SurvivalGames.debug(0, "Decoding potion effect :  " + type);

			if ((!type.isEmpty()) && (!type.equalsIgnoreCase("null"))) {
				String[] e = type.toLowerCase().split(":");
				if (num + 1 == potionTypes.length) {
					if (e[0].toLowerCase().equals("color")) {
						if (e.length != 4) {
							SurvivalGames.info(0, "  Potion Color Data error: must have color:red:green:blue (where the red green and blue are integers 0-255)");
							continue;
						}
						int red = Integer.parseInt(e[1]);
						int green = Integer.parseInt(e[2]);
						int blue = Integer.parseInt(e[3]);
						pMeta.setColor(Color.fromRGB(red, green, blue));
						continue;
					}
				}
				PotionType pType = null;
				PotionEffectType potType = potionEffects.get(e[0].toLowerCase());
				if (potType == null) {
					if (num == 0) {
						switch (e[0].toLowerCase()) {
							case "mundane":
								pType = PotionType.MUNDANE;
								break;
							case "thick":
								pType = PotionType.THICK;
								break;
							case "water":
								pType = PotionType.WATER;
								break;
							case "awkward":
								pType = PotionType.AWKWARD;
								break;
						}
						if (pType != null) {
							PotionData pData = new PotionData(pType, false, false);
							pMeta.setBasePotionData(pData);
							num = num + 1;
							continue;
						}
					}
					SurvivalGames.info(0, "  INVALID PotionData: " + e[0]);
				} else {
					int duration = Integer.parseInt(e[1]);
					int amp = Integer.parseInt(e[2]);
					boolean override = Boolean.parseBoolean(e[3]);

					if (num == 0) {
						pType = PotionType.getByEffect(potType);
						if (pType == null) {
							num = num + 1;
							continue;
						}
						boolean upgraded = pType.isUpgradeable() & amp > 1;
						boolean extended = pType.isExtendable() & duration > 30;
						PotionData pData = new PotionData(pType, extended, upgraded);
						pMeta.setBasePotionData(pData);
					} else {
						PotionEffect effect = new PotionEffect(potType, duration * 20, amp);
						pMeta.addCustomEffect(effect, override);
					}
				}
			}
			num = num + 1;
		}
		i.setItemMeta(pMeta);
		SurvivalGames.debug(0, "Potion data: " + pMeta.toString());
	}


	public static String getFriendlyItemName(Material m){
		String str = m.toString();
		str = str.replace('_',' ');
		str = str.substring(0, 1).toUpperCase() +
				str.substring(1).toLowerCase();
		return str;
	}
	
	
}
