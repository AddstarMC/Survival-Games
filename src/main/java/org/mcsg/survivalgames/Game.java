package org.mcsg.survivalgames;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.mcsg.survivalgames.MessageManager.PrefixType;
import org.mcsg.survivalgames.api.PlayerJoinArenaEvent;
import org.mcsg.survivalgames.api.PlayerKilledEvent;
import org.mcsg.survivalgames.hooks.HookManager;
import org.mcsg.survivalgames.logging.QueueManager;
import org.mcsg.survivalgames.stats.StatsManager;
import org.mcsg.survivalgames.util.ItemReader;
import org.mcsg.survivalgames.util.Kit;

//Data container for a game

public class Game {

	public static enum GameMode {
		DISABLED, LOADING, INACTIVE, WAITING,
		STARTING, INGAME, FINISHING, RESETING, ERROR
	}

	private GameMode mode = GameMode.DISABLED;
	private ArrayList < Player > activePlayers = new ArrayList < Player > ();
	private ArrayList < Player > inactivePlayers = new ArrayList < Player > ();
	private ArrayList < String > spectators = new ArrayList < String > ();
	private ArrayList < Player > queue = new ArrayList < Player > ();
	private HashMap < String, Object > flags = new HashMap < String, Object > ();
	HashMap < Player, Integer > nextspec = new HashMap < Player, Integer > ();
	private ArrayList<Integer>tasks = new ArrayList<Integer>();

	private Arena arena;
	private int gameID;
	private String name;
	private Location dmspawn;
	private Location winloc;
	private int dmradius;
	private int gcount = 0;
	private FileConfiguration config;
	private FileConfiguration system;
	private HashMap < Integer, Player > spawns = new HashMap < Integer, Player > ();
	private int spawnCount = 0;
	private int vote = 0;
	private boolean disabled = false;
	private int endgameTaskID = 0;
	private int dmTaskID = 0;
	private boolean endgameRunning = false;
	private double rbpercent = 0;
	private String rbstatus = "";
	private long startTime = 0;
	private boolean countdownRunning;
	private StatsManager sm = StatsManager.getInstance();
	private HashMap < String, String > hookvars = new HashMap < String, String > ();
	private MessageManager msgmgr = MessageManager.getInstance();
	private GameScoreboard scoreBoard = null;

	public Game(int gameid) {
		gameID = gameid;
		name = "Arena " + gameID;
		reloadConfig();
		setup();
	}

	public void reloadConfig(){
		config = SettingsManager.getInstance().getConfig();
		system = SettingsManager.getInstance().getSystemConfig();
	}

	public void $(String msg){
		SurvivalGames.$(msg);
	}

	public void debug(String msg){
		SurvivalGames.debug(msg);
	}

	public void setup() {
		mode = GameMode.LOADING;
		int x = system.getInt("sg-system.arenas." + gameID + ".x1");
		int y = system.getInt("sg-system.arenas." + gameID + ".y1");
		int z = system.getInt("sg-system.arenas." + gameID + ".z1");

		int x1 = system.getInt("sg-system.arenas." + gameID + ".x2");
		int y1 = system.getInt("sg-system.arenas." + gameID + ".y2");
		int z1 = system.getInt("sg-system.arenas." + gameID + ".z2");

		Location max = new Location(SettingsManager.getGameWorld(gameID), Math.max(x, x1), Math.max(y, y1), Math.max(z, z1));
		Location min = new Location(SettingsManager.getGameWorld(gameID), Math.min(x, x1), Math.min(y, y1), Math.min(z, z1));

		name = system.getString("sg-system.arenas." + gameID + ".name", name);
		
		arena = new Arena(min, max);

		int dmx = system.getInt("sg-system.arenas." + gameID + ".deathmatch.x", 0);
		int dmy = system.getInt("sg-system.arenas." + gameID + ".deathmatch.y", 65);
		int dmz = system.getInt("sg-system.arenas." + gameID + ".deathmatch.z", 0);
		dmspawn = new Location(SettingsManager.getGameWorld(gameID), dmx, dmy, dmz);
		
		dmradius = system.getInt("sg-system.arenas." + gameID + ".deathmatch.radius", 26);

		String winw = system.getString("sg-system.arenas." + gameID + ".win.world", "games");
		int winx = system.getInt("sg-system.arenas." + gameID + ".win.x", 0);
		int winy = system.getInt("sg-system.arenas." + gameID + ".win.y", 65);
		int winz = system.getInt("sg-system.arenas." + gameID + ".win.z", 0);
		float winyaw = system.getInt("sg-system.arenas." + gameID + ".win.yaw", 0);
		float winp = system.getInt("sg-system.arenas." + gameID + ".win.pitch", 0);
		winloc = new Location(Bukkit.getWorld(winw), winx, winy, winz, winyaw, winp);
		
		loadspawns();

		hookvars.put("arena", gameID + "");
		hookvars.put("maxplayers", spawnCount + "");
		hookvars.put("activeplayers", "0");

		mode = GameMode.WAITING;
		
		scoreBoard = new GameScoreboard(gameID);
	}

