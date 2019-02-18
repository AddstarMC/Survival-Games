package org.mcsg.survivalgames;

import java.util.*;

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
    private final ArrayList<Game> games = new ArrayList<>();
    private final HashSet<UUID> kitsel = new HashSet<>();
    ArrayList<Kit> kits = new ArrayList<>();
    MessageManager msgmgr = MessageManager.getInstance();
    private SurvivalGames p;
    
    private GameManager() {
    
    }
    
    public void setup(final SurvivalGames plugin) {
        this.p = plugin;
        this.LoadGames();
        this.LoadKits();
        for (final Game g : this.getGames()) {
            openedChest.put(g.getID(), new HashSet<>());
        }
    }
    
    public void LoadGames() {
        final FileConfiguration c = SettingsManager.getInstance().getSystemConfig();
        this.games.clear();
        final int no = c.getInt("sg-system.arenano", 0);
        int loaded = 0;
        int a = 1;
        while (loaded < no) {
            if (c.isSet("sg-system.arenas." + a + ".x1")) {
                //c.set("sg-system.arenas."+a+".enabled",c.getBoolean("sg-system.arena."+a+".enabled", true));
                if (c.getBoolean("sg-system.arenas." + a + ".enabled")) {
                    //SurvivalGames.$(c.getString("sg-system.arenas."+a+".enabled"));
                    //c.set("sg-system.arenas."+a+".vip",c.getBoolean("sg-system.arenas."+a+".vip", false));
                    SurvivalGames.$(0, "Loading Arena: " + a);
                    SurvivalGames.$(0, "  Spawn points: " + SettingsManager.getInstance().getSpawnCount(a));
                    SurvivalGames.$(0, "  DM spawns   : " + SettingsManager.getInstance().getDMSpawnCount(a));
                    loaded++;
                    this.games.add(new Game(a));
                    StatsManager.getInstance().addArena(a);
                }
            }
            a++;
        }
    }
    
    public void LoadKits() {
        final Set<String> kits1 = SettingsManager.getInstance().getKits().getConfigurationSection("kits").getKeys(false);
        for (final String s : kits1) {
            this.kits.add(new Kit(s));
        }
    }
    
    public ArrayList<Game> getGames() {
        return this.games;
    }
    
    public Plugin getPlugin() {
        return this.p;
    }
    
    public void reloadGames() {
        this.LoadGames();
    }
    
    public boolean isPlayerActive(final Player player) {
        for (final Game g : this.games) {
            if (g.isPlayerActive(player)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean isPlayerInactive(final Player player) {
        for (final Game g : this.games) {
            if (g.isPlayerActive(player)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean isSpectator(final Player player) {
        for (final Game g : this.games) {
            if (g.isSpectator(player)) {
                return true;
            }
        }
        return false;
    }
    
    public void removeFromOtherQueues(final Player p, final int id) {
        for (final Game g : this.getGames()) {
            if (g.isInQueue(p) && g.getID() != id) {
                g.removeFromQueue(p);
                this.msgmgr.sendMessage(PrefixType.INFO, "Removed from the queue in arena " + g.getID(), p);
            }
        }
    }
    
    public boolean isInKitMenu(final Player p) {
        return this.kitsel.contains(p.getUniqueId());
    }
    
    public void leaveKitMenu(final Player p) {
        this.kitsel.remove(p.getUniqueId());
    }
    
    public void openKitMenu(final Player p) {
        this.kitsel.add(p.getUniqueId());
    }
    
    public void selectKit(final Player p, final int i) {
        this.selectKit(p, null, i);
    }
    
    public void selectKit(final Player p, final Game g, final int i) {
        p.getInventory().clear();
        p.getEquipment().setArmorContents(null);
        p.updateInventory();
        final ArrayList<Kit> kits = this.getKits(p, g);
        if (i >= 0 && i < kits.size()) {
            final Kit k = kits.get(i);
            if (k != null) {
                p.getInventory().setContents(k.getContents().toArray(new ItemStack[0]));
            }
        }
        p.updateInventory();
    }
    
    public ArrayList<Kit> getKits(final Player p, final Game g) {
        final ArrayList<Kit> k = new ArrayList<>();
        if (g != null) {
            for (final Kit kit : g.getKits()) {
                if (kit.canUse(p)) {
                    k.add(kit);
                }
            }
        } else
            for (final Kit kit : this.kits) {
                if (kit.canUse(p)) {
                    k.add(kit);
                }
            }
        return k;
    }
    
    public Game getGame(final Player p) {
        for (final Game g : this.games) {
            if (g.isInQueue(p) || g.isPlayerActive(p) || g.isPlayerinactive(p))
                return g;
        }
        return null;
    }
    
    public int getGameCount() {
        return this.games.size();
    }
    
    public void removePlayer(final Player p, final boolean b) {
        final Game game = this.getGame(this.getPlayerGameId(p));
        game.playerLeave(p, b);
        final PlayerLeaveArenaEvent event = new PlayerLeaveArenaEvent(p, this.getGame(this.getPlayerGameId(p)), !p.isOnline());
        Bukkit.getPluginManager().callEvent(event);
    }
    
    public Game getGame(final int a) {
        //int t = gamemap.get(a);
        for (final Game g : this.games) {
            if (g.getID() == a) {
                return g;
            }
        }
        return null;
    }
    
    public int getPlayerGameId(final Player p) {
        for (final Game g : this.games) {
            if (g.isPlayerActive(p)) {
                return g.getID();
            }
        }
        return -1;
    }
    
    public void removeSpectator(final Player p) {
        this.getGame(this.getPlayerSpectateId(p)).removeSpectator(p);
    }
    
    public int getPlayerSpectateId(final Player p) {
        for (final Game g : this.games) {
            if (g.isSpectator(p)) {
                return g.getID();
            }
        }
        return -1;
    }
    
    public void disableGame(final int id) {
        this.getGame(id).disable();
    }
    
    public void enableGame(final int id) {
        this.getGame(id).enable();
    }
    
    public GameMode getGameMode(final int a) {
        for (final Game g : this.games) {
            if (g.getID() == a) {
                return g.getMode();
            }
        }
        return null;
    }
    
    public ArrayList<Kit> getKits(final Player p) {
        return this.getKits(p, null);
    }
    
    public List<Kit> getKits(final Game g) {
        if (g != null) {
            return g.getKits();
        }
        return this.kits;
    }
    
    //TODO: Actually make this countdown correctly
    public void startGame(final int gameId) {
        this.getGame(gameId).countdown(10);
    }
    
    public void addPlayer(final Player p, final int g) {
        final Game game = this.getGame(g);
        if (game == null) {
            MessageManager.getInstance().sendFMessage(PrefixType.ERROR, "error.input", p, "message-No game by this ID exist!");
            return;
        }
        game.addPlayer(p);
    }
    
    public void autoAddPlayer(final Player pl) {
        final ArrayList<Game> qg = new ArrayList<>();
        for (final Game g : this.games) {
            if (g.getMode() == Game.GameMode.WAITING) {
                qg.add(g);
            }
        }
        //TODO: fancy auto balance algorithm
        if (qg.size() == 0) {
            pl.sendMessage(ChatColor.RED + "No games to join");
            this.msgmgr.sendMessage(PrefixType.WARNING, "No games to join!", pl);
            return;
        }
        
        final int randomGame = new Random().nextInt(qg.size());
        final Game game = qg.get(randomGame);
        game.addPlayer(pl);
    }
    
    public WorldEditPlugin getWorldEdit() {
        return this.p.getWorldEdit();
    }
    
    public void createArenaFromSelection(final Player pl) {
        final FileConfiguration c = SettingsManager.getInstance().getSystemConfig();
        //SettingsManager s = SettingsManager.getInstance();
        
        final WorldEditPlugin we = this.p.getWorldEdit();
        final LocalSession session = we.getSession(pl);
        final RegionSelector selector = session.getRegionSelector(we.wrapPlayer(pl).getWorld());
        Region region = null;
        try {
            region = selector.getRegion();
        } catch (final IncompleteRegionException ignored) {
        }
        if (region == null) {
            this.msgmgr.sendMessage(PrefixType.WARNING, "You must make a WorldEdit Selection first!", pl);
            return;
        }
        final BlockVector3 max = region.getMaximumPoint();
        final BlockVector3 min = region.getMinimumPoint();
        if (region.getWorld() == null) {
            this.msgmgr.sendMessage(PrefixType.WARNING, "Selection did not have a valid world !!", pl);
            return;
        }
        /* if(max.getWorld()!=SettingsManager.getGameWorld() || min.getWorld()!=SettingsManager.getGameWorld()){
            pl.sendMessage(ChatColor.RED+"Wrong World!");
            return;
        }*/
        
        int no = c.getInt("sg-system.arenano") + 1;
        c.set("sg-system.arenano", no);
        if (this.games.size() == 0) {
            no = 1;
        } else no = this.games.get(this.games.size() - 1).getID() + 1;
        SettingsManager.getInstance().getSpawns().set("spawns." + no, null);
        c.set("sg-system.arenas." + no + ".world", region.getWorld().getName());
        c.set("sg-system.arenas." + no + ".x1", max.getBlockX());
        c.set("sg-system.arenas." + no + ".y1", max.getBlockY());
        c.set("sg-system.arenas." + no + ".z1", max.getBlockZ());
        c.set("sg-system.arenas." + no + ".x2", min.getBlockX());
        c.set("sg-system.arenas." + no + ".y2", min.getBlockY());
        c.set("sg-system.arenas." + no + ".z2", min.getBlockZ());
        c.set("sg-system.arenas." + no + ".enabled", true);
        
        SettingsManager.getInstance().saveSystemConfig();
        this.hotAddArena(no);
        pl.sendMessage(ChatColor.GREEN + "Arena ID " + no + " Succesfully added");
        
    }
    
    private void hotAddArena(final int no) {
        final Game game = new Game(no);
        this.games.add(game);
        StatsManager.getInstance().addArena(no);
        //SurvivalGames.$("game added "+ games.size()+" "+SettingsManager.getInstance().getSystemConfig().getInt("gs-system.arenano"));
    }
    
    public void hotRemoveArena(final int no) {
        for (final Game g : this.games.toArray(new Game[0])) {
            if (g.getID() == no) {
                this.games.remove(this.getGame(no));
            }
        }
    }
    
    public void gameEndCallBack(final int id) {
        this.getGame(id).setRBStatus("clearing chest");
        openedChest.put(id, new HashSet<>());
    }
    
    public boolean checkGameDisabled(final Location loc, final Player player) {
        final int gameID = this.getPlayerGameId(player);
        if (gameID == -1) {
            final int blockgameid = GameManager.getInstance().getBlockGameId(loc);
            if (blockgameid != -1) {
                return GameManager.getInstance().getGame(blockgameid).getGameMode() != GameMode.DISABLED;
            }
            return false;
        }
        return false;
    }
    
    public int getBlockGameId(final Location v) {
        for (final Game g : this.games) {
            if (g.isBlockInArena(v)) {
                return g.getID();
            }
        }
        return -1;
    }
    
    public static GameManager getInstance() {
        return instance;
    }
    
    public String getStringList(final int gid) {
        final Game g = this.getGame(gid);
        if (g == null)
            return null;
        
        final StringBuilder sb = new StringBuilder();
        final Player[][] players = g.getPlayers();

        sb.append(ChatColor.GREEN + "<---------------------[ Alive: ").append(players[0].length).append(" ]--------------------->\n").append(ChatColor.GREEN).append(" ");
        for (final Player p : players[0]) {
            sb.append(p.getName()).append(",");
        }
        sb.append("\n\n");
        sb.append(ChatColor.RED + "<---------------------[ Dead: ").append(players[1].length).append(" ]---------------------->\n").append(ChatColor.GREEN).append(" ");
        for (final Player p : players[1]) {
            sb.append(p.getName()).append(",");
        }
        sb.append("\n\n");
        
        return sb.toString();
    }
    
    public int getIdFromName(final String gameName) {
        for (final Game game : this.games) {
            if (game.getName().equalsIgnoreCase(gameName))
                return game.getID();
        }
        return -1;
    }


}
