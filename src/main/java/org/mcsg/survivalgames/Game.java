package org.mcsg.survivalgames;

import java.util.*;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.mcsg.survivalgames.MessageManager.PrefixType;
import org.mcsg.survivalgames.api.PlayerJoinArenaEvent;
import org.mcsg.survivalgames.api.PlayerKilledEvent;
import org.mcsg.survivalgames.api.PlayerWinEvent;
import org.mcsg.survivalgames.hooks.HookManager;
import org.mcsg.survivalgames.logging.QueueManager;
import org.mcsg.survivalgames.stats.StatsManager;
import org.mcsg.survivalgames.util.ItemReader;
import org.mcsg.survivalgames.util.Kit;

//Data container for a game

public class Game {
    
    private final ArrayList<Player> activePlayers = new ArrayList<>();
    private final ArrayList<Player> inactivePlayers = new ArrayList<>();
    private final ArrayList<String> spectators = new ArrayList<>();
    private final ArrayList<Player> queue = new ArrayList<>();
    private final ArrayList<Integer> tasks = new ArrayList<>();
    private final List<Kit> kits = new ArrayList<>();
    private final int gameID;
    private final HashMap<Integer, Player> spawns = new HashMap<>();
    private final int endgameTaskID;
    private final StatsManager sm = StatsManager.getInstance();
    private final HashMap<String, String> hookvars = new HashMap<>();
    private final MessageManager msgmgr = MessageManager.getInstance();
    HashMap<Player, Integer> nextspec = new HashMap<>();
    ArrayList<Player> voted = new ArrayList<>();
    int count = 20;
    int tid;
    private GameMode mode = GameMode.DISABLED;
    private HashMap<String, Object> flags = new HashMap<>();
    private Arena arena;
    private String name;
    private Location dmspawn;
    private Location winloc;
    private int dmradius;
    private int gcount;
    private FileConfiguration config;
    private FileConfiguration system;
    private int spawnCount;
    private int vote;
    private boolean disabled;
    private int dmTaskID;
    private boolean endgameRunning;
    private double rbpercent;
    private String rbstatus = "";
    private long startTime;
    private boolean countdownRunning;
    private GameScoreboard scoreBoard;
    
    public Game(final int gameid) {
        this.gameID = gameid;
        this.name = "Arena " + this.gameID;
        this.reloadConfig();
        this.setup();
    }
    
    public void reloadConfig() {
        this.config = SettingsManager.getInstance().getConfig();
        this.system = SettingsManager.getInstance().getSystemConfig();
    }
    
    public void setup() {
        this.mode = GameMode.LOADING;
        final int x = this.system.getInt("sg-system.arenas." + this.gameID + ".x1");
        final int y = this.system.getInt("sg-system.arenas." + this.gameID + ".y1");
        final int z = this.system.getInt("sg-system.arenas." + this.gameID + ".z1");
        
        final int x1 = this.system.getInt("sg-system.arenas." + this.gameID + ".x2");
        final int y1 = this.system.getInt("sg-system.arenas." + this.gameID + ".y2");
        final int z1 = this.system.getInt("sg-system.arenas." + this.gameID + ".z2");
        
        final Location max = new Location(SettingsManager.getGameWorld(this.gameID), Math.max(x, x1), Math.max(y, y1), Math.max(z, z1));
        final Location min = new Location(SettingsManager.getGameWorld(this.gameID), Math.min(x, x1), Math.min(y, y1), Math.min(z, z1));
        
        this.name = this.system.getString("sg-system.arenas." + this.gameID + ".name", this.name);
        
        this.arena = new Arena(min, max);
        
        final double dmx = this.system.getDouble("sg-system.arenas." + this.gameID + ".deathmatch.x", 0);
        final double dmy = this.system.getDouble("sg-system.arenas." + this.gameID + ".deathmatch.y", 65);
        final double dmz = this.system.getDouble("sg-system.arenas." + this.gameID + ".deathmatch.z", 0);
        this.dmspawn = new Location(SettingsManager.getGameWorld(this.gameID), dmx, dmy, dmz);
        
        this.dmradius = this.system.getInt("sg-system.arenas." + this.gameID + ".deathmatch.radius", 26);
        
        final String winw = this.system.getString("sg-system.arenas." + this.gameID + ".win.world", "games");
        final double winx = this.system.getDouble("sg-system.arenas." + this.gameID + ".win.x", 0);
        final double winy = this.system.getDouble("sg-system.arenas." + this.gameID + ".win.y", 65);
        final double winz = this.system.getDouble("sg-system.arenas." + this.gameID + ".win.z", 0);
        final float winyaw = this.system.getInt("sg-system.arenas." + this.gameID + ".win.yaw", 0);
        final float winp = this.system.getInt("sg-system.arenas." + this.gameID + ".win.pitch", 0);
        this.winloc = new Location(Bukkit.getWorld(winw), winx, winy, winz, winyaw, winp);
        final List<String> kitNames = this.system.getStringList("sg-system.arenas." + this.gameID + ".kits");
        if (!kitNames.isEmpty()) {
            for (final String kit : kitNames) {
                for (final Kit k : GameManager.getInstance().kits) {
                    if (k.getName().equals(kit)) {
                        this.kits.add(k);
                    }
                }
            }
        } else {
            this.kits.addAll(GameManager.getInstance().kits);
        }
        this.loadspawns();
        
        this.hookvars.put("arena", this.gameID + "");
        this.hookvars.put("maxplayers", this.spawnCount + "");
        this.hookvars.put("activeplayers", "0");
        
        this.mode = GameMode.WAITING;
        
        this.scoreBoard = new GameScoreboard(this.gameID);
    }
    
    public void loadspawns() {
        for (int a = 1; a <= SettingsManager.getInstance().getSpawnCount(this.gameID); a++) {
            this.spawns.put(a, null);
            this.spawnCount = a;
        }
    }
    
    public static ChatColor GetColorPrefix(final GameMode gameMode) {
        
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
    
    public void reloadFlags() {
        this.flags = SettingsManager.getInstance().getGameFlags(this.gameID);
    }
    
    public void saveFlags() {
        SettingsManager.getInstance().saveGameFlags(this.flags, this.gameID);
    }
    
    public void addSpawn() {
        this.spawnCount++;
        this.spawns.put(this.spawnCount, null);
    }
    
    public GameMode getGameMode() {
        return this.mode;
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
    
    public Arena getArena() {
        return this.arena;
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
    
    public List<Kit> getKits() {
        return this.kits;
    }
    
    public void removeFromQueue(final Player p) {
        this.queue.remove(p);
    }
    
    public void vote(final Player pl) {
        
        
        if (GameMode.STARTING == this.mode) {
            this.msgmgr.sendMessage(PrefixType.WARNING, "Game already starting!", pl);
            return;
        }
        if (GameMode.WAITING != this.mode) {
            this.msgmgr.sendMessage(PrefixType.WARNING, "Game already started!", pl);
            return;
        }
        if (this.voted.contains(pl)) {
            this.msgmgr.sendMessage(PrefixType.WARNING, "You already voted!", pl);
            return;
        }
        this.vote++;
        this.voted.add(pl);
        this.msgFall(PrefixType.INFO, "game.playervote", "player-" + pl.getDisplayName());
        HookManager.getInstance().runHook("PLAYER_VOTE", "player-" + pl.getName());
        this.scoreBoard.playerLiving(pl);
        /*for(Player p: activePlayers){
            p.sendMessage(ChatColor.AQUA+pl.getName()+" Voted to start the game! "+ Math.round((vote +0.0) / ((getActivePlayers() +0.0)*100)) +"/"+((c.getInt("auto-start-vote")+0.0))+"%");
        }*/
        // Bukkit.getServer().broadcastPrefixType((vote +0.0) / (getActivePlayers() +0.0) +"% voted, needs "+(c.getInt("auto-start-vote")+0.0)/100);
        if ((this.vote + 0.0) / (this.getActivePlayers() + 0.0) >= (this.config.getInt("auto-start-vote") + 0.0) / 100 && this.getActivePlayers() > 1) {
            this.countdown(this.config.getInt("auto-start-time"));
            for (final Player p : this.activePlayers) {
                //p.sendMessage(ChatColor.LIGHT_PURPLE + "Game Starting in " + c.getInt("auto-start-time"));
                this.msgmgr.sendMessage(PrefixType.INFO, "Game starting in " + this.config.getInt("auto-start-time") + "!", p);
                this.scoreBoard.playerLiving(pl);
            }
        }
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
    
    public void msgFall(final PrefixType type, final String msg, final String... vars) {
        for (final Player p : this.activePlayers) {
            this.msgmgr.sendFMessage(type, msg, p, vars);
        }
        for (final String ps : this.spectators) {
            final Player p = Bukkit.getServer().getPlayer(ps);
            if (p != null) {
                this.msgmgr.sendFMessage(type, msg, p, vars);
            }
        }
    }
    
    public int getActivePlayers() {
        return this.activePlayers.size();
    }
    
    public void countdown(final int time) {
        //Bukkit.broadcastMessage(""+time);
        MessageManager.getInstance().broadcastFMessage(PrefixType.INFO, "broadcast.gamestarting", "arena-" + this.gameID, "t-" + time, "arenaname-" + this.name);
        this.countdownRunning = true;
        this.count = time;
        Bukkit.getScheduler().cancelTask(this.tid);
        
        if (this.mode == GameMode.WAITING || this.mode == GameMode.STARTING) {
            this.mode = GameMode.STARTING;
            this.tid = Bukkit.getScheduler().scheduleSyncRepeatingTask(GameManager.getInstance().getPlugin(), () -> {
                // Fail safe to stop timer if game is ended or not in correct state
                if (this.mode != GameMode.STARTING) {
                    Bukkit.getScheduler().cancelTask(this.tid);
                    return;
                }
                
                if (this.count > 0) {
                    if (this.count % 10 == 0) {
                        this.msgFall(PrefixType.INFO, "game.countdown", "t-" + this.count);
                    }
                    if (this.count < 6) {
                        this.msgFall(PrefixType.INFO, "game.countdown", "t-" + this.count);
                        
                    }
                    this.count--;
                    LobbyManager.getInstance().updateWall(this.gameID);
                } else {
                    this.startGame();
                    Bukkit.getScheduler().cancelTask(this.tid);
                    this.countdownRunning = false;
                }
            }, 0, 20);
            
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
        if (this.mode == GameMode.INGAME) {
            return;
        }
        
        if (this.activePlayers.size() < 2) {
            for (final Player pl : this.activePlayers) {
                this.msgmgr.sendMessage(PrefixType.WARNING, "Not enough players!", pl);
                this.mode = GameMode.WAITING;
                LobbyManager.getInstance().updateWall(this.gameID);
            }
            return;
        } else {
            // Remove all entities in the world
            for (final Entity entity : this.arena.getMax().getWorld().getEntities()) {
                if (entity instanceof Player) continue;
                entity.remove();
            }
            this.startTime = new Date().getTime();
            for (final Player pl : this.activePlayers) {
                pl.setHealth(pl.getAttribute(Attribute.GENERIC_MAX_HEALTH).getDefaultValue());
                this.msgmgr.sendFMessage(PrefixType.INFO, "game.goodluck", pl);
                this.scoreBoard.playerLiving(pl);
            }
            if (this.config.getBoolean("restock-chest")) {
                SettingsManager.getGameWorld(this.gameID).setTime(0);
                this.gcount++;
                this.tasks.add(Bukkit.getScheduler().scheduleSyncDelayedTask(GameManager.getInstance().getPlugin(),
                        new NightChecker(),
                        14400));
            }
            if (this.config.getInt("grace-period") != 0) {
                for (final Player play : this.activePlayers) {
                    this.msgmgr.sendMessage(PrefixType.INFO, "You have a " + this.config.getInt("grace-period") + " second grace period!", play);
                }
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(GameManager.getInstance().getPlugin(), () -> {
                    for (final Player play : this.activePlayers) {
                        this.msgmgr.sendMessage(PrefixType.INFO, "Grace period has ended!", play);
                    }
                }, this.config.getInt("grace-period") * 20);
            }
            if (this.config.getBoolean("deathmatch.enabled")) {
                SurvivalGames.$(this.gameID, "Launching deathmatch timer...");
                this.dmTaskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(GameManager.getInstance().getPlugin(), new DeathMatchTimer(), 40L, 20L);
                this.tasks.add(this.dmTaskID);
            }
        }
        
        this.mode = GameMode.INGAME;
        LobbyManager.getInstance().updateWall(this.gameID);
        MessageManager.getInstance().broadcastFMessage(PrefixType.INFO, "broadcast.gamestarted", "arena-" + this.gameID, "arenaname-" + this.name);
        
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
        return this.count;
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
    public void playerDeath(final PlayerDeathEvent e) {
        final Player p = e.getEntity();
        if (!this.activePlayers.contains(p)) return;
        
        this.sm.playerDied(p, this.activePlayers.size(), this.gameID, new Date().getTime() - this.startTime);
        this.scoreBoard.playerDead(p);
        this.activePlayers.remove(p);
        this.inactivePlayers.add(p);
        for (final Object in : this.spawns.keySet().toArray()) {
            if (this.spawns.get(in) == p) this.spawns.remove(in);
        }
        
        PlayerKilledEvent pk = null;
        if (this.mode != GameMode.WAITING && p.getLastDamageCause() != null && p.getLastDamageCause().getCause() != null) {
            final DamageCause cause = p.getLastDamageCause().getCause();
            switch (cause) {
                case ENTITY_ATTACK:
                    if (p.getLastDamageCause().getEntityType() == EntityType.PLAYER) {
                        final EntityType enttype = p.getLastDamageCause().getEntityType();
                        final Player killer = p.getKiller();
                        String killername = "Unknown";
                        
                        if (killer != null) {
                            killername = killer.getDisplayName();
                        }
                        
                        String itemname = "Unknown Item";
                        if (killer != null) {
                            itemname = ItemReader.getFriendlyItemName(killer.getEquipment().getItemInMainHand().getType());
                        }
                        
                        this.msgFall(PrefixType.INFO, "death." + enttype, "player-" + p.getDisplayName(), "killer-" + killername, "item-" + itemname);
                        
                        if (killer != null && p != null) {
                            this.sm.addKill(killer, p, this.gameID, this.name);
                            this.scoreBoard.incScore(killer);
                        }
                        pk = new PlayerKilledEvent(p, this, killer, cause);
                    } else {
                        this.msgFall(PrefixType.INFO, "death." + p.getLastDamageCause().getEntityType(),
                                "player-" + p.getDisplayName(),
                                "killer-" + p.getLastDamageCause().getEntityType());
                        pk = new PlayerKilledEvent(p, this, null, cause);
                    }
                    break;
                default:
                    this.msgFall(PrefixType.INFO, "death." + cause.name(),
                            "player-" + p.getDisplayName(),
                            "killer-" + cause);
                    pk = new PlayerKilledEvent(p, this, null, cause);
                    
                    break;
            }
            Bukkit.getServer().getPluginManager().callEvent(pk);
            
            if (this.getActivePlayers() > 1) {
                for (final Player pl : this.getAllPlayers()) {
                    this.msgmgr.sendMessage(PrefixType.INFO, ChatColor.DARK_AQUA + "There are " + ChatColor.YELLOW + ""
                            + this.getActivePlayers() + ChatColor.DARK_AQUA + " players remaining!", pl);
                }
            }
        }
        
        for (final Player pe : this.activePlayers) {
            final Location l = pe.getLocation();
            l.setY(l.getWorld().getMaxHeight());
            l.getWorld().strikeLightningEffect(l);
        }
        
        if (this.getActivePlayers() <= this.config.getInt("endgame.players") && this.config.getBoolean("endgame.fire-lighting.enabled") && !this.endgameRunning) {
            
            this.tasks.add(Bukkit.getScheduler().scheduleSyncRepeatingTask(GameManager.getInstance().getPlugin(),
                    new EndgameManager(),
                    0,
                    this.config.getInt("endgame.fire-lighting.interval") * 20));
        }
        
        if (this.activePlayers.size() < 2 && this.mode == GameMode.INGAME) {
            this.mode = GameMode.FINISHING;
            LobbyManager.getInstance().updateWall(this.gameID);
            this.tasks.add(Bukkit.getScheduler().scheduleSyncDelayedTask(GameManager.getInstance().getPlugin(), () -> {
                this.playerWin(p);
                this.endGame();
            }, 10L));
        }
        LobbyManager.getInstance().updateWall(this.gameID);
        this.sm.removePlayer(p, this.gameID);
    }
    
    public ArrayList<Player> getAllPlayers() {
        final ArrayList<Player> all = new ArrayList<>();
        all.addAll(this.activePlayers);
        all.addAll(this.inactivePlayers);
        return all;
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
    public void playerWin(final Player p) {
        if (GameMode.DISABLED == this.mode) return;
        
        if (this.activePlayers.size() == 0) {
            // No players left means this is the winner dying, just ignore it.
            // The actual win task would have already been launched before this one.
            SurvivalGames.$(this.gameID, Level.WARNING, "Last player (" + p.getName() + ") died in the arena!");
            return;
        }
        
        final Player win = this.activePlayers.get(0);
        final Inventory inv = p.getInventory();
        inv.clear();
        p.getInventory().setHeldItemSlot(0);
        p.getEquipment().setArmorContents(null);
        p.updateInventory();
        // clearInv(p);
        win.teleport(this.winloc);
        //restoreInv(win);
        this.scoreBoard.removePlayer(p);
        
        final String msg = this.msgmgr.getFMessage(PrefixType.INFO, "game.playerwin", "arena-" + this.gameID, "victim-" + p.getDisplayName(), "player-" + win.getDisplayName(), "arenaname-" + this.name);
        final PlayerWinEvent ev = new PlayerWinEvent(this, win, p, msg);
        Bukkit.getServer().getPluginManager().callEvent(ev);
        
        if (SettingsManager.getInstance().getMessageConfig().getBoolean("messages.game.playerwin_enabled", true)) {
            if (ev.getMessage() != null && !ev.getMessage().isEmpty()) {
                Bukkit.broadcastMessage(ev.getMessage());
            }
        }
        
        this.mode = GameMode.FINISHING;
        LobbyManager.getInstance().updateWall(this.gameID);
        LobbyManager.getInstance().gameEnd(this.gameID, win);
        
        win.setHealth(p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getDefaultValue());
        win.setFoodLevel(20);
        win.setFireTicks(0);
        win.setFallDistance(0);
        
        this.sm.playerWin(win, this.gameID, new Date().getTime() - this.startTime);
        this.sm.saveGame(this.gameID, win, this.getActivePlayers() + this.getInactivePlayers(), new Date().getTime() - this.startTime);
        this.sm.removePlayer(win, this.gameID);
        
        this.loadspawns();
        LobbyManager.getInstance().updateWall(this.gameID);
        MessageManager.getInstance().broadcastFMessage(PrefixType.INFO, "broadcast.gameend", "arena-" + this.gameID, "arenaname-" + this.name);
        
        // Remove all entities in the world
        for (final Entity entity : this.arena.getMax().getWorld().getEntities()) {
            if (entity instanceof Player) continue;
            entity.remove();
        }
    }
    
    public void endGame() {
        this.mode = GameMode.WAITING;
        this.resetArena();
        LobbyManager.getInstance().updateWall(this.gameID);
    }
    
    public int getInactivePlayers() {
        return this.inactivePlayers.size();
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
        
        for (final Integer i : this.tasks) {
            Bukkit.getScheduler().cancelTask(i);
        }
        
        this.tasks.clear();
        this.vote = 0;
        this.voted.clear();
        this.activePlayers.clear();
        this.inactivePlayers.clear();
        this.spawns.clear();
        this.clearSpecs();
        
        this.mode = GameMode.RESETING;
        this.endgameRunning = false;
        
        Bukkit.getScheduler().cancelTask(this.endgameTaskID);
        GameManager.getInstance().gameEndCallBack(this.gameID);
        QueueManager.getInstance().rollback(this.gameID, false);
        LobbyManager.getInstance().updateWall(this.gameID);
        
        this.scoreBoard.reset();
        
    }
    
    public void clearSpecs() {
        
        for (int a = 0; a < this.spectators.size(); a = 0) {
            this.removeSpectator(Bukkit.getPlayerExact(this.spectators.get(0)));
        }
        this.spectators.clear();
        this.nextspec.clear();
    }
    
    public void removeSpectator(final Player p) {
        final ArrayList<Player> players = new ArrayList<>();
        players.addAll(this.activePlayers);
        players.addAll(this.inactivePlayers);
        
        if (p.isOnline()) {
            for (final Player pl : Bukkit.getOnlinePlayers()) {
                pl.showPlayer(SurvivalGames.plugin, p);
            }
        }
        //restoreInv(p);
        p.setFlying(false);
        p.setAllowFlight(false);
        p.setFallDistance(0);
        p.setHealth(p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getDefaultValue());
        p.setFoodLevel(20);
        p.setSaturation(20);
        p.setGameMode(org.bukkit.GameMode.SURVIVAL);
        this.scoreBoard.removeScoreboard(p);
        final Inventory inv = p.getInventory();
        inv.clear();
        p.getInventory().setHeldItemSlot(0);
        p.getEquipment().setArmorContents(null);
        p.updateInventory();
        p.teleport(SettingsManager.getInstance().getLobbySpawn());
        p.setGameMode(org.bukkit.GameMode.SURVIVAL);
        p.setWalkSpeed(0.2F);
        p.setFlySpeed(0.2F);
        p.setCollidable(true);
        this.spectators.remove(p.getName());
        this.nextspec.remove(p);
        this.msgFall(PrefixType.INFO, "game.spectatorleave", "player-" + p.getDisplayName(), "spectators-" + this.spectators.size());
    }
    
    public void playerLeave(final Player p, final boolean teleport) {
        this.msgFall(PrefixType.INFO, "game.playerleavegame", "player-" + p.getDisplayName());
        final Player win = this.activePlayers.get(0);
        final Inventory inv = p.getInventory();
        inv.clear();
        p.getInventory().setHeldItemSlot(0);
        p.getEquipment().setArmorContents(null);
        p.updateInventory();
        if (teleport) {
            p.teleport(SettingsManager.getInstance().getLobbySpawn());
        }
        // Remove any potion/fire effects
        for (final PotionEffect effect : p.getActivePotionEffects()) {
            p.removePotionEffect(effect.getType());
        }
        if (p.getFireTicks() > 0) {
            p.setFireTicks(0);
        }
        
        this.sm.removePlayer(p, this.gameID);
        this.scoreBoard.removePlayer(p);
        this.activePlayers.remove(p);
        this.inactivePlayers.remove(p);
        this.voted.remove(p);
        
        for (final Object in : this.spawns.keySet().toArray()) {
            if (this.spawns.get(in) == p) this.spawns.remove(in);
        }
        
        HookManager.getInstance().runHook("PLAYER_REMOVED", "player-" + p.getName());
        LobbyManager.getInstance().updateWall(this.gameID);
        
        if (this.activePlayers.size() < 2) {
            if (this.mode == GameMode.INGAME) {
                this.mode = GameMode.FINISHING;
                this.tasks.add(Bukkit.getScheduler().scheduleSyncDelayedTask(GameManager.getInstance().getPlugin(), () -> {
                    this.playerWin(p);
                    this.endGame();
                }, 1L));
            } else if (this.mode == GameMode.STARTING) {
                if (this.activePlayers.size() == 1) {
                    // Only one player remaining, cancel timer and tell the player
                    this.mode = GameMode.WAITING;
                    final Player l = this.activePlayers.get(0);
                    LobbyManager.getInstance().updateWall(this.gameID);
                } else {
                    // No players left so just end the game
                    this.mode = GameMode.FINISHING;
                    this.tasks.add(Bukkit.getScheduler().scheduleSyncDelayedTask(GameManager.getInstance().getPlugin(), this::endGame, 1L));
                }
            }
        }
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
        this.disabled = true;
        this.spawns.clear();
        this.scoreBoard.reset();
        
        for (int a = 0; a < this.activePlayers.size(); a = 0) {
            try {
                
                final Player p = this.activePlayers.get(a);
                this.msgmgr.sendMessage(PrefixType.WARNING, "Game disabled!", p);
                this.playerLeave(p, true);
            } catch (final Exception ignored) {
            }
            
        }
        
        for (int a = 0; a < this.inactivePlayers.size(); a = 0) {
            try {
                
                final Player p = this.inactivePlayers.remove(a);
                this.msgmgr.sendMessage(PrefixType.WARNING, "Game disabled!", p);
            } catch (final Exception ignored) {
            }
            
        }
        
        this.clearSpecs();
        this.queue.clear();
        
        this.endGame();
        LobbyManager.getInstance().updateWall(this.gameID);
        MessageManager.getInstance().broadcastFMessage(PrefixType.INFO, "broadcast.gamedisabled", "arena-" + this.gameID, "arenaname-" + this.name);
        
    }
    
    public void addSpectator(final Player p) {
        if (this.mode != GameMode.INGAME) {
            this.msgmgr.sendMessage(PrefixType.WARNING, "You can only spectate running games!", p);
            return;
        }
        
        p.teleport(SettingsManager.getInstance().getSpawnPoint(this.gameID, 1).add(0, 10, 0));
        p.setNoDamageTicks(40);
        
        HookManager.getInstance().runHook("PLAYER_SPECTATE", "player-" + p.getName());
        
        for (final Player pl : Bukkit.getOnlinePlayers()) {
            pl.hidePlayer(SurvivalGames.plugin, p);
        }
        
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(GameManager.getInstance().getPlugin(), () -> {
            p.setGameMode(org.bukkit.GameMode.CREATIVE);
            p.setAllowFlight(true);
            p.setFlying(true);
            p.setWalkSpeed(0.3F);
            p.setFlySpeed(0.3F);
            p.setFireTicks(0);
            p.setCollidable(false);
            
            final Inventory inv = p.getInventory();
            inv.clear();
            p.getInventory().setHeldItemSlot(0);
            inv.setItem(0, SettingsManager.getInstance().getSpecItemNext());
            inv.setItem(1, SettingsManager.getInstance().getSpecItemPrev());
            inv.setItem(2, SettingsManager.getInstance().getSpecItemExit());
            p.getEquipment().setArmorContents(null);
            p.updateInventory();
            
            for (final PotionEffect effect : p.getActivePotionEffects()) {
                p.removePotionEffect(effect.getType());
            }
            
            this.scoreBoard.addScoreboard(p);
        }, 10L);
        
        this.msgFall(PrefixType.INFO, "game.spectatorjoin", "player-" + p.getDisplayName(), "spectators-" + (this.spectators.size() + 1));
        
        this.spectators.add(p.getName());
        
        this.msgmgr.sendMessage(PrefixType.INFO, "You are now spectating the game!.", p);
        this.msgmgr.sendMessage(PrefixType.INFO, "Use the items in your quickbar to control spectating.", p);
        this.nextspec.put(p, 0);
    }
    
    public void resetCallback() {
        if (!this.disabled) {
            this.enable();
        } else this.mode = GameMode.DISABLED;
        LobbyManager.getInstance().updateWall(this.gameID);
    }
    
    public void enable() {
        this.mode = GameMode.WAITING;
        if (this.disabled) {
            MessageManager.getInstance().broadcastFMessage(PrefixType.INFO, "broadcast.gameenabled", "arena-" + this.gameID, "arenaname-" + this.name);
        }
        this.disabled = false;
        final int b = SettingsManager.getInstance().getSpawnCount(this.gameID) > this.queue.size() ? this.queue.size() : SettingsManager.getInstance().getSpawnCount(this.gameID);
        for (int a = 0; a < b; a++) {
            this.addPlayer(this.queue.remove(0));
        }
        int c = 1;
        for (final Player p : this.queue) {
            this.msgmgr.sendMessage(PrefixType.INFO, "You are now #" + c + " in line for arena " + this.gameID, p);
            c++;
        }
        
        LobbyManager.getInstance().updateWall(this.gameID);
        
        MessageManager.getInstance().broadcastFMessage(PrefixType.INFO, "broadcast.gamewaiting", "arena-" + this.gameID, "arenaname-" + this.name);
        
        this.scoreBoard.reset();
        
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
    
    @SuppressWarnings("deprecation")
    public boolean addPlayer(final Player p) {
        if (SettingsManager.getInstance().getLobbySpawn() == null) {
            this.msgmgr.sendFMessage(PrefixType.WARNING, "error.nolobbyspawn", p);
            return false;
        }
        if (!p.hasPermission("sg.arena.join." + this.gameID)) {
            SurvivalGames.debug(this.gameID, "permission needed to join arena: " + "sg.arena.join." + this.gameID);
            this.msgmgr.sendFMessage(PrefixType.WARNING, "game.nopermission", p, "arena-" + this.gameID);
            return false;
        }
        HookManager.getInstance().runHook("GAME_PRE_ADDPLAYER", "arena-" + this.gameID, "player-" + p.getDisplayName(), "maxplayers-" + this.spawns.size(), "players-" + this.activePlayers.size());
        
        GameManager.getInstance().removeFromOtherQueues(p, this.gameID);
        
        if (GameManager.getInstance().getPlayerGameId(p) != -1) {
            if (GameManager.getInstance().isPlayerActive(p)) {
                this.msgmgr.sendMessage(PrefixType.ERROR, "Cannot join multiple games!", p);
                return false;
            }
        }
        if (p.isInsideVehicle()) {
            p.leaveVehicle();
        }
        if (this.spectators.contains(p)) this.removeSpectator(p);
        if (this.mode == GameMode.WAITING || this.mode == GameMode.STARTING) {
            if (this.activePlayers.size() < SettingsManager.getInstance().getSpawnCount(this.gameID)) {
                this.msgmgr.sendMessage(PrefixType.INFO, "Joining Arena '" + this.name + "'", p);
                final PlayerJoinArenaEvent joinarena = new PlayerJoinArenaEvent(p, GameManager.getInstance().getGame(this.gameID));
                Bukkit.getServer().getPluginManager().callEvent(joinarena);
                if (joinarena.isCancelled()) return false;
                boolean placed = false;
                final int spawnCount = SettingsManager.getInstance().getSpawnCount(this.gameID);
                
                for (int a = 1; a <= spawnCount; a++) {
                    if (this.spawns.get(a) == null) {
                        placed = true;
                        this.spawns.put(a, p);
                        p.setGameMode(org.bukkit.GameMode.SURVIVAL);
                        
                        //p.teleport(SettingsManager.getInstance().getLobbySpawn());
                        p.teleport(SettingsManager.getInstance().getSpawnPoint(this.gameID, a));
                        
                        p.setHealth(p.getMaxHealth());
                        p.setFoodLevel(20);
                        p.getInventory().clear();
                        p.getEquipment().setArmorContents(null);
                        p.updateInventory();
                        
                        p.setFlying(false);
                        p.setAllowFlight(false);
                        p.setWalkSpeed(0.2F);
                        p.setFireTicks(0);
                        
                        this.activePlayers.add(p);
                        this.sm.addPlayer(p, this.gameID);
                        
                        this.scoreBoard.addPlayer(p);
                        
                        this.hookvars.put("activeplayers", this.activePlayers.size() + "");
                        LobbyManager.getInstance().updateWall(this.gameID);
                        HookManager.getInstance().runHook("GAME_POST_ADDPLAYER", "activePlayers-" + this.activePlayers.size());
                        
                        if (spawnCount == this.activePlayers.size()) {
                            this.countdown(5);
                        }
                        
                        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(GameManager.getInstance().getPlugin(), () -> {
                            p.setFlying(false);
                            p.setAllowFlight(false);
                            p.setWalkSpeed(0.2F);
                            p.setFireTicks(0);
                            
                            p.getInventory().clear();
                            p.getEquipment().setArmorContents(null);
                            p.updateInventory();
                            this.showMenu(p);
                            
                            for (final PotionEffect effect : p.getActivePotionEffects()) {
                                p.removePotionEffect(effect.getType());
                            }
                            
                        }, 5L);
                        
                        break;
                    }
                }
                if (!placed) {
                    this.msgmgr.sendFMessage(PrefixType.ERROR, "error.gamefull", p, "arena-" + this.gameID);
                    return false;
                }
                
            } else if (SettingsManager.getInstance().getSpawnCount(this.gameID) == 0) {
                this.msgmgr.sendMessage(PrefixType.WARNING, "No spawns set for Arena " + this.gameID + "!", p);
                return false;
            } else {
                this.msgmgr.sendFMessage(PrefixType.WARNING, "error.gamefull", p, "arena-" + this.gameID);
                return false;
            }
            this.msgFall(PrefixType.INFO, "game.playerjoingame", "player-" + p.getDisplayName(), "activeplayers-" + this.getActivePlayers(), "maxplayers-" + SettingsManager.getInstance().getSpawnCount(this.gameID));
            if (this.activePlayers.size() >= this.config.getInt("auto-start-players") && !this.countdownRunning)
                this.countdown(this.config.getInt("auto-start-time"));
            return true;
        } else {
            if (this.config.getBoolean("enable-player-queue")) {
                if (!this.queue.contains(p)) {
                    this.queue.add(p);
                    this.msgmgr.sendFMessage(PrefixType.INFO, "game.playerjoinqueue", p, "queuesize-" + this.queue.size());
                }
                int a = 1;
                for (final Player qp : this.queue) {
                    if (qp == p) {
                        this.msgmgr.sendFMessage(PrefixType.INFO, "game.playercheckqueue", p, "queuepos-" + a);
                        break;
                    }
                    a++;
                }
            }
        }
        switch (this.mode) {
            case INGAME:
                this.msgmgr.sendFMessage(PrefixType.WARNING, "error.alreadyingame", p);
                break;
            case DISABLED:
                this.msgmgr.sendFMessage(PrefixType.WARNING, "error.gamedisabled", p, "arena-" + this.gameID);
                break;
            case RESETING:
                this.msgmgr.sendFMessage(PrefixType.WARNING, "error.gamereseting", p);
                break;
            default:
                this.msgmgr.sendMessage(PrefixType.INFO, "Cannot join game!", p);
                break;
        }
        LobbyManager.getInstance().updateWall(this.gameID);
        return false;
    }
    
    public void showMenu(final Player p) {
        GameManager.getInstance().openKitMenu(p);
        final Inventory i = Bukkit.getServer().createInventory(p, 45, ChatColor.RED + "" + ChatColor.BOLD + "Please select a kit:");
        
        int a = 0;
        int b = 0;
        
        
        final ArrayList<Kit> kits = GameManager.getInstance().getKits(p, this);
        if (kits == null || kits.size() == 0 || !SettingsManager.getInstance().getKits().getBoolean("enabled")) {
            GameManager.getInstance().leaveKitMenu(p);
            return;
        }
        
        for (final Kit k : kits) {
            final ItemStack i1 = k.getIcon();
            final ItemMeta im = i1.getItemMeta();
            
            im.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + k.getName());
            i1.setItemMeta(im);
            i.setItem(9 * a + b, i1);
            a = 2;
            
            for (final ItemStack s2 : k.getContents()) {
                if (s2 != null) {
                    i.setItem(9 * a + b, s2);
                    a++;
                }
            }
            
            a = 0;
            b++;
        }
        p.openInventory(i);
        SurvivalGames.debug(this.gameID, "Showing kit menu for: " + p.getName());
    }
    
    public boolean isProtectionOn() {
        final long t = this.startTime / 1000;
        final long l = this.config.getLong("grace-period");
        final long d = new Date().getTime() / 1000;
        return d - t < l;
    }
    
    public HashMap<Player, Integer> getNextSpec() {
        return this.nextspec;
    }
    
    public boolean isBlockInArena(final Location v) {
        return this.arena.containsBlock(v);
    }
    
    public int getID() {
        return this.gameID;
    }
    
    public Player[][] getPlayers() {
        return new Player[][]{
                this.activePlayers.toArray(new Player[0]), this.inactivePlayers.toArray(new Player[0])
        };
    }
    
    public GameScoreboard getScoreboard() {
        return this.scoreBoard;
    }
    
    public boolean isSpectator(final Player p) {
        return this.spectators.contains(p.getName());
    }
    
    public boolean isInQueue(final Player p) {
        return this.queue.contains(p);
    }
    
    public boolean isPlayerActive(final Player player) {
        return this.activePlayers.contains(player);
    }
    
    public boolean isPlayerinactive(final Player player) {
        return this.inactivePlayers.contains(player);
    }
    
    public boolean hasPlayer(final Player p) {
        return this.activePlayers.contains(p) || this.inactivePlayers.contains(p);
    }
    
    public GameMode getMode() {
        return this.mode;
    }
    
    public void setMode(final GameMode m) {
        this.mode = m;
    }
    
    public double getRBPercent() {
        return this.rbpercent;
    }
    
    public synchronized void setRBPercent(final double d) {
        this.rbpercent = d;
    }
    
    public String getRBStatus() {
        return this.rbstatus;
    }
    
    public void setRBStatus(final String s) {
        this.rbstatus = s;
    }
    
    public String getName() {
        return this.name;
    }
    
    public enum GameMode {
        DISABLED, LOADING, INACTIVE, WAITING,
        STARTING, INGAME, FINISHING, RESETING, ERROR
    }
    
    class NightChecker implements Runnable {
        boolean reset;
        int tgc = Game.this.gcount;
        
        public void run() {
            if (SettingsManager.getGameWorld(Game.this.gameID).getTime() > 14000) {
                for (final Player pl : Game.this.activePlayers) {
                    Game.this.msgmgr.sendMessage(PrefixType.INFO, "Chests restocked!", pl);
                }
                GameManager.openedChest.get(Game.this.gameID).clear();
                this.reset = true;
            }
            
        }
    }
    
    class EndgameManager implements Runnable {
        @Override
        public void run() {
            for (final Player player : Game.this.activePlayers.toArray(new Player[0])) {
                final Location l = player.getLocation();
                l.add(0, 5, 0);
                player.getWorld().strikeLightningEffect(l);
            }
            
        }
    }
    
    class DeathMatchTimer implements Runnable {
        public void run() {
            final int now = (int) (new Date().getTime() / 1000);
            final long length = Game.this.config.getInt("deathmatch.time") * 60;
            final long remaining = length - (now - Game.this.startTime / 1000);
            
            // Death Match countdown warning:
            //   Every 3 minutes
            //   Every minute in the last 3 minutes
            //   At 30 seconds + 10 seconds
            //   Every second for the last 5 seconds
            if (remaining % 180 == 0
                    || remaining % 60 == 0 && remaining <= 180
                    || remaining == 30 || remaining == 10 || remaining <= 5) {
                if (remaining > 60) {
                    Game.this.msgFall(PrefixType.INFO, "game.deathmatchwarning", "t-" + remaining / 60 + " minutes(s)");
                    SurvivalGames.$(Game.this.gameID, "Deathmatch mode will begin in " + remaining / 60 + " minute(s)");
                } else if (remaining > 0) {
                    Game.this.msgFall(PrefixType.INFO, "game.deathmatchwarning", "t-" + remaining + " seconds");
                    SurvivalGames.$(Game.this.gameID, "Deathmatch mode will begin in " + remaining + " seconds");
                }
            }
            
            // Death match time!!
            if (remaining > 0) return;
            SurvivalGames.debug(Game.this.gameID, "DeathMatch mode starting!");
            
            Bukkit.getScheduler().cancelTask(Game.this.dmTaskID);
            if (!Game.this.tasks.remove((Integer) Game.this.dmTaskID)) {
                SurvivalGames.$(Game.this.gameID, "WARNING: DeathMatch task NOT removed!");
            }
            
            final ArrayList<Location> dmspawns = new ArrayList<>();
            boolean dmarena = false;
            if (SettingsManager.getInstance().getDMSpawnCount(Game.this.gameID) >= Game.this.activePlayers.size()) {
                // Death match arena mode (only if we have enough DM spawns for the number of players)
                SurvivalGames.debug(Game.this.gameID, "Deathmatch mode: DM Arena");
                dmarena = true;
                
                // Build a random list of DM spawn locations
                for (int x = 0; x < SettingsManager.getInstance().getDMSpawnCount(Game.this.gameID); x++) {
                    dmspawns.add(SettingsManager.getInstance().getDMSpawnPoint(Game.this.gameID, x));
                }
                Collections.shuffle(dmspawns);
            } else {
                // Death match spawn point mode
                SurvivalGames.debug(Game.this.gameID, "Deathmatch mode: Spawn");
            }
            
            // Teleport everyone to their original spawn point
            for (final Map.Entry<Integer, Player> entry : Game.this.spawns.entrySet()) {
                final Player p = entry.getValue();
                final Integer a = entry.getKey();
                if (Game.this.activePlayers.contains(p) && p.isOnline() && !p.isDead()) {
                    if (dmarena) {
                        // Teleport player to the next random DM spawn point on the list, then remove it
                        SurvivalGames.debug(Game.this.gameID, "Teleporting " + p.getName() + " to random DM spawn point");
                        p.teleport(dmspawns.get(0));
                        dmspawns.remove(0);
                    } else {
                        SurvivalGames.debug(Game.this.gameID, "Teleporting " + p.getName() + " to spawn point #" + a);
                        p.teleport(SettingsManager.getInstance().getSpawnPoint(Game.this.gameID, a).add(0, 1.5, 0));
                    }
                    p.sendMessage(ChatColor.RED + "DeathMatch mode has begun!! Attack!!");
                }
            }
            dmspawns.clear();
            
            Game.this.tasks.add(Bukkit.getScheduler().scheduleSyncRepeatingTask(GameManager.getInstance().getPlugin(), () -> {
                // Game could end (or players die) while inside this loop
                // This must be carefully handled so we dont CME or damage a player that has already left the game
                final ArrayList<Player> players = new ArrayList<>(Game.this.activePlayers);
                for (final Player p : players) {
                    // Verify they are still "alive" and still in the game
                    if (Game.this.mode == GameMode.INGAME && p != null && !p.isDead() && Game.this.activePlayers.contains(p)) {
                        // Player out of arena or too high (towering to avoid players)
                        final int ydiff = Math.abs(Game.this.dmspawn.getBlockY() - p.getLocation().getBlockY());
                        final double dist = Game.this.dmspawn.distance(p.getLocation());
                        if (dist > Game.this.dmradius || ydiff > 4) {
                            p.sendMessage(ChatColor.RED + "Return to the death match area!");
                            p.getLocation().getWorld().strikeLightningEffect(p.getLocation());
                            p.damage(5);
                            p.setFireTicks(60);
                        }
                    }
                }
            }, 10 * 20L, Game.this.config.getInt("deathmatch.killtime") * 20));
        }
    }
}
