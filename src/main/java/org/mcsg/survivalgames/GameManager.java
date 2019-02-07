package org.mcsg.survivalgames;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.mcsg.survivalgames.Game.GameMode;
import org.mcsg.survivalgames.MessageManager.PrefixType;
import org.mcsg.survivalgames.api.PlayerLeaveArenaEvent;
import org.mcsg.survivalgames.stats.StatsManager;
import org.mcsg.survivalgames.util.Kit;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;

public class GameManager {

	static GameManager instance = new GameManager();
    public static HashMap<Integer, HashSet<Block>> openedChest = new HashMap<>();
	private SurvivalGames p;
    private ArrayList<Game> games = new ArrayList<>();
    ArrayList<Kit> kits = new ArrayList<>();
    private HashSet<UUID> kitsel = new HashSet<>();
	MessageManager msgmgr = MessageManager.getInstance();

	private GameManager() {

	}

	public static GameManager getInstance() {
		return instance;
	}

	public void setup(SurvivalGames plugin) {
		p = plugin;
		LoadGames();
		LoadKits();
		for (Game g: getGames()) {
            openedChest.put(g.getID(), new HashSet<>());
		}
	}

	public Plugin getPlugin() {
		return p;
	}

	public void reloadGames() {
		LoadGames();
	}


	public void LoadKits(){
		Set<String> kits1 = SettingsManager.getInstance().getKits().getConfigurationSection("kits").getKeys(false);
		for(String s:kits1){
			kits.add(new Kit(s));
		}
	}

	public void LoadGames() {
		FileConfiguration c = SettingsManager.getInstance().getSystemConfig();
		games.clear();
		int no = c.getInt("sg-system.arenano", 0);
		int loaded = 0;
		int a = 1;
		while (loaded < no) {
			if (c.isSet("sg-system.arenas." + a + ".x1")) {
				//c.set("sg-system.arenas."+a+".enabled",c.getBoolean("sg-system.arena."+a+".enabled", true));
				if (c.getBoolean("sg-system.arenas." + a + ".enabled")) {
					//SurvivalGames.$(c.getString("sg-system.arenas."+a+".enabled"));
					//c.set("sg-system.arenas."+a+".vip",c.getBoolean("sg-system.arenas."+a+".vip", false));
					SurvivalGames.log(0, "Loading Arena: " + a);
					SurvivalGames.log(0, "  Spawn points: " + SettingsManager.getInstance().getSpawnCount(a));
					SurvivalGames.log(0, "  DM spawns   : " + SettingsManager.getInstance().getDMSpawnCount(a));
					loaded++;
					games.add(new Game(a));
					StatsManager.getInstance().addArena(a);
				}
			}
			a++;
		}		
	}

	public int getBlockGameId(Location v) {
		for (Game g: games) {
			if (g.isBlockInArena(v)) {
				return g.getID();
			}
		}
		return -1;
	}

	public int getPlayerGameId(Player p) {
		for (Game g: games) {
			if (g.isPlayerActive(p)) {
				return g.getID();
			}
		}
		return -1;
	}

	public int getPlayerSpectateId(Player p) {
		for (Game g: games) {
			if (g.isSpectator(p)) {
				return g.getID();
			}
		}
		return -1;
	}

	public boolean isPlayerActive(Player player) {
		for (Game g: games) {
			if (g.isPlayerActive(player)) {
				return true;
			}
		}
		return false;
	}

	public boolean isPlayerInactive(Player player) {
		for (Game g: games) {
			if (g.isPlayerActive(player)) {
				return true;
			}
		}
		return false;
	}

	public boolean isSpectator(Player player) {
		for (Game g: games) {
			if (g.isSpectator(player)) {
				return true;
			}
		}
		return false;
	}

	public void removeFromOtherQueues(Player p, int id) {
		for (Game g: getGames()) {
			if (g.isInQueue(p) && g.getID() != id) {
				g.removeFromQueue(p);
				msgmgr.sendMessage(PrefixType.INFO, "Removed from the queue in arena " + g.getID(), p);
			}
		}
	}

