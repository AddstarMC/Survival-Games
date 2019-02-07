package org.mcsg.survivalgames.events;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.mcsg.survivalgames.Game;
import org.mcsg.survivalgames.GameManager;
import org.mcsg.survivalgames.SettingsManager;

@SuppressWarnings("deprecation")
public class PlaceEvent implements Listener {

    public ArrayList<Material> allowedPlace = new ArrayList<>();

    public PlaceEvent(){
        List<String> materialList = SettingsManager.getInstance().getConfig().getStringList("block.place.whitelist");
        for (String matName : materialList) {
            allowedPlace.add(Material.getMaterial(matName));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled=true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player p = event.getPlayer();
        if (GameManager.getInstance().checkGameDisabled(event.getBlock().getLocation(), p)) {
            event.setCancelled(true);
            return;
        }

        Game g = GameManager.getInstance().getGame(GameManager.getInstance().getPlayerGameId(p));
        if(g == null || g.isPlayerinactive(p)){
            return;
        }
        if(g.getMode() == Game.GameMode.DISABLED){
            return;
        }
        if(g.getMode() != Game.GameMode.INGAME){
            event.setCancelled(true);
            return;

        }

        if (!allowedPlace.contains(event.getBlock().getType())) {
            event.setCancelled(true);
        }

    }
}