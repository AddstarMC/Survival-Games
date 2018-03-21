package org.mcsg.survivalgames.events;

import java.util.HashMap;

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
	@EventHandler
	public void onBandageUse(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (p.getInventory().getItemInMainHand().getType() == Material.PAPER) {
				if (GameManager.getInstance().getBlockGameId(p.getLocation()) != -1) {
					ItemStack paper = p.getInventory().getItemInMainHand().clone();    // Must match item in hand exactly, so we copy it
					paper.setAmount(1);
					HashMap<Integer, ItemStack> removed = p.getInventory().removeItem(paper);
					p.updateInventory();
					//if ((removed != null) && (removed.size() > 0)) {
						double newhealth = e.getPlayer().getHealth() + 10;
						if (newhealth > 20) newhealth = 20;
						p.setHealth(newhealth);
						p.sendMessage(ChatColor.GREEN + "You used a bandage to heal yourself.");
					//} else {
					//	p.sendMessage(ChatColor.RED + "Sorry, unable to heal due to system error.");
					//	SurvivalGames.$(ChatColor.RED + "Healing refused! Unable to remove Paper/Bandage from player: " + p.getName());
					//}
				}
			}
		}
	}
}