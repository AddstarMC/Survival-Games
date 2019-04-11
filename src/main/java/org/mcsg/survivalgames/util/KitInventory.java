package org.mcsg.survivalgames.util;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
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
    private ItemStack leftHand = null;
    private ItemStack rightHand = null;

    public static KitInventory deserialize(Map<String, Object> map){
        KitInventory inv = new KitInventory();
        inv.setHead((ItemStack) map.get("head"));
        inv.setChest((ItemStack) map.get("chest"));
        inv.setFeet((ItemStack) map.get("feet"));
        inv.setLegs((ItemStack) map.get("legs"));
        inv.setRightHand((ItemStack) map.get("mainHand"));
        inv.setLeftHand((ItemStack) map.get("offHand"));
        ItemStack[] contents = (ItemStack[]) map.get("contents");
        int i = 0;
        while(i < contents.length){
            inv.addContent(i,contents[i]);
            i++;
        }
        return inv;
    }

    public ItemStack getHead() {
        return this.head.clone();
    }

    public void setHead(final ItemStack head) {
        this.head = head.clone();
    }

    public ItemStack getChest() {
        return this.chest.clone();
    }

    public void setChest(final ItemStack chest) {
        this.chest = chest.clone();
    }

    public ItemStack getLegs() {
        return this.legs.clone();
    }

    public void setLegs(final ItemStack legs) {
        this.legs = legs.clone();
    }

    public ItemStack getFeet() {
        return this.feet;
    }

    public void setFeet(final ItemStack feet) {
        this.feet = feet.clone();
    }

    public ItemStack getLeftHand() {
        return this.leftHand;
    }

    public void setLeftHand(final ItemStack leftHand) {
        this.leftHand = leftHand.clone();
    }

    public ItemStack getRightHand() {
        return this.rightHand;
    }

    public void setRightHand(final ItemStack rightHand) {
        this.rightHand = rightHand.clone();
    }

    public void addContent(int pos, ItemStack i){
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
        out.put("mainHand",rightHand);
        out.put("offHand",leftHand);
        out.put("contents",contents);
        return out;
    }
}
