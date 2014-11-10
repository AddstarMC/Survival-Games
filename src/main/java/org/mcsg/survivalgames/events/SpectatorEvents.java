package org.mcsg.survivalgames.events;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.mcsg.survivalgames.Game;
import org.mcsg.survivalgames.GameManager;
import org.mcsg.survivalgames.SettingsManager;

public class SpectatorEvents implements Listener {
	
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled=true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (GameManager.getInstance().isSpectator(player)) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled=true)
    public void onBlockDamage(BlockDamageEvent event) {
        Player player = event.getPlayer();
        if (GameManager.getInstance().isSpectator(player)) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled=true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (GameManager.getInstance().isSpectator(player)) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerClickEvent(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        try{
            if(GameManager.getInstance().isSpectator(player)) {
            	event.setCancelled(true);
    			Game g = GameManager.getInstance().getGame(GameManager.getInstance().getPlayerSpectateId(player));
                Player[] players = g.getPlayers()[0];

                int i = g.getNextSpec().get(player);
                ItemStack is = player.getItemInHand();
                if (is.getType() == SettingsManager.getInstance().getSpecItemNext().getType()) {
                	// Next player
                	i++;
        		} else if (is.getType() == SettingsManager.getInstance().getSpecItemPrev().getType()) {
                	// Previous player
                	i--;
                } else if (is.getType() == SettingsManager.getInstance().getSpecItemExit().getType()) {
                	// Exit spectating
                    GameManager.getInstance().removeSpectator(player);
                    return;
                }
                else {
                	// Spectators shouldn't have other item types, but if they do, just ignore the click.
                	return;
                }

                // Handle wrap around in player list
                if (i > players.length-1) i = 0;
                if (i < 0) i = players.length-1;

                g.getNextSpec().put(player, i);
                Player tpto = players[i];
                Location l = tpto.getLocation();
                l.setPitch(0);
                l.setY(l.getY()+3);
                if (l.getBlock().getType() != Material.AIR) {
                	// No room to float above player, so teleport exactly at player
	                l.setY(tpto.getLocation().getY());
                }
				player.setAllowFlight(true);
                player.setFlying(true);
                player.teleport(l);
                player.setFlying(true);
                player.sendMessage(ChatColor.GOLD + "You are now spectating: " + ChatColor.AQUA + tpto.getName());
            }
        }
        catch(Exception e){e.printStackTrace();}
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled=true)
    public void onSignChange(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        if (GameManager.getInstance().isSpectator(player)) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled=true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        Player player = null;
        if (event.getDamager() instanceof Player) {
            player = (Player)event.getDamager();
        }
        else return;
        if (GameManager.getInstance().isSpectator(player)) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled=true)
    public void onEntityDamage(EntityDamageEvent event) {
        Player player = null;
        if (event.getEntity() instanceof Player) {
            player = (Player)event.getEntity();
        }
        else return;
        if (GameManager.getInstance().isSpectator(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled=true)
    public void onItemDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (GameManager.getInstance().isSpectator(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled=true)
    public void onInventoryDrag(InventoryDragEvent event) {
    	if ((event.getInventory().getType() != InventoryType.PLAYER) && (event.getInventory().getType() != InventoryType.CRAFTING))
    		return;
    	
    	Player p = (Player) event.getWhoClicked();
        if (GameManager.getInstance().isSpectator(p)) {
            event.setCancelled(true);
        }
    }
}