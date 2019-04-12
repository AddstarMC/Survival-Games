package org.mcsg.survivalgames.util;

import org.apache.commons.lang.StringUtils;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ItemReader {


    private static final HashMap<String, Enchantment> ENCIDS = new HashMap<>();
    private static final HashMap<String, PotionEffectType> POTION_EFFECTS = new HashMap<>();


    public static void loadIds() {
        ENCIDS.clear();
        POTION_EFFECTS.clear();
        for(final Enchantment e:Enchantment.values()){
            final String name = e.getKey().getKey().toLowerCase().replace("_", "");
            ENCIDS.put(name, e);
        }

        ENCIDS.put("protect", Enchantment.PROTECTION_ENVIRONMENTAL);
        ENCIDS.put("protection", Enchantment.PROTECTION_ENVIRONMENTAL);
        ENCIDS.put("protectionfire", Enchantment.PROTECTION_FIRE);
        ENCIDS.put("sharpness", Enchantment.DAMAGE_ALL);
        ENCIDS.put("dmg", Enchantment.DAMAGE_ALL);
        ENCIDS.put("fire", Enchantment.FIRE_ASPECT);
        for (final Enchantment val : Enchantment.values()) {
            ENCIDS.put(val.getKey().getKey(), val);
        }
        for (final PotionEffectType type : PotionEffectType.values()) {
            final String name = type.getName();
            POTION_EFFECTS.put(StringUtils.lowerCase(name), type);
        }
        SurvivalGames.debug(0, "Available PotionEffects: " + POTION_EFFECTS.keySet());

    }

    public static ItemStack read(final String str) {
        if(str == null){
            return null;
        }
        final String[] split = str.split(",");
        for (int a = 0; a < split.length; a++) {
            split[a] = split[a].trim();
        }

        if (split.length == 0) {
            return null;
        }

        Material mat = null;
        ItemStack i = null;
        int size = 1;

        try {
            mat = Material.matchMaterial(split[0]);
            if (mat == null) {
                SurvivalGames.info(0, "ERROR: Unable to match item \"" + split[0] + "\" to Material");
                return null;
            }
        } catch (final Exception e) {
            SurvivalGames.info(0, "ERROR: Unknown item named \"" + split[0] + '"');
            return null;
        }

        // Grab item qty
        if (split.length >= 2) {
            size = Integer.parseInt(split[1]);
        }
        // Create the item
        i = new ItemStack(mat, size);

        if (split.length >= 3) {
            if (i.getItemMeta() instanceof PotionMeta) {
                decodePotionData(i, split[2]);
            } else {
                decodeEnchants(i, split[2]);
            }
        }
        final ItemMeta im = i.getItemMeta();
        // Set item display name
        if (split.length >= 4) {
            final String name = MessageUtil.replaceColors(split[3]);
            if (im != null) {
                im.setDisplayName(name);
            }
            SurvivalGames.debug(0, "  Name: " + name);
        }

        // Set item lore
        if (split.length == 5) {
            final String[] l = split[4].split("\\|", -1);
            for (int a = 0; a < l.length; a++) {
                SurvivalGames.debug(0, "  Lore " + (a + 1) + ": " + l[a]);
            }
            final List<String> lore = new ArrayList<>(Arrays.asList(l));
            if (im != null) {
                im.setLore(lore);
            }
        }
        i.setItemMeta(im);
        SurvivalGames.debug(0, "Item: " + i);
        return i;
    }

    private static void decodeEnchants(final ItemStack i, final String data) {
        final String[] encs = data.split(" ");
        for (final String enc : encs) {
            if (!enc.isEmpty() && !"null".equalsIgnoreCase(enc)) {
                final String[] e = enc.toLowerCase().split(":");
                final Enchantment enchant = ENCIDS.get(e[0]);
                if (enchant == null) {
                    SurvivalGames.info(0, "  INVALID ENCHANT: " + e[0]);
                } else {
                    final int level = Integer.parseInt(e[1]);
                    i.addUnsafeEnchantment(enchant, level);
                }
            }
        }
        for (final Enchantment e : i.getEnchantments().keySet()) {
            SurvivalGames.debug(0, "  Enchant: " + e.getKey().getKey() + " = " + e.getStartLevel());
        }
    }

    private static void decodePotionData(final ItemStack i, final String data) {
            final PotionMeta pMeta = (PotionMeta) i.getItemMeta();
        final String[] potionTypes = data.split(" ");
        SurvivalGames.debug(0, "Decoding potion data: " + data);
        int num = 0;
        for (final String type : potionTypes) {
            SurvivalGames.debug(0, "Decoding potion effect :  " + type);

            if (!type.isEmpty() && !"null".equalsIgnoreCase(type)) {
                final String[] e = type.toLowerCase().split(":");
                if (num + 1 == potionTypes.length) {
                    if ("color".equals(e[0].toLowerCase())) {
                        if (e.length != 4) {
                            SurvivalGames.info(0, "  Potion Color Data error: must have color:red:green:blue (where the red green and blue are integers 0-255)");
                            continue;
                        }
                        final int red = Integer.parseInt(e[1]);
                        final int green = Integer.parseInt(e[2]);
                        final int blue = Integer.parseInt(e[3]);
                        if (pMeta != null) {
                            pMeta.setColor(Color.fromRGB(red, green, blue));
                        }
                        continue;
                    }
                }
                PotionType pType = null;
                final PotionEffectType potType = POTION_EFFECTS.get(e[0].toLowerCase());
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
                            final PotionData pData = new PotionData(pType, false, false);
                            if (pMeta != null) {
                                pMeta.setBasePotionData(pData);
                            }
                            num = num + 1;
                            continue;
                        }
                    }
                    SurvivalGames.info(0, "  INVALID PotionData: " + e[0]);
                } else {
                    final int duration = Integer.parseInt(e[1]);
                    final int amp = Integer.parseInt(e[2]);
                    final boolean override = Boolean.parseBoolean(e[3]);

                    if (num == 0) {
                        pType = PotionType.getByEffect(potType);
                        if (pType == null) {
                            num = num + 1;
                            continue;
                        }
                        final boolean upgraded = pType.isUpgradeable() & amp > 1;
                        final boolean extended = pType.isExtendable() & duration > 30;
                        final PotionData pData = new PotionData(pType, extended, upgraded);
                        if (pMeta != null) {
                            pMeta.setBasePotionData(pData);
                        }
                    } else {
                        final PotionEffect effect = new PotionEffect(potType, duration * 20, amp);
                        if (pMeta != null) {
                            pMeta.addCustomEffect(effect, override);
                        }
                    }
                }
            }
            num = num + 1;
        }
        i.setItemMeta(pMeta);
        SurvivalGames.debug(0, "Potion data: " + pMeta);
    }


    public static String getFriendlyItemName(final Material m){
        String str = m.toString();
        str = str.replace('_',' ');
        str = str.substring(0, 1).toUpperCase() +
                str.substring(1).toLowerCase();
        return str;
    }


}
