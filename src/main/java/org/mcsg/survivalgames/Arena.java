package org.mcsg.survivalgames;

import org.bukkit.Location;

public class Arena {

    Location min;
    Location max;

    public Arena(final Location min, final Location max) {
        this.max = max;
        this.min = min;
        
    }

    public boolean containsBlock(final Location v) {
        if (v.getWorld() != this.min.getWorld()) return false;
        final double x = v.getX();
        final double y = v.getY();
        final double z = v.getZ();
        return x >= this.min.getBlockX() && x < this.max.getBlockX() + 1 && y >= this.min.getBlockY() && y < this.max.getBlockY() + 1 && z >= this.min.getBlockZ() && z < this.max.getBlockZ() + 1;
    }

    public Location getMax() {
    	Runtime.getRuntime().freeMemory();
        return this.max;
    }

    public Location getMin() {
        return this.min;
    }
}