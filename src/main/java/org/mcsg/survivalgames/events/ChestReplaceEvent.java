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
	
    @EventHandler(priority = EventPriority.HIGHEST)
    public void ChestListener(PlayerInteractEvent e){
    	if(e.getAction() == Action.RIGHT_CLICK_BLOCK){
    		BlockState clicked = e.getClickedBlock().getState();
    		if(clicked instanceof Chest || clicked instanceof DoubleChest){
    			int gameid = GameManager.getInstance().getPlayerGameId(e.getPlayer());
    			if(gameid != -1){
    				Game game = GameManager.getInstance().getGame(gameid);
    				if(game.getMode() == GameMode.INGAME){
    					HashSet<Block>openedChest = GameManager.openedChest.get(gameid);
    					openedChest = (openedChest == null)? new HashSet<Block>() : openedChest;
    					if(!openedChest.contains(e.getClickedBlock())){
    						Inventory[] invs = ((clicked instanceof Chest))? new Inventory[] {((Chest) clicked).getBlockInventory()}
    						: new Inventory[] {((DoubleChest)clicked).getLeftSide().getInventory(), ((DoubleChest)clicked).getRightSide().getInventory()};

    						for(Inventory inv : invs){
    							inv.setContents(new ItemStack[inv.getContents().length]);
    							
    							List<ItemStack> chestContents = ChestRatioStorage.getInstance().getItems();
    							
    				            for(ItemStack i : chestContents){
    				                int l = rand.nextInt(26);
    				                while(inv.getItem(l) != null)
    				                    l = rand.nextInt(26);
    				                inv.setItem(l, i);
    				            }
    						}
    					}
    					openedChest.add(e.getClickedBlock());
    					GameManager.openedChest.put(gameid, openedChest);
    				} else {
    					e.setCancelled(true);
    					return;
    				}
    			}
    		}
    	}
    }
}