	public void reloadFlags() {
		flags = SettingsManager.getInstance().getGameFlags(gameID);
	}

	public void saveFlags() {
		SettingsManager.getInstance().saveGameFlags(flags, gameID);
	}

	public void loadspawns() {
		for (int a = 1; a <= SettingsManager.getInstance().getSpawnCount(gameID); a++) {
			spawns.put(a, null);
			spawnCount = a;
		}
	}

	public void addSpawn() {
		spawnCount++;
		spawns.put(spawnCount, null);
	}

	public void setMode(GameMode m) {
		mode = m;
	}

	public GameMode getGameMode() {
		return mode;
	}

	public Arena getArena() {
		return arena;
	}


	/*
	 * 
	 * ################################################
	 * 
	 * 				ENABLE
	 * 
	 * ################################################
	 * 
	 * 
	 */


	public void enable() {
		mode = GameMode.WAITING;
		if(disabled){
			MessageManager.getInstance().broadcastFMessage(PrefixType.INFO, "broadcast.gameenabled", "arena-"+gameID, "arenaname-"+name);
		}
		disabled = false;
		int b = (SettingsManager.getInstance().getSpawnCount(gameID) > queue.size()) ? queue.size() : SettingsManager.getInstance().getSpawnCount(gameID);
		for (int a = 0; a < b; a++) {
			addPlayer(queue.remove(0));
		}
		int c = 1;
		for (Player p : queue) {
			msgmgr.sendMessage(PrefixType.INFO, "You are now #" + c + " in line for arena " + gameID, p);
			c++;
		}

		LobbyManager.getInstance().updateWall(gameID);

		MessageManager.getInstance().broadcastFMessage(PrefixType.INFO, "broadcast.gamewaiting", "arena-"+gameID, "arenaname-"+name);
		
		scoreBoard.reset();

	}


	/*
	 * 
	 * ################################################
	 * 
	 * 				ADD PLAYER
	 * 
	 * ################################################
	 * 
	 * 
	 */


