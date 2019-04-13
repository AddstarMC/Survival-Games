package org.mcsg.survivalgames.util;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created for the AddstarMC Project.
 * Created by narimm on 11/04/2019.
 */

public class KitInventory implements ConfigurationSerializable {
    private final Map<Integer, ItemStack> contents = new HashMap<>();
    private ItemStack head = null;
    private ItemStack chest = null;
    private ItemStack legs = null;
    private ItemStack feet = null;
    private ItemStack offHand = null;
    private ItemStack mainHand = null;

    @SuppressWarnings("unchecked")
    public static KitInventory deserialize(final Map<String, Object> map){
        final KitInventory inv = new KitInventory();
        inv.setHead(creatItemStack(map.get("head")));
        inv.setChest(creatItemStack(map.get("chest")));
        inv.setFeet(creatItemStack(map.get("feet")));
        inv.setLegs(creatItemStack(map.get("legs")));
        inv.setMainHand(creatItemStack(map.get("mainHand")));
        inv.setOffHand(creatItemStack(map.get("offHand")));
        Object contents = map.get("contents");
        if (contents instanceof List) {
            final List<ItemStack> listContents = (List<ItemStack>) contents;
            int i = 0;
            for (final ItemStack item : listContents) {
                if (item != null) {
                    inv.addContent(i, item);
                    i++;
                }
            }
        } else if (contents instanceof Map) {
            final Map<Integer, ItemStack> mapContents = (Map<Integer, ItemStack>) contents;
            inv.getContentsAsMap().putAll(mapContents);
        }
        return inv;
    }
    @SuppressWarnings("unchecked")
    private static ItemStack creatItemStack(final Object object) {
        if (object == null) {
            return null;
        }
        if (object instanceof ItemStack) {
            return (ItemStack) object;
        }
        if (object instanceof Map) {
            final Map map = (Map) object;
            return ItemStack.deserialize(map);
        }
        return null;
    }

    public ItemStack getHead() {
        if (this.head != null) {
            return this.head.clone();
        } else {
            return null;
        }

    }

    public void setHead(final ItemStack head) {
        if (head != null) {
            this.head = head.clone();
        }
    }

    public ItemStack getChest() {
        if (this.chest != null) {
            return this.chest.clone();
        } else {
            return null;
        }
    }

    public void setChest(final ItemStack chest) {
        if (chest != null) {
            this.chest = chest.clone();
        }
    }

    public ItemStack getLegs() {
        if (this.legs != null) {
            return this.legs.clone();
        } else {
            return null;
        }
    }

    public void setLegs(final ItemStack legs) {
        if (legs != null) {
            this.legs = legs.clone();
        }
    }

    public ItemStack getFeet() {
        if (this.feet != null) {
            return this.feet.clone();
        } else {
            return null;
        }
    }

    public void setFeet(final ItemStack feet) {
        if (feet != null) {
            this.feet = feet.clone();
        }
    }

    public ItemStack getOffHand() {
        if (this.offHand != null) {
            return this.offHand.clone();
        } else {
            return null;
        }
    }

    public void setOffHand(final ItemStack offHand) {
        if (offHand != null) {
            this.offHand = offHand.clone();
        }
    }

    public ItemStack getMainHand() {
        if (this.mainHand != null) {
            return this.mainHand.clone();
        } else {
            return null;
        }
    }

    public void setMainHand(final ItemStack mainHand) {
        if (mainHand != null) {
            this.mainHand = mainHand.clone();
        }
    }

    public void addContent(final int pos, final ItemStack i) {
        this.contents.put(pos, i);
    }

    public void removeContent(final int pos) {
        this.contents.remove(pos);

    }

    public Map<Integer, ItemStack> getContentsAsMap() {
        return this.contents;
    }
    public ItemStack[] getContents(){
        final ItemStack[] out = new ItemStack[9];
        for (Map.Entry<Integer, ItemStack> e : contents.entrySet()) {
            out[e.getKey()] = e.getValue();
        }
        return out;
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        final Map<String, Object> out = new HashMap<>();
        out.put("head",this.head);
        out.put("chest",this.chest);
        out.put("feet",this.feet);
        out.put("legs",this.legs);
        out.put("mainHand", this.mainHand);
        out.put("offHand", this.offHand);
        out.put("contents",this.contents);
        return out;
    }

    public PlayerInventory fillInventory(PlayerInventory inv) {
        inv.setBoots(this.feet);
        inv.setLeggings(this.legs);
        inv.setChestplate(this.chest);
        inv.setHelmet(this.head);
        for (Map.Entry<Integer, ItemStack> e : this.contents.entrySet()) {
            inv.setItem(e.getKey(), e.getValue());
        }
        return inv;
    }
}
