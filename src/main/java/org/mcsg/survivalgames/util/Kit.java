package org.mcsg.survivalgames.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.mcsg.survivalgames.SettingsManager;
import org.mcsg.survivalgames.SurvivalGames;

public class Kit {

    private String name;
    private double cost;

    public KitInventory getKitInventory() {
        return this.kitInventory;
    }

    private KitInventory kitInventory = new KitInventory();
    private ItemStack icon;

    public Kit(String name){
        this.name = name;
        load();
    }

    public void load(){
        FileConfiguration c = SettingsManager.getInstance().getKits();
        final ConfigurationSection section = c.getConfigurationSection("kits."+this.name);
        this.cost = section.getDouble("cost",0);
        this.icon = ItemReader.read(section.getString("icon"));
        SurvivalGames.info(0, "Kit Icon: " + this.icon);
        if(section.contains("contents")) {
            List<String> cont = section.getStringList("contents");
            int i = 0;
            for (String s : cont) {
                this.kitInventory.addContent(i, ItemReader.read(s));
                i++;
            }
        }
        if(section.contains("kitInventory")) {
           final Object kitInv = section.get("kitInventory");
            if (kitInv != null) {
                if (kitInv instanceof KitInventory) {
                    this.kitInventory = (KitInventory) kitInv;
                }
            }
        }
    }

    public List<ItemStack> getContents(){
        List<ItemStack> items = Arrays.asList(kitInventory.getContents());
        return items;
    }

    public boolean canUse(Player p){
        return p.hasPermission("sg.kit."+name);
    }

    public String getName() {
        return name;
    }

    public ItemStack getIcon(){
        return icon;
    }

    public double getCost(){
        return cost;
    }
}