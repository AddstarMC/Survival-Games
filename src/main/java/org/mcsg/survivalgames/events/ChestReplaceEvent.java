package org.mcsg.survivalgames.events;

import java.util.HashSet;
import java.util.List;
import java.util.Random;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.mcsg.survivalgames.Game;
import org.mcsg.survivalgames.GameManager;
import org.mcsg.survivalgames.Game.GameMode;
import org.mcsg.survivalgames.util.ChestRatioStorage;



public class ChestReplaceEvent implements Listener{

	private Random rand = new Random();
	
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled=true)
    public void ChestListener(PlayerInteractEvent e) {
    	if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
    		BlockState clicked = e.getClickedBlock().getState();
    		if (clicked instanceof Chest || clicked instanceof DoubleChest) {
    			// Chest has been right clicked
    			int gameid = GameManager.getInstance().getPlayerGameId(e.getPlayer());
    			if (gameid != -1) {
    				Game game = GameManager.getInstance().getGame(gameid);
    				if (game.getMode() == GameMode.INGAME) {
    					// Grab the "opened chest" list or create an empty one
    					HashSet<Block>openedChest = GameManager.openedChest.get(gameid);
    					if (openedChest == null) {
    						openedChest = new HashSet<Block>();
    					}

    					// If chest hasn't been opened yet.. we have to fill it!
    					if (!openedChest.contains(e.getClickedBlock())) {
    					
    						// Single or double chest?
    						Inventory[] invs = null;
    						if (clicked instanceof Chest) {
    							invs = new Inventory[] {
    									((Chest) clicked).getBlockInventory()
    							};
    						} else {
    							invs = new Inventory[] {
    									((DoubleChest) clicked).getLeftSide().getInventory(),
    									((DoubleChest) clicked).getRightSide().getInventory()
    							};
    						}

    						// Loop through all the item lists, pick some items and randomly scatter them
    						for (Inventory inv : invs) {
    							inv.setContents(new ItemStack[inv.getContents().length]);
    							List<ItemStack> chestContents = ChestRatioStorage.getInstance().getItems();
    							
    				            for (ItemStack i : chestContents) {
    				                // Find a random empty slot
    				                int l = rand.nextInt(26);
    				                while(inv.getItem(l) != null) {
    				                    l = rand.nextInt(26);
    				                }
    				                inv.setItem(l, i); 		// Add selected item to this chest
    				            }
    						}
        					// Record this new chest in the game data
        					openedChest.add(e.getClickedBlock());
        					GameManager.openedChest.put(gameid, openedChest);
    					}
    				} else {
    					e.setCancelled(true);
    					return;
    				}
    			}
    		}
    	}
    }
}
