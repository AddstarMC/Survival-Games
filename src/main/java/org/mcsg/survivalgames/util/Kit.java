package org.mcsg.survivalgames.util;

import java.util.Arrays;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
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

    public Kit() {
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setCost(final double cost) {
        this.cost = cost;
    }

    public void setKitInventory(final KitInventory kitInventory) {
        this.kitInventory = kitInventory;
    }

    private ItemStack icon;

    public void setIcon(final ItemStack icon) {
        this.icon = icon;
    }

    public Kit(final String name){
        this.name = name;
        this.load();
    }

    private void load(){
        final FileConfiguration c = SettingsManager.getInstance().getKits();
        if (!c.contains("kits." + this.name)) {
            this.cost = 0;
            this.icon = null;
            this.kitInventory = new KitInventory();
            return;
        }
        final ConfigurationSection section = c.getConfigurationSection("kits."+this.name);
        this.cost = section.getDouble("cost",0);
        this.icon = ItemReader.read(section.getString("icon"));
        SurvivalGames.info(0, "Kit Icon: " + this.icon);
        if(section.contains("contents")) {
            final List<String> cont = section.getStringList("contents");
            int i = 0;
            for (final String s : cont) {
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
        return Arrays.asList(this.kitInventory.getContents());
    }

    public boolean canUse(final Player p){
        return p.hasPermission("sg.kit."+this.name);
    }

    public String getName() {
        return this.name;
    }

    public ItemStack getIcon(){
        return this.icon;
    }

    public double getCost(){
        return this.cost;
    }
}