	@SuppressWarnings("deprecation")
	public boolean addPlayer(final Player p) {
		if(SettingsManager.getInstance().getLobbySpawn() == null){
			msgmgr.sendFMessage(PrefixType.WARNING, "error.nolobbyspawn", p);
			return false;
		}
		if(!p.hasPermission("sg.arena.join."+gameID)){
			debug("permission needed to join arena: " + "sg.arena.join."+gameID);
			msgmgr.sendFMessage(PrefixType.WARNING, "game.nopermission", p, "arena-"+gameID);
			return false;
		}
		HookManager.getInstance().runHook("GAME_PRE_ADDPLAYER", "arena-"+gameID, "player-"+p.getName(), "maxplayers-"+spawns.size(), "players-"+activePlayers.size());

		GameManager.getInstance().removeFromOtherQueues(p, gameID);

		if (GameManager.getInstance().getPlayerGameId(p) != -1) {
			if (GameManager.getInstance().isPlayerActive(p)) {
				msgmgr.sendMessage(PrefixType.ERROR, "Cannot join multiple games!", p);
				return false;
			}
		}
		if(p.isInsideVehicle()){
			p.leaveVehicle();
		}
		if (spectators.contains(p)) removeSpectator(p);
		if (mode == GameMode.WAITING || mode == GameMode.STARTING) {
			if (activePlayers.size() < SettingsManager.getInstance().getSpawnCount(gameID)) {
				msgmgr.sendMessage(PrefixType.INFO, "Joining Arena '" + name + "'", p);
				PlayerJoinArenaEvent joinarena = new PlayerJoinArenaEvent(p, GameManager.getInstance().getGame(gameID));
				Bukkit.getServer().getPluginManager().callEvent(joinarena);
				if(joinarena.isCancelled()) return false;
				boolean placed = false;
				int spawnCount = SettingsManager.getInstance().getSpawnCount(gameID);

				for (int a = 1; a <= spawnCount; a++) {
					if (spawns.get(a) == null) {
						placed = true;
						spawns.put(a, p);
						p.setGameMode(org.bukkit.GameMode.SURVIVAL);

						//p.teleport(SettingsManager.getInstance().getLobbySpawn());
						p.teleport(SettingsManager.getInstance().getSpawnPoint(gameID, a));

						p.setHealth(p.getMaxHealth());
						p.setFoodLevel(20);
						p.getInventory().clear();
						p.getEquipment().setArmorContents(null);
						p.updateInventory();
						
						p.setFlying(false);
						p.setAllowFlight(false);
						p.setWalkSpeed(0.2F);
						p.setFireTicks(0);
						
						activePlayers.add(p);
						sm.addPlayer(p, gameID);
						
						scoreBoard.addPlayer(p);

						hookvars.put("activeplayers", activePlayers.size()+"");
						LobbyManager.getInstance().updateWall(gameID);
						HookManager.getInstance().runHook("GAME_POST_ADDPLAYER", "activePlayers-"+activePlayers.size());

						if(spawnCount == activePlayers.size()){
							countdown(5);
						}

						Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(GameManager.getInstance().getPlugin(), new Runnable() {
							public void run() {
								p.setFlying(false);
								p.setAllowFlight(false);
								p.setWalkSpeed(0.2F);
								p.setFireTicks(0);

								p.getInventory().clear();
								p.getEquipment().setArmorContents(null);
								p.updateInventory();
								showMenu(p);
								
								for (PotionEffect effect : p.getActivePotionEffects()) {
                                    p.removePotionEffect(effect.getType());
								}

							}
						}, 1L);
						
						break;
					}
				}
				if (!placed) {
					msgmgr.sendFMessage(PrefixType.ERROR,"error.gamefull", p,"arena-"+gameID);
					return false;
				}

			} else if (SettingsManager.getInstance().getSpawnCount(gameID) == 0) {
				msgmgr.sendMessage(PrefixType.WARNING, "No spawns set for Arena " + gameID + "!", p);
				return false;
			} else {
				msgmgr.sendFMessage(PrefixType.WARNING, "error.gamefull", p, "arena-"+gameID);
				return false;
			}
			msgFall(PrefixType.INFO, "game.playerjoingame", "player-"+p.getName(), "activeplayers-"+ getActivePlayers(), "maxplayers-"+ SettingsManager.getInstance().getSpawnCount(gameID));
			if (activePlayers.size() >= config.getInt("auto-start-players") && !countdownRunning) countdown(config.getInt("auto-start-time"));
			return true;
		} else {
			if (config.getBoolean("enable-player-queue")) {
				if (!queue.contains(p)) {
					queue.add(p);
					msgmgr.sendFMessage(PrefixType.INFO, "game.playerjoinqueue", p, "queuesize-"+queue.size());
				}
				int a = 1;
				for (Player qp: queue) {
					if (qp == p) {
						msgmgr.sendFMessage(PrefixType.INFO, "game.playercheckqueue", p,"queuepos-"+a);
						break;
					}
					a++;
				}
			}
		}
		if (mode == GameMode.INGAME) msgmgr.sendFMessage(PrefixType.WARNING, "error.alreadyingame", p);
		else if (mode == GameMode.DISABLED) msgmgr.sendFMessage(PrefixType.WARNING, "error.gamedisabled", p, "arena-"+gameID);
		else if (mode == GameMode.RESETING) msgmgr.sendFMessage(PrefixType.WARNING, "error.gamereseting", p);
		else msgmgr.sendMessage(PrefixType.INFO, "Cannot join game!", p);
		LobbyManager.getInstance().updateWall(gameID);
		return false;
	}


	public void showMenu(Player p){
		GameManager.getInstance().openKitMenu(p);
		Inventory i = Bukkit.getServer().createInventory(p, 45, ChatColor.RED+""+ChatColor.BOLD+"Please select a kit:");

		int a = 0;
		int b = 0;


		ArrayList<Kit>kits = GameManager.getInstance().getKits(p);
		if(kits == null || kits.size() == 0 || !SettingsManager.getInstance().getKits().getBoolean("enabled")){
			GameManager.getInstance().leaveKitMenu(p);
			return;
		}

		for(Kit k: kits){
			ItemStack i1 = k.getIcon();
			ItemMeta im = i1.getItemMeta();

			im.setDisplayName(ChatColor.GOLD+""+ChatColor.BOLD+k.getName());
			i1.setItemMeta(im);
			i.setItem((9 * a) + b, i1);
			a = 2;

			for(ItemStack s2:k.getContents()){
				if(s2 != null){
					i.setItem((9 * a) + b, s2);
					a++;
				}
			}

			a = 0;
			b++;
		}
		p.openInventory(i);
		debug("Showing kit menu for: " + p.getName());
	}


	public void removeFromQueue(Player p) {
		queue.remove(p);
	}

	/*
	 * 
	 * ################################################
	 * 
	 * 				VOTE
	 * 
	 * ################################################
	 * 
	 * 
	 */


	ArrayList < Player > voted = new ArrayList < Player > ();

