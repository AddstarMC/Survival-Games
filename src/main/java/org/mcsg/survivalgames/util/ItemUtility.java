package org.mcsg.survivalgames.util;

import com.google.common.collect.Multimap;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.json.simple.JSONObject;
import org.mcsg.survivalgames.SurvivalGames;

import java.util.*;

/**
 * Created for the AddstarMC Project. Created by Narimm on 29/10/2018. This will eventually replaced
 * the ItemReader class and the ChestStorage JSON reader for items so that they can are similar in
 * terms of configuration loading.
 */
public class ItemUtility {

    private static HashMap<String, Enchantment> encids;
    private static HashMap<String, ItemFlag> flagids;
    private static HashMap<String, Attribute> attributeMap;
    private static HashMap<String, AttributeModifier.Operation> attributeOperatorMap;


    private static void loadIds() {
        encids = new HashMap<>();
        flagids = new HashMap<>();
        attributeMap = new HashMap<>();
        attributeOperatorMap = new HashMap<>();
        for (Enchantment e : Enchantment.values()) {
            String name = e.getKey().getKey().toLowerCase().replace("_", "");
            encids.put(name, e);
        }
        //Only Attributes that can affect players.

        attributeMap.put("armor", Attribute.GENERIC_ARMOR);
        attributeMap.put("attack_damage", Attribute.GENERIC_ATTACK_DAMAGE);
        attributeMap.put("attack_speed", Attribute.GENERIC_ATTACK_SPEED);
        attributeMap.put("max_health", Attribute.GENERIC_MAX_HEALTH);
        attributeMap.put("movement_speed", Attribute.GENERIC_MOVEMENT_SPEED);
        attributeMap.put("armor_toughness", Attribute.GENERIC_ARMOR_TOUGHNESS);
        attributeMap.put("luck", Attribute.GENERIC_LUCK);

        attributeOperatorMap.put("add", AttributeModifier.Operation.ADD_NUMBER);
        attributeOperatorMap.put("scalar", AttributeModifier.Operation.ADD_SCALAR);
        attributeOperatorMap.put("multiply", AttributeModifier.Operation.MULTIPLY_SCALAR_1);

        for (ItemFlag flag : ItemFlag.values()) {
            flagids.put(flag.name().toLowerCase(), flag);
        }

        encids.put("protect", Enchantment.PROTECTION_ENVIRONMENTAL);
        encids.put("protection", Enchantment.PROTECTION_ENVIRONMENTAL);

        encids.put("sharpness", Enchantment.DAMAGE_ALL);
        encids.put("dmg", Enchantment.DAMAGE_ALL);
        encids.put("fire", Enchantment.FIRE_ASPECT);
        encids.put("curse", Enchantment.BINDING_CURSE);
        encids.put("riptide", Enchantment.RIPTIDE);
        encids.put("channeling", Enchantment.CHANNELING);
        encids.put("knockback", Enchantment.KNOCKBACK);
        encids.put("punch", Enchantment.ARROW_KNOCKBACK);
        encids.put("fireprotection", Enchantment.PROTECTION_FIRE);
        encids.put("blastprotection", Enchantment.PROTECTION_EXPLOSIONS);
        encids.put("projectileprotection", Enchantment.PROTECTION_PROJECTILE);
        encids.put("featherfall", Enchantment.PROTECTION_FALL);
        encids.put("frostwalker", Enchantment.FROST_WALKER);
        encids.put("smiting", Enchantment.DAMAGE_UNDEAD);
        encids.put("thorns", Enchantment.THORNS);
        encids.put("infiniteammo", Enchantment.ARROW_INFINITE);
        encids.put("loyalty", Enchantment.LOYALTY);
        encids.put("spiderdamage", Enchantment.DAMAGE_ARTHROPODS);
        encids.put("sweeping", Enchantment.SWEEPING_EDGE);
        encids.put("oxygen", Enchantment.OXYGEN);
    }

    public static ItemStack fromString(String input) {
        if (encids == null) {
            loadIds();
        }
        String split[] = input.split(",");
        for (int a = 0; a < split.length; a++) {
            split[a] = split[a].trim();
        }

        if (split.length == 0)
            return null;
        Material mat = null;
        ItemStack item = null;
        int size = 1;
        try {
            mat = Material.matchMaterial(split[0]);
        } catch (Exception e) {
            SurvivalGames.log(0, "ERROR: Unknown item named \"" + split[0] + "\"");
            return null;
        }

        // Grab item qty
        if (split.length >= 2)
            try {
                size = Integer.parseInt(split[1]);
            } catch (NumberFormatException e) {
                SurvivalGames.log(0, "Cannot be converted to an integer: " + split[1] + "| 1st Input: FROM " + input);
            }


        // Create the item
        item = new ItemStack(mat, size);
        ItemMeta im = item.getItemMeta();
        if (split.length >= 3) {
            if (im instanceof Damageable) {
                try {
                    ((Damageable) im).setDamage(Integer.parseInt(split[2]));
                } catch (NumberFormatException e) {
                    SurvivalGames.log(0, "Cannot be converted to an integer: " + split[2] + "| 2nd Input: FROM " + input);
                }
            }
        }
        SurvivalGames.log(0, "Item: " + item);
        if (split.length >= 4) {
            String encs[] = split[3].split(" ");
            for (String enc : encs) {
                if ((!enc.isEmpty()) && (!enc.equalsIgnoreCase("null"))) {
                    String e[] = enc.toLowerCase().split(":");
                    item.addUnsafeEnchantment(encids.get(e[0]), Integer.parseInt(e[1]));
                }
            }
        }
        item.setItemMeta(im);
        for (Enchantment e : item.getEnchantments().keySet()) {
            SurvivalGames.log(0, "  Enchant: " + e.getKey().getKey() + " = " + e.getStartLevel());
        }
        // Set item display name
        if (split.length >= 5) {
            String name = MessageUtil.replaceColors(split[4]);
            im.setDisplayName(name);
            SurvivalGames.log(0, "  Name: " + name);
        }

        // Set item lore
        if (split.length >= 6) {
            String[] l = split[5].split("\\|", -1);
            for (int a = 0; a < l.length; a++) {
                SurvivalGames.log(0, "  Lore " + (a + 1) + ": " + l[a]);
            }
            List<String> lore = new ArrayList<>(Arrays.asList(l));
            im.setLore(lore);
        }

        if (split.length >= 7) {
            String[] flags = split[6].split(" ");
            for (String f : flags) {
                if (flagids.containsKey(f.toLowerCase()))
                    item.getItemMeta().addItemFlags(flagids.get(f.toLowerCase()));
            }
        }

        if (split.length == 8) {
            Multimap<Attribute, AttributeModifier> out = item.getItemMeta().getAttributeModifiers();
            String[] attributes = split[7].split(" ");
            for (String a : attributes) {
                String[] modifierString = a.split(":", 1);
                if (attributeMap.containsKey(modifierString[0].toLowerCase())) {
                    Attribute attribute = attributeMap.get(modifierString[0].toLowerCase());
                    String[] mod = modifierString[1].split("\\|");
                    if (mod.length < 3) {
                        continue;
                    }
                    EquipmentSlot slot = null;
                    String name = "unknown";
                    Double amount = 1D;
                    AttributeModifier.Operation operation = AttributeModifier.Operation.ADD_NUMBER;
                    if (mod.length >= 3) {
                        name = mod[0];
                        amount = Double.parseDouble(mod[1]);
                        if (attributeOperatorMap.containsKey(mod[2]))
                            operation = attributeOperatorMap.get(mod[2]);
                    }
                    if (mod.length == 4) {
                        try {
                            slot = EquipmentSlot.valueOf(mod[3].toUpperCase());
                        } catch (IllegalArgumentException e) {
                            SurvivalGames.log(0, "No EquipmentSlot matches: " + mod[3]);
                        }
                    }

                    AttributeModifier modifier = new AttributeModifier(UUID.randomUUID(), name, amount, operation, slot);
                    out.put(attribute, modifier);
                }
            }
            item.getItemMeta().setAttributeModifiers(out);
        }
        return item;
    }
    
    public static ItemStack fromJson(JSONObject object) {
        ItemStack result = null;
        return result;
    }

    public static JSONObject toJson(ItemStack item) {
        return null;
    }

    public static String getFriendlyItemName(Material m) {
        String str = m.toString();
        str = str.replace('_', ' ');
        str = str.substring(0, 1).toUpperCase() +
                str.substring(1).toLowerCase();
        return str;
    }
}
