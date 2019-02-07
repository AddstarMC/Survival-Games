package org.mcsg.survivalgames.logging;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.mcsg.survivalgames.Game;
import org.mcsg.survivalgames.GameManager;

@SuppressWarnings("deprecation")
public class LoggingManager implements  Listener{
    public static HashMap<String, Integer> i = new HashMap<>();

	private static LoggingManager instance = new LoggingManager();



	private LoggingManager(){

		i.put("BCHANGE",1);
		i.put("BPLACE", 1);
		i.put("BFADE", 1);
		i.put("BBLOW", 1);
		i.put("BSTARTFIRE",1);
		i.put("BBURN",1);
		i.put("BREDSTONE",1);
		i.put("LDECAY",1);
		i.put("BPISTION", 1);


	}

	public static LoggingManager getInstance(){
		return instance;
	}


	@EventHandler(priority = EventPriority.MONITOR)
	public void blockChanged(BlockBreakEvent e){
		if(e.isCancelled())return;
        logBlockDestroyed(e.getBlock());
		i.put("BCHANGE", i.get("BCHANGE")+1);
		//    Sur(1);
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void blockChanged(BlockPlaceEvent e){
		if(e.isCancelled())return;

		logBlockCreated(e.getBlock());
		i.put("BPLACE", i.get("BPLACE")+1);

		//    System.out.println(2);

	}
	/* @EventHandler(priority = EventPriority.MONITOR)
    public void blockChanged(BlockPhysicsEvent e){
        logBlockCreated(e.getBlock());
    }*/
	@EventHandler(priority = EventPriority.MONITOR)
	public void blockChanged(BlockFadeEvent e){
		if(e.isCancelled())return;

        logBlockDestroyed(e.getBlock());
		i.put("BFADE", i.get("BFADE")+1);

		//    System.out.println(3);

	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void blockChange(EntityExplodeEvent e){
		if(e.isCancelled())return;

		for(Block b :e.blockList()){
            logBlockDestroyed(b);
			//        System.out.println(4);

		}

		i.put("BBLOW", i.get("BBLOW")+1);

	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void blockChange(BlockIgniteEvent e){
		if(e.isCancelled())return;

		logBlockCreated(e.getBlock());
		i.put("BSTARTFIRE", i.get("BSTARTFIRE")+1);

		//     System.out.println(5);


	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void blockChanged(BlockBurnEvent e){
		if(e.isCancelled())return;

        logBlockDestroyed(e.getBlock());
		i.put("BBURN", i.get("BBURN")+1);

		//    System.out.println(6);

	}

    @EventHandler(priority = EventPriority.MONITOR)
	public void blockChanged(BlockGrowEvent e){
		if(e.isCancelled())return;

		logBlockCreated(e.getBlock());
		//    System.out.println(7);

	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void blockChanged(BlockFormEvent e){
		logBlockCreated(e.getBlock());
	}

	/*@EventHandler(priority = EventPriority.MONITOR)
	public void blockChanged(BlockRedstoneEvent e){
		if(e.getBlock().getType() != Material.REDSTONE_WIRE){
			logBlockDestoryed(e.getBlock());
			i.put("BREDSTONE", i.get("BREDSTONE")+1);
		}

		//   System.out.println(8);

	}*/
	@EventHandler(priority = EventPriority.MONITOR)
	public void blockChanged(LeavesDecayEvent e){
		if(e.isCancelled())return;

        logBlockDestroyed(e.getBlock());
		i.put("LDECAY", i.get("LDECAY")+1);

		//    System.out.println(9);

	}

	public void blockChange(BlockPistonExtendEvent e){
		if(e.isCancelled())return;

		for(Block b: e.getBlocks()){
			logBlockCreated(b);
		}
		i.put("BPISTION", i.get("BPISTION")+1);

	}

	public void logBlockCreated(Block b){
		if(GameManager.getInstance().getBlockGameId(b.getLocation()) == -1)
			return;
		if( GameManager.getInstance().getGameMode(GameManager.getInstance().getBlockGameId(b.getLocation())) == Game.GameMode.DISABLED)
			return ;

		QueueManager.getInstance().add(
                new SgBlockData(
						GameManager.getInstance().getBlockGameId(b.getLocation()),
						b.getWorld().getName(),
                        Material.AIR.createBlockData(),
                        b.getType().createBlockData(),
						b.getX(),
						b.getY(),
						b.getZ(),
						null)
				);
	}


    public void logBlockDestroyed(Block b) {
		if(GameManager.getInstance().getBlockGameId(b.getLocation()) == -1)
			return;
		if( GameManager.getInstance().getGameMode(GameManager.getInstance().getBlockGameId(b.getLocation())) == Game.GameMode.DISABLED)
			return ;
        if (b.getType() == Material.FIRE)
			return;
		QueueManager.getInstance().add(
                new SgBlockData(
						GameManager.getInstance().getBlockGameId(b.getLocation()),
						b.getWorld().getName(),
                        b.getType().createBlockData(),
                        Material.AIR.createBlockData(),
						b.getX(),
						b.getY(),
						b.getZ(),
						null)
				);
	}

}