	public void vote(Player pl) {


		if (GameMode.STARTING == mode) {
			msgmgr.sendMessage(PrefixType.WARNING, "Game already starting!", pl);
			return;
		}
		if (GameMode.WAITING != mode) {
			msgmgr.sendMessage(PrefixType.WARNING, "Game already started!", pl);
			return;
		}
		if (voted.contains(pl)) {
			msgmgr.sendMessage(PrefixType.WARNING, "You already voted!", pl);
			return;
		}
		vote++;
		voted.add(pl);
		msgFall(PrefixType.INFO, "game.playervote", "player-"+pl.getName());
		HookManager.getInstance().runHook("PLAYER_VOTE", "player-"+pl.getName());
		scoreBoard.playerLiving(pl);
		/*for(Player p: activePlayers){
            p.sendMessage(ChatColor.AQUA+pl.getName()+" Voted to start the game! "+ Math.round((vote +0.0) / ((getActivePlayers() +0.0)*100)) +"/"+((c.getInt("auto-start-vote")+0.0))+"%");
        }*/
		// Bukkit.getServer().broadcastPrefixType((vote +0.0) / (getActivePlayers() +0.0) +"% voted, needs "+(c.getInt("auto-start-vote")+0.0)/100);
		if ((((vote + 0.0) / (getActivePlayers() +0.0))>=(config.getInt("auto-start-vote")+0.0)/100) && getActivePlayers() > 1) {
			countdown(config.getInt("auto-start-time"));
			for (Player p: activePlayers) {
				//p.sendMessage(ChatColor.LIGHT_PURPLE + "Game Starting in " + c.getInt("auto-start-time"));
				msgmgr.sendMessage(PrefixType.INFO, "Game starting in " + config.getInt("auto-start-time") + "!", p);
				scoreBoard.playerLiving(pl);
			}
		}
	}

	/*
	 * 
	 * ################################################
	 * 
	 * 				START GAME
	 * 
	 * ################################################
	 * 
	 * 
	 */
	public void startGame() {
		if (mode == GameMode.INGAME) {
			return;
		}

		if (activePlayers.size() <= 0) {
			for (Player pl: activePlayers) {
				msgmgr.sendMessage(PrefixType.WARNING, "Not enough players!", pl);
				mode = GameMode.WAITING;
				LobbyManager.getInstance().updateWall(gameID);

			}
			return;
		} else {
			// Remove all entities in the world
			for (Entity entity : this.arena.getMax().getWorld().getEntities()) {
				if (entity instanceof Player) continue;
				entity.remove();
            }
			startTime = new Date().getTime();
			for (Player pl: activePlayers) {
				pl.setHealth(pl.getMaxHealth());
				msgmgr.sendFMessage(PrefixType.INFO, "game.goodluck", pl);
				scoreBoard.playerLiving(pl);
			}
			if (config.getBoolean("restock-chest")) {
				SettingsManager.getGameWorld(gameID).setTime(0);
				gcount++;
				tasks.add(Bukkit.getScheduler().scheduleSyncDelayedTask(GameManager.getInstance().getPlugin(),
						new NightChecker(),
						14400));
			}
			if (config.getInt("grace-period") != 0) {
				for (Player play: activePlayers) {
					msgmgr.sendMessage(PrefixType.INFO, "You have a " + config.getInt("grace-period") + " second grace period!", play);
				}
				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(GameManager.getInstance().getPlugin(), new Runnable() {
					public void run() {
						for (Player play: activePlayers) {
							msgmgr.sendMessage(PrefixType.INFO, "Grace period has ended!", play);
						}
					}
				}, config.getInt("grace-period") * 20);
			}
			if(config.getBoolean("deathmatch.enabled")) {
				SurvivalGames.$("Launching deathmatch timer...");
				dmTaskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(GameManager.getInstance().getPlugin(), new DeathMatchTimer(), 40L, 20L);
				tasks.add(dmTaskID);
			}
		}

		mode = GameMode.INGAME;
		LobbyManager.getInstance().updateWall(gameID);
		MessageManager.getInstance().broadcastFMessage(PrefixType.INFO, "broadcast.gamestarted", "arena-"+gameID, "arenaname-"+name);

	}
	/*
	 * 
	 * ################################################
	 * 
	 * 				COUNTDOWN
	 * 
	 * ################################################
	 * 
	 * 
	 */
	public int getCountdownTime() {
		return count;
	}

	int count = 20;
	int tid = 0;
	public void countdown(int time) {
		//Bukkit.broadcastMessage(""+time);
		MessageManager.getInstance().broadcastFMessage(PrefixType.INFO, "broadcast.gamestarting", "arena-"+gameID, "t-"+time, "arenaname-"+name);
		countdownRunning = true;
		count = time;
		Bukkit.getScheduler().cancelTask(tid);

		if (mode == GameMode.WAITING || mode == GameMode.STARTING) {
			mode  = GameMode.STARTING;
			tid = Bukkit.getScheduler().scheduleSyncRepeatingTask((Plugin) GameManager.getInstance().getPlugin(), new Runnable() {
				public void run() {
					if (count > 0) {
						if (count % 10 == 0) {
							msgFall(PrefixType.INFO, "game.countdown","t-"+count);
						}
						if (count < 6) {
							msgFall(PrefixType.INFO, "game.countdown","t-"+count);

						}
						count--;
						LobbyManager.getInstance().updateWall(gameID);
					} else {
						startGame();
						Bukkit.getScheduler().cancelTask(tid);
						countdownRunning = false;
					}
				}
			}, 0, 20);

		}
	}

