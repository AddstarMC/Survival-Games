package org.mcsg.survivalgames.logging;

import java.io.Serializable;

import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;

public class SgBlockData implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String world;
    private BlockData prevBlockData;
    private BlockData newBlockData;
    private int x,y,z;
    private int gameid;
    private ItemStack[] items;
    
    /**
     *
     * @param prevMaterial the previous material
     * @param newMaterial the new material
     * @param x x loc
     * @param y y loc
     * @param z z loc
     * 
     * Provides a object for holding the data for block changes
     */
    public SgBlockData(int gameid, String world, BlockData prevMaterial, BlockData newMaterial, int x, int y, int z, ItemStack[] items) {
        this.gameid = gameid;
        this.world = world;
        this.prevBlockData = prevMaterial;
        this.newBlockData = newMaterial;
        this.x = x;
        this.y = y;
        this.z = z;
        this.items = items;
    }
    
    public int getGameId(){
        return gameid;
    }
    
    public String getWorld(){
        return world;
    }

    public BlockData getPrevBlockData() {
        return prevBlockData;
    }

    public BlockData getNewBlockData() {
        return newBlockData;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }
    
    public ItemStack[] getItems(){
    	return items;
    }
}
