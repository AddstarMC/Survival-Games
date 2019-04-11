package org.mcsg.survivalgames.util;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created for the AddstarMC Project.
 * Created by narimm on 11/04/2019.
 */

public class KitInventory implements ConfigurationSerializable {
    private ItemStack[] contents = new ItemStack[36];
    private ItemStack head = null;
    private ItemStack chest = null;
    private ItemStack legs = null;
    private ItemStack feet = null;
    private ItemStack offHand = null;
    private ItemStack mainHand = null;

    public static KitInventory deserialize(Map<String, Object> map){
        KitInventory inv = new KitInventory();
        inv.setHead(creatItemStack(map.get("head")));
        inv.setChest(creatItemStack(map.get("chest")));
        inv.setFeet(creatItemStack(map.get("feet")));
        inv.setLegs(creatItemStack(map.get("legs")));
        inv.setMainHand(creatItemStack(map.get("mainHand")));
        inv.setOffHand(creatItemStack(map.get("offHand")));
        List<ItemStack> contents = (List<ItemStack>) map.get("contents");
        int i = 0;
        for (ItemStack item : contents) {
            if (item != null) {
                inv.addContent(i, item);
                i++;
            }
        }
        return inv;
    }

    public static ItemStack creatItemStack(Object object) {
        if (object == null) return null;
        if (object instanceof ItemStack)
            return (ItemStack) object;
        if (object instanceof Map) {
            Map map = (Map) object;
            return ItemStack.deserialize(map);
        }
        return null;
    }

    public ItemStack getHead() {
        if (head != null) {
            return this.head.clone();
        } else return null;

    }

    public void setHead(final ItemStack head) {
        if (head != null)
            this.head = head.clone();
    }

    public ItemStack getChest() {
        if (chest != null) {
            return this.chest.clone();
        } else return null;
    }

    public void setChest(final ItemStack chest) {
        if (chest != null)
            this.chest = chest.clone();
    }

    public ItemStack getLegs() {
        if (legs != null) {
            return this.legs.clone();
        } else return null;
    }

    public void setLegs(final ItemStack legs) {
        if (legs != null)
            this.legs = legs.clone();
    }

    public ItemStack getFeet() {
        if (feet != null) {
            return this.feet.clone();
        } else return null;
    }

    public void setFeet(final ItemStack feet) {
        if (feet != null)
            this.feet = feet.clone();
    }

    public ItemStack getOffHand() {
        if (offHand != null) {
            return this.offHand.clone();
        } else return null;
    }

    public void setOffHand(final ItemStack offHand) {
        if (offHand != null)
            this.offHand = offHand.clone();
    }

    public ItemStack getMainHand() {
        if (mainHand != null) {
            return this.mainHand.clone();
        } else return null;
    }

    public void setMainHand(final ItemStack mainHand) {
        if (mainHand != null)
            this.mainHand = mainHand.clone();
    }

    public void addContent(int pos, ItemStack i) {
        contents[pos] = i.clone();
    }

    public ItemStack removeContent(int pos) {
        ItemStack i = contents[pos].clone();
        contents[pos] = null;
        return i;
    }

    public ItemStack[] getContents(){
        return contents;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> out = new HashMap<>();
        out.put("head",head);
        out.put("chest",chest);
        out.put("feet",feet);
        out.put("legs",legs);
        out.put("mainHand", mainHand);
        out.put("offHand", offHand);
        out.put("contents",contents);
        return out;
    }
}