	/*
	 * 
	 * ################################################
	 * 
	 * 				REMOVE PLAYER
	 * 
	 * ################################################
	 * 
	 * 
	 */

	public void playerLeave(final Player p, boolean teleport) {
		msgFall(PrefixType.INFO, "game.playerleavegame", "player-" + p.getName());
		if (teleport) {
			p.teleport(SettingsManager.getInstance().getLobbySpawn());
		}
		// Remove any potion/fire effects
        for (PotionEffect effect : p.getActivePotionEffects()) {
            p.removePotionEffect(effect.getType());
        }
        if (p.getFireTicks() > 0) {
            	p.setFireTicks(0);
        }

        sm.removePlayer(p, gameID);
		scoreBoard.removePlayer(p);
		activePlayers.remove(p);
		inactivePlayers.remove(p);
		voted.remove(p);
		
		for (Object in : spawns.keySet().toArray()) {
			if (spawns.get(in) == p) spawns.remove(in);
		}

		HookManager.getInstance().runHook("PLAYER_REMOVED", "player-"+p.getName());
		LobbyManager.getInstance().updateWall(gameID);

		if (activePlayers.size() < 2 && mode == GameMode.INGAME) {
			mode = GameMode.FINISHING;
			tasks.add(Bukkit.getScheduler().scheduleSyncDelayedTask(GameManager.getInstance().getPlugin(), new Runnable() {
				public void run(){
					playerWin(p);
					endGame();
				}
			}, 1L));
		}
	}

	/*
	 * 
	 * ################################################
	 * 
	 * 			   HANDLE PLAYER DEATH
	 * 
	 *  PLAYERS DIE A REAL DEATH WHICH IS HANDLED HERE 
	 * 
	 * ################################################
	 * 
	 * 
	 */
	public void playerDeath(PlayerDeathEvent e) {
		final Player p = e.getEntity();
		if (!activePlayers.contains(p)) return;
		
		sm.playerDied(p, activePlayers.size(), gameID, new Date().getTime() - startTime);
		scoreBoard.playerDead(p);
		activePlayers.remove(p);
		inactivePlayers.add(p);
		for (Object in : spawns.keySet().toArray()) {
			if (spawns.get(in) == p) spawns.remove(in);
		}

		PlayerKilledEvent pk = null;
		if (mode != GameMode.WAITING && p.getLastDamageCause() != null && p.getLastDamageCause().getCause() != null) {
			DamageCause cause = p.getLastDamageCause().getCause();
			switch (cause) {
			case ENTITY_ATTACK:
				if(p.getLastDamageCause().getEntityType() == EntityType.PLAYER){
					EntityType enttype = p.getLastDamageCause().getEntityType();
					Player killer = p.getKiller();
					String killername = "Unknown";
					
					if (killer != null) {
						killername = killer.getName();
					}
					
					String itemname = "Unknown Item";
					if (killer != null) {
						itemname = ItemReader.getFriendlyItemName(killer.getItemInHand().getType());
					}

					msgFall(PrefixType.INFO, "death."+enttype, "player-"+p.getName(), "killer-"+killername, "item-"+itemname);
							
					if (killer != null && p != null) {
						sm.addKill(killer, p, gameID, name);
						scoreBoard.incScore(killer);
					}
					pk = new PlayerKilledEvent(p, this, killer, cause);
				}
				else {
					msgFall(PrefixType.INFO, "death." + p.getLastDamageCause().getEntityType(),
							"player-" + p.getName(),
							"killer-" + p.getLastDamageCause().getEntityType());
					pk = new PlayerKilledEvent(p, this, null, cause);
				}
				break;
			default:
				msgFall(PrefixType.INFO, "death." + cause.name(), 
						"player-" + p.getName(), 
						"killer-" + cause);
				pk = new PlayerKilledEvent(p, this, null, cause);

				break;
			}
			Bukkit.getServer().getPluginManager().callEvent(pk);

			if (getActivePlayers() > 1) {
				for (Player pl: getAllPlayers()) {
					msgmgr.sendMessage(PrefixType.INFO, ChatColor.DARK_AQUA + "There are " + ChatColor.YELLOW + "" 
							+ getActivePlayers() + ChatColor.DARK_AQUA + " players remaining!", pl);
				}
			}
		}

		for (Player pe: activePlayers) {
			Location l = pe.getLocation();
			l.setY(l.getWorld().getMaxHeight());
			l.getWorld().strikeLightningEffect(l);
		}

		if (getActivePlayers() <= config.getInt("endgame.players") && config.getBoolean("endgame.fire-lighting.enabled") && !endgameRunning) {

			tasks.add(Bukkit.getScheduler().scheduleSyncRepeatingTask(GameManager.getInstance().getPlugin(),
					new EndgameManager(),
					0,
					config.getInt("endgame.fire-lighting.interval") * 20));
		}

		if (activePlayers.size() < 2 && mode == GameMode.INGAME) {
			mode = GameMode.FINISHING;
			LobbyManager.getInstance().updateWall(gameID);
			tasks.add(Bukkit.getScheduler().scheduleSyncDelayedTask(GameManager.getInstance().getPlugin(), new Runnable() {
				public void run(){
					playerWin(p);
					endGame();
				}
			}, 10L));
		}
		LobbyManager.getInstance().updateWall(gameID);
	}


