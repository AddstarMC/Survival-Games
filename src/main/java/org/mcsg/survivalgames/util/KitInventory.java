package org.mcsg.survivalgames.util;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
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
    private final List<ItemStack> contents = new ArrayList<>(36);
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
        final List<ItemStack> contents = (List<ItemStack>) map.get("contents");
        int i = 0;
        for (final ItemStack item : contents) {
            if (item != null) {
                inv.addContent(i, item);
                i++;
            }
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
        if(this.contents.size() < pos){
            while(this.contents.size() < pos){
                this.contents.add(null);
            }
        }
        this.contents.add(pos,i);
    }

    public void removeContent(final int pos) {
        this.contents.add(pos,null);

    }
    public List<ItemStack> getContentsAsList(){
        return this.contents;
    }
    public ItemStack[] getContents(){
        final ItemStack[] out  = new ItemStack[this.contents.size()];
        this.contents.toArray(out);
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
}
