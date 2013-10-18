package org.mcsg.survivalgames.events;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.mcsg.survivalgames.GameManager;

public class BandageUse implements Listener {
	@EventHandler(ignoreCancelled=true)
	public void onBandageUse(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (p.getItemInHand().getType() == Material.PAPER) {
				if (GameManager.getInstance().getBlockGameId(p.getLocation()) != -1) {
					p.getInventory().removeItem(new ItemStack(Material.PAPER, 1));
					p.setHealth(e.getPlayer().getHealth() + 10);
					p.sendMessage(ChatColor.GREEN + "You used a bandage to heal yourself.");
				}
			}
		}
	}
}