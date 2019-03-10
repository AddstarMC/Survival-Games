package org.mcsg.survivalgames.events;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.mcsg.survivalgames.Game;
import org.mcsg.survivalgames.GameManager;
import org.mcsg.survivalgames.SettingsManager;

public class BreakEvent implements Listener {

    public ArrayList<Material> allowedBreak = new ArrayList<>();

    public BreakEvent(FileConfiguration config) {
        List<String> materials = config.getStringList("block.break.whitelist");
        for (String mat : materials) {
            Material m = Material.getMaterial(mat);
            if (m != null) allowedBreak.add(m);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled=true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player p = event.getPlayer();
        if (GameManager.getInstance().checkGameDisabled(event.getBlock().getLocation(), p)) {
            event.setCancelled(true);
            return;
        }
        Game g = GameManager.getInstance().getGame(GameManager.getInstance().getPlayerGameId(p));
        if(g == null || g.getMode() == Game.GameMode.DISABLED){
            return;
        }
        if(g.getMode() != Game.GameMode.INGAME){
            event.setCancelled(true);
            return;
        }

        if (!allowedBreak.contains(event.getBlock().getType())) event.setCancelled(true);
    }
}