	/*
	 * 
	 * ################################################
	 * 
	 * 				PLAYER WIN
	 * 
	 * ################################################
	 * 
	 * 
	 */
	public void playerWin(Player p) {
		if (GameMode.DISABLED == mode) return;
		Player win = activePlayers.get(0);
		// clearInv(p);
		win.teleport(winloc);
		//restoreInv(win);
		scoreBoard.removePlayer(p);
		msgmgr.broadcastFMessage(PrefixType.INFO, "game.playerwin","arena-"+gameID, "victim-"+p.getName(), "player-"+win.getName(), "arenaname-"+name);

		mode = GameMode.FINISHING;
		LobbyManager.getInstance().updateWall(gameID);
		LobbyManager.getInstance().gameEnd(gameID, win);

		clearSpecs();
		win.setHealth(p.getMaxHealth());
		win.setFoodLevel(20);
		win.setFireTicks(0);
		win.setFallDistance(0);

		sm.playerWin(win, gameID, new Date().getTime() - startTime);
		sm.saveGame(gameID, win, getActivePlayers() + getInactivePlayers(), new Date().getTime() - startTime);

		activePlayers.clear();
		inactivePlayers.clear();
		spawns.clear();

		loadspawns();
		LobbyManager.getInstance().updateWall(gameID);
		MessageManager.getInstance().broadcastFMessage(PrefixType.INFO, "broadcast.gameend", "arena-"+gameID, "arenaname-"+name);

		// Remove all entities in the world
		for (Entity entity : this.arena.getMax().getWorld().getEntities()) {
			if (entity instanceof Player) continue;
			entity.remove();
        }
	}

	public void endGame() {
		mode = GameMode.WAITING;
		resetArena();
		LobbyManager.getInstance().updateWall(gameID);
	}
	/*
	 * 
	 * ################################################
	 * 
	 * 				DISABLE
	 * 
	 * ################################################
	 * 
	 * 
	 */
	public void disable() {
		disabled = true;
		spawns.clear();
		scoreBoard.reset();

		for (int a = 0; a < activePlayers.size(); a = 0) {
			try {

				Player p = activePlayers.get(a);
				msgmgr.sendMessage(PrefixType.WARNING, "Game disabled!", p);
				playerLeave(p, true);
			} catch (Exception e) {}

		}

		for (int a = 0; a < inactivePlayers.size(); a = 0) {
			try {

				Player p = inactivePlayers.remove(a);
				msgmgr.sendMessage(PrefixType.WARNING, "Game disabled!", p);
			} catch (Exception e) {}

		}

		clearSpecs();
		queue.clear();

		endGame();
		LobbyManager.getInstance().updateWall(gameID);
		MessageManager.getInstance().broadcastFMessage(PrefixType.INFO, "broadcast.gamedisabled", "arena-"+gameID, "arenaname-"+name);

	}
	/*
	 * 
	 * ################################################
	 * 
	 * 				RESET
	 * 
	 * ################################################
	 * 
	 * 
	 */
	public void resetArena() {

		for(Integer i: tasks){
			Bukkit.getScheduler().cancelTask(i);
		}

		tasks.clear();
		vote = 0;
		voted.clear();

		mode = GameMode.RESETING;
		endgameRunning = false;

		Bukkit.getScheduler().cancelTask(endgameTaskID);
		GameManager.getInstance().gameEndCallBack(gameID);
		QueueManager.getInstance().rollback(gameID, false);
		LobbyManager.getInstance().updateWall(gameID);
		
		scoreBoard.reset();

	}

	public void resetCallback() {
		if (!disabled){
			enable();
		}
		else mode = GameMode.DISABLED;
		LobbyManager.getInstance().updateWall(gameID);
	}

	/*
	public void saveInv(Player p) {
		ItemStack[][] store = new ItemStack[2][1];

		store[0] = p.getInventory().getContents();
		store[1] = p.getInventory().getArmorContents();

		inv_store.put(p, store);

	}
	*/

	/*
	public void restoreInvOffline(String p) {
		restoreInv(Bukkit.getPlayer(p));
	}
	*/

	/*
	 * 
	 * ################################################
	 * 
	 * 				SPECTATOR
	 * 
	 * ################################################
	 * 
	 * 
	 */




	public void addSpectator(final Player p) {
		if (mode != GameMode.INGAME) {
			msgmgr.sendMessage(PrefixType.WARNING, "You can only spectate running games!", p);
			return;
		}

		p.teleport(SettingsManager.getInstance().getSpawnPoint(gameID, 1).add(0, 10, 0));

		HookManager.getInstance().runHook("PLAYER_SPECTATE", "player-"+p.getName());

		for (Player pl: Bukkit.getOnlinePlayers()) {
			pl.hidePlayer(p);
		}

		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(GameManager.getInstance().getPlugin(), new Runnable() {
			@SuppressWarnings("deprecation")
			public void run() {
				p.setFlying(false);
				p.setAllowFlight(false);
				p.setWalkSpeed(0.2F);
				p.setFireTicks(0);

				p.getInventory().clear();
				p.getEquipment().setArmorContents(null);
				p.updateInventory();
				showMenu(p);
				
				for (PotionEffect effect : p.getActivePotionEffects()) {
                    p.removePotionEffect(effect.getType());
				}

			}
		}, 1L);
		
		spectators.add(p.getName());
		msgmgr.sendMessage(PrefixType.INFO, "You are now spectating! Use /sg spectate again to return to the lobby.", p);
		msgmgr.sendMessage(PrefixType.INFO, "Right click while holding shift to teleport to the next ingame player, left click to go back.", p);
		nextspec.put(p, 0);
	}

	public void removeSpectator(Player p) {
		ArrayList < Player > players = new ArrayList < Player > ();
		players.addAll(activePlayers);
		players.addAll(inactivePlayers);

		if(p.isOnline()){
			for (Player pl: Bukkit.getOnlinePlayers()) {
				pl.showPlayer(p);
			}
		}
		//restoreInv(p);
		p.setAllowFlight(false);
		p.setFlying(false);
		p.setFallDistance(0);
		p.setHealth(p.getMaxHealth());
		p.setFoodLevel(20);
		p.setSaturation(20);
		p.teleport(SettingsManager.getInstance().getLobbySpawn());
		// Bukkit.getServer().broadcastPrefixType("Removing Spec "+p.getName()+" "+spectators.size()+" left");
		spectators.remove(p.getName());
		// Bukkit.getServer().broadcastPrefixType("Removed");

		nextspec.remove(p);
	}

	public void clearSpecs() {

		for (int a = 0; a < spectators.size(); a = 0) {
			removeSpectator(Bukkit.getPlayerExact(spectators.get(0)));
		}
		spectators.clear();
		nextspec.clear();
	}


	public HashMap < Player, Integer > getNextSpec() {
		return nextspec;
	}

	/*
	@SuppressWarnings("deprecation")
	public void restoreInv(Player p) {
		try {
			clearInv(p);
			p.getInventory().setContents(inv_store.get(p)[0]);
			p.getInventory().setArmorContents(inv_store.get(p)[1]);
			inv_store.remove(p);
			p.updateInventory();
		} catch (Exception e) {
			//p.sendMessage(ChatColor.RED+"Inentory failed to restore or nothing was in it.");
		}
	}
	*/

	/*
	@SuppressWarnings("deprecation")
	public void clearInv(Player p) {
		ItemStack[] inv = p.getInventory().getContents();
		for (int i = 0; i < inv.length; i++) {
			inv[i] = null;
		}
		p.getInventory().setContents(inv);
		inv = p.getInventory().getArmorContents();
		for (int i = 0; i < inv.length; i++) {
			inv[i] = null;
		}
		p.getInventory().setArmorContents(inv);
		p.updateInventory();
	}
	*/

	class NightChecker implements Runnable {
		boolean reset = false;
		int tgc = gcount;
		public void run() {
			if (SettingsManager.getGameWorld(gameID).getTime() > 14000) {
				for (Player pl: activePlayers) {
					msgmgr.sendMessage(PrefixType.INFO, "Chests restocked!", pl);
				}
				GameManager.openedChest.get(gameID).clear();
				reset = true;
			}

		}
	}

	class EndgameManager implements Runnable {
		@Override
		public void run() {
			for (Player player: activePlayers.toArray(new Player[0])) {
				Location l = player.getLocation();
				l.add(0, 5, 0);
				player.getWorld().strikeLightningEffect(l);
			}

		}
	}

