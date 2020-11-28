package org.mcsg.survivalgames.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.mcsg.survivalgames.LobbyManager;
import org.mcsg.survivalgames.SurvivalGames;


public class KeepLobbyLoadedEvent implements Listener{
    
    @EventHandler(ignoreCancelled=true)
    public void onChunkUnload(ChunkLoadEvent e){
        LobbyManager.getInstance();
		if(LobbyManager.lobbychunks.contains(e.getChunk())){
            e.getChunk().setForceLoaded(true);
            e.getChunk().addPluginChunkTicket(SurvivalGames.plugin);
        }
    }
}