	public boolean isInKitMenu(Player p){
		return kitsel.contains(p.getUniqueId());
	}

	public void leaveKitMenu(Player p){
		kitsel.remove(p.getUniqueId());
	}

	public void openKitMenu(Player p){
		kitsel.add(p.getUniqueId());
	}

    public void selectKit(Player p, Game g, int i) {
        p.getInventory().clear();
        p.getEquipment().setArmorContents(null);
        p.updateInventory();
        ArrayList<Kit> kits = getKits(p, g);
        if ((i >= 0) && (i < kits.size())) {
            Kit k = kits.get(i);
            if (k != null) {
                p.getInventory().setContents(k.getContents().toArray(new ItemStack[0]));
            }
        }
        p.updateInventory();
    }
	public void selectKit(Player p, int i) {
        selectKit(p, null, i);
    }

    public Game getGame(Player p) {
        for (Game g : games) {
            if (g.isInQueue(p) || g.isPlayerActive(p) || g.isPlayerinactive(p))
                return g;
        }
        return null;
    }

	public int getGameCount() {
		return games.size();
	}

	public Game getGame(int a) {
		//int t = gamemap.get(a);
		for (Game g: games) {
			if (g.getID() == a) {
				return g;
			}
		}
		return null;
	}

	public void removePlayer(Player p, boolean b) {
		Game game = getGame(getPlayerGameId(p));
		game.playerLeave(p, b);
		PlayerLeaveArenaEvent event = new PlayerLeaveArenaEvent(p, getGame(getPlayerGameId(p)), !p.isOnline());
		Bukkit.getPluginManager().callEvent(event);
	}

	public void removeSpectator(Player p) {
		getGame(getPlayerSpectateId(p)).removeSpectator(p);
	}

	public void disableGame(int id) {
		getGame(id).disable();
	}

	public void enableGame(int id) {
		getGame(id).enable();
	}

	public ArrayList < Game > getGames() {
		return games;
	}

	public GameMode getGameMode(int a) {
		for (Game g: games) {
			if (g.getID() == a) {
				return g.getMode();
			}
		}
		return null;
	}

    public ArrayList<Kit> getKits(Player p){
        return getKits(p, null);
    }

    public ArrayList<Kit> getKits(Player p, Game g) {
        ArrayList<Kit> k = new ArrayList<>();
        if (g != null) {
            for (Kit kit : g.getKits()) {
                if (kit.canUse(p)) {
                    k.add(kit);
                }
            }
        } else
            for (Kit kit : kits) {
                if (kit.canUse(p)) {
                    k.add(kit);
                }
            }
        return k;
    }

	//TODO: Actually make this countdown correctly
	public void startGame(int gameId) {
		getGame(gameId).countdown(10);
	}

	public void addPlayer(Player p, int g) {
		Game game = getGame(g);
		if (game == null) {
			MessageManager.getInstance().sendFMessage(PrefixType.ERROR, "error.input",p, "message-No game by this ID exist!");
			return;
		}
		game.addPlayer(p);
	}

	public void autoAddPlayer(Player pl) {
        ArrayList<Game> qg = new ArrayList<>();
		for (Game g: games) {
			if (g.getMode() == Game.GameMode.WAITING) {
				qg.add(g);
			}
		}
		//TODO: fancy auto balance algorithm
		if (qg.size() == 0) {
			pl.sendMessage(ChatColor.RED + "No games to join");
			msgmgr.sendMessage(PrefixType.WARNING, "No games to join!", pl);
			return;
		}
		
		int randomGame = (new Random()).nextInt(qg.size());
		Game game = qg.get(randomGame);
		game.addPlayer(pl);
	}

	public WorldEditPlugin getWorldEdit() {
		return p.getWorldEdit();
	}