	class DeathMatchTimer implements Runnable {
		public void run() {
			int now = (int) (new Date().getTime() / 1000);
			long length = config.getInt("deathmatch.time") * 60;
			long remaining = (length - (now - (startTime / 1000)));
			//SurvivalGames.$("Remaining: " + remaining + " (" + now + " / " + length + " / " + (startTime / 1000) + ")");

			// Every 3 minutes or every minute in the last 3 minutes
			if (((remaining % 180) == 0) || (((remaining % 60) == 0) && (remaining <= 180))) {
				if (remaining > 0) {
					msgFall(PrefixType.INFO, "game.deathmatchwarning", "t-" + (remaining / 60));
				}
			}
			
			// Death match time!!
			if (remaining > 0) return;
			debug("DeathMatch mode starting!");
			
			Bukkit.getScheduler().cancelTask(dmTaskID);
			if (!tasks.remove((Integer) dmTaskID)) {
				SurvivalGames.$("WARNING: DeathMatch task NOT removed!");
			}

			// Teleport everyone to their original spawn point
			for(Map.Entry<Integer, Player> entry : spawns.entrySet()) {
				Player p = entry.getValue();
				Integer a = entry.getKey();
				if (activePlayers.contains(p) && p.isOnline() && !p.isDead()) {
					debug("Teleporting " + p.getName() + " (spawn " + a + ")");
					p.teleport(SettingsManager.getInstance().getSpawnPoint(gameID, a).add(0, 1.5, 0));
					p.sendMessage(ChatColor.RED + "DeathMatch mode has begun!! Attack!!");
				}
			}

			tasks.add(Bukkit.getScheduler().scheduleSyncRepeatingTask(GameManager.getInstance().getPlugin(), new Runnable() {
				public void run() {
					// Game could end (or players die) while inside this loop
					// This must be carefully handled so we dont CME or damage a player that has already left the game
					ArrayList <Player> players = new ArrayList<Player>(activePlayers);
					for(Player p: players) {
						// Verify they are still "alive" and still in the game
						if ((mode == GameMode.INGAME) && (p != null) && (!p.isDead()) && (activePlayers.contains(p))) {
							// Player out of arena or too high (towering to avoid players)
							int ydiff = Math.abs(dmspawn.getBlockY() - p.getLocation().getBlockY());
							double dist = dmspawn.distance(p.getLocation());
							if ((dist > dmradius) || (ydiff > 4)) {
								p.sendMessage(ChatColor.RED + "Return to the death match area!");
								p.getLocation().getWorld().strikeLightningEffect(p.getLocation());
								p.damage(5);
								p.setFireTicks(60);
							}
						}
					}
				}
			}, 10 * 20L, config.getInt("deathmatch.killtime") * 20));
		}
	}

	public boolean isBlockInArena(Location v) {
		return arena.containsBlock(v);
	}

	public boolean isProtectionOn() {
		long t = startTime / 1000;
		long l = config.getLong("grace-period");
		long d = new Date().getTime() / 1000;
		if ((d - t) < l) return true;
		return false;
	}

	public int getID() {
		return gameID;
	}

	public int getActivePlayers() {
		return activePlayers.size();
	}

	public int getInactivePlayers() {
		return inactivePlayers.size();
	}

	public Player[][] getPlayers() {
		return new Player[][] {
				activePlayers.toArray(new Player[0]), inactivePlayers.toArray(new Player[0])
		};
	}

	public ArrayList < Player > getAllPlayers() {
		ArrayList < Player > all = new ArrayList < Player > ();
		all.addAll(activePlayers);
		all.addAll(inactivePlayers);
		return all;
	}
	
	public GameScoreboard getScoreboard() {
		return scoreBoard;
	}

	public boolean isSpectator(Player p) {
		return spectators.contains(p.getName());
	}

	public boolean isInQueue(Player p) {
		return queue.contains(p);
	}

	public boolean isPlayerActive(Player player) {
		return activePlayers.contains(player);
	}
	public boolean isPlayerinactive(Player player) {
		return inactivePlayers.contains(player);
	}
	public boolean hasPlayer(Player p) {
		return activePlayers.contains(p) || inactivePlayers.contains(p);
	}
	public GameMode getMode() {
		return mode;
	}

	public synchronized void setRBPercent(double d) {
		rbpercent = d;
	}

	public double getRBPercent() {
		return rbpercent;
	}

	public void setRBStatus(String s) {
		rbstatus = s;
	}

	public String getRBStatus() {
		return rbstatus;
	}

	public String getName() {
		return name;
	}

	public void msgFall(PrefixType type, String msg, String...vars){
		for(Player p: activePlayers) {
			msgmgr.sendFMessage(type, msg, p, vars);
		}
		for(String ps: spectators) {
			Player p = Bukkit.getServer().getPlayer(ps);
			if (p != null) {
				msgmgr.sendFMessage(type, msg, p, vars);
			}
		}
	}

	public static ChatColor GetColorPrefix(GameMode gameMode) {

		if (gameMode == GameMode.DISABLED)
			return ChatColor.RED;
		if (gameMode == GameMode.ERROR)
			return ChatColor.DARK_RED;
		if (gameMode == GameMode.FINISHING)
			return ChatColor.DARK_PURPLE;
		if (gameMode == GameMode.WAITING)
			return ChatColor.GOLD;
		if (gameMode == GameMode.INGAME)
			return ChatColor.DARK_GREEN;
		if (gameMode == GameMode.STARTING)
			return ChatColor.GREEN;
		if (gameMode == GameMode.RESETING)
			return ChatColor.DARK_AQUA;
		if (gameMode == GameMode.LOADING)
			return ChatColor.BLUE;
		if (gameMode == GameMode.INACTIVE)
			return ChatColor.DARK_GRAY;

		return ChatColor.WHITE;
	}
}