	public void createArenaFromSelection(Player pl) {
		FileConfiguration c = SettingsManager.getInstance().getSystemConfig();
		//SettingsManager s = SettingsManager.getInstance();

		WorldEditPlugin we = p.getWorldEdit();
		LocalSession session = we.getSession(pl);
		RegionSelector selector = session.getRegionSelector(we.wrapPlayer(pl).getWorld());
		Region region = null;
		try {
			region = selector.getRegion();
		} catch (IncompleteRegionException ignored) {
		}
		if (region == null) {
			msgmgr.sendMessage(PrefixType.WARNING, "You must make a WorldEdit Selection first!", pl);
			return;
		}
		BlockVector3 max = region.getMaximumPoint();
		BlockVector3 min = region.getMinimumPoint();
		if (region.getWorld() == null) {
			msgmgr.sendMessage(PrefixType.WARNING, "Selection did not have a valid world !!", pl);
			return;
		}
		/* if(max.getWorld()!=SettingsManager.getGameWorld() || min.getWorld()!=SettingsManager.getGameWorld()){
            pl.sendMessage(ChatColor.RED+"Wrong World!");
            return;
        }*/

		int no = c.getInt("sg-system.arenano") + 1;
		c.set("sg-system.arenano", no);
		if (games.size() == 0) {
			no = 1;
		} else no = games.get(games.size() - 1).getID() + 1;
		SettingsManager.getInstance().getSpawns().set(("spawns." + no), null);
		c.set("sg-system.arenas." + no + ".world", region.getWorld().getName());
		c.set("sg-system.arenas." + no + ".x1", max.getBlockX());
		c.set("sg-system.arenas." + no + ".y1", max.getBlockY());
		c.set("sg-system.arenas." + no + ".z1", max.getBlockZ());
		c.set("sg-system.arenas." + no + ".x2", min.getBlockX());
		c.set("sg-system.arenas." + no + ".y2", min.getBlockY());
		c.set("sg-system.arenas." + no + ".z2", min.getBlockZ());
		c.set("sg-system.arenas." + no + ".enabled", true);

		SettingsManager.getInstance().saveSystemConfig();
		hotAddArena(no);
		pl.sendMessage(ChatColor.GREEN + "Arena ID " + no + " Succesfully added");

	}

	private void hotAddArena(int no) {
		Game game = new Game(no);
		games.add(game);
		StatsManager.getInstance().addArena(no);
		//SurvivalGames.$("game added "+ games.size()+" "+SettingsManager.getInstance().getSystemConfig().getInt("gs-system.arenano"));
	}

	public void hotRemoveArena(int no) {
		for (Game g: games.toArray(new Game[0])) {
			if (g.getID() == no) {
				games.remove(getGame(no));
			}
		}
	}

	public void gameEndCallBack(int id) {
		getGame(id).setRBStatus("clearing chest");
        openedChest.put(id, new HashSet<>());
	}

	public boolean checkGameDisabled(Location loc, Player player) {
		int gameID = getPlayerGameId(player);
		if (gameID == -1) {
			int blockgameid = GameManager.getInstance().getBlockGameId(loc);
			if (blockgameid != -1) {
				return GameManager.getInstance().getGame(blockgameid).getGameMode() != GameMode.DISABLED;
			}
			return false;
		}
		return false;
	}
	public String getStringList(int gid){
		Game g = getGame(gid);
		if (g == null)
			return null;
		
		StringBuilder sb = new StringBuilder();
		Player[][]players = g.getPlayers();

        sb.append(ChatColor.GREEN + "<---------------------[ Alive: ").append(players[0].length).append(" ]--------------------->\n").append(ChatColor.GREEN).append(" ");
		for(Player p: players[0]){
            sb.append(p.getName()).append(",");
		}
		sb.append("\n\n");
        sb.append(ChatColor.RED + "<---------------------[ Dead: ").append(players[1].length).append(" ]---------------------->\n").append(ChatColor.GREEN).append(" ");
		for(Player p: players[1]){
            sb.append(p.getName()).append(",");
		}
		sb.append("\n\n");

		return sb.toString();
	}

	public int getIdFromName(String gameName) {
		for (Game game : games) {
			if (game.getName().equalsIgnoreCase(gameName))
				return game.getID();
		}
		return -1;
	}


}
