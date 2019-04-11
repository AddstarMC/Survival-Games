package org.mcsg.survivalgames;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.mcsg.survivalgames.util.ItemReader;
import org.mcsg.survivalgames.util.Kit;

public class SettingsManager {

	//makes the config easily accessible

	private static SettingsManager instance = new SettingsManager();
	private static Plugin p;
	private FileConfiguration spawns;
	private FileConfiguration system;
	private FileConfiguration kits;
	private FileConfiguration messages;
	private FileConfiguration dmspawns;

	private File f; //spawns
	private File f2; //system
	private File kitFile; //kits
	private File f4; //messages
	private File f5; //deathmatch spawns
	private File chestFile; //chest

	private static final int KIT_VERSION = 2;
	private static final int MESSAGE_VERSION = 1;
	private static final int SPAWN_VERSION = 0;
	private static final int DMSPAWN_VERSION = 0;
	private static final int SYSTEM_VERSION = 0;

	private ItemStack specItemNext = null;
	private ItemStack specItemPrev = null;
	private ItemStack specItemExit = null;
	
	private SettingsManager() {

	}

	public static SettingsManager getInstance() {
		return instance;
	}

	public void setup(Plugin p) {
		SettingsManager.p = p;
		if (p.getConfig().getInt("config-version") == SurvivalGames.config_version) {
			SurvivalGames.config_todate = true;
		}else{
			File config = new File(p.getDataFolder(), "config.yml");
			config.delete();
		}
		ItemReader.loadIds();
		p.getConfig().options().copyDefaults(true);
		p.saveDefaultConfig();

		f = new File(p.getDataFolder(), "spawns.yml");
		f2 = new File(p.getDataFolder(), "system.yml");
		kitFile = new File(p.getDataFolder(), "kits.yml");
		f4 = new File(p.getDataFolder(), "messages.yml");
		f5 = new File(p.getDataFolder(), "dmspawns.yml");
		chestFile = new File(p.getDataFolder(), "items.json");

		specItemNext = ItemReader.read(getConfig().getString("spectate.next-item"));
		specItemPrev = ItemReader.read(getConfig().getString("spectate.prev-item"));
		specItemExit = ItemReader.read(getConfig().getString("spectate.exit-item"));

		try {
			if (!f.exists()) 	f.createNewFile();
			if (!f2.exists())	f2.createNewFile();
			if (!kitFile.exists()) loadFile("kits.yml");
			if (!f4.exists()) 	loadFile("messages.yml");
			if (!f5.exists()) 	f5.createNewFile();
			if (!chestFile.exists()) 	loadFile("items.json");

		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		
		reloadSystem();
		saveSystemConfig();
		
		reloadSpawns();
		saveSpawns();

		reloadDMSpawns();
		saveDMSpawns();
				
		reloadKits();
		
		reloadMessages();
		saveMessages();
	}

	public ItemStack getSpecItemNext() {
		return specItemNext;
	}

	public void setSpecItemNext(ItemStack specItemNext) {
		this.specItemNext = specItemNext;
	}

	public ItemStack getSpecItemPrev() {
		return specItemPrev;
	}

	public void setSpecItemPrev(ItemStack specItemPrev) {
		this.specItemPrev = specItemPrev;
	}

	public ItemStack getSpecItemExit() {
		return specItemExit;
	}

	public void setSpecItemExit(ItemStack specItemExit) {
		this.specItemExit = specItemExit;
	}

	public void set(String arg0, Object arg1) {
		p.getConfig().set(arg0, arg1);
	}

	public FileConfiguration getConfig() {
		return p.getConfig();
	}

	public FileConfiguration getSystemConfig() {
		return system;
	}

	public FileConfiguration getSpawns() {
		return spawns;
	}

	public FileConfiguration getDMSpawns() {
		return dmspawns;
	}

	public FileConfiguration getKits() {
		return kits;
	}
	
	public File getChestFile() {
		return chestFile;
	}
	
	public FileConfiguration getMessageConfig() {
		//System.out.println("asdf"+messages.getString("prefix.main"));
		return messages;
	}

	public void saveConfig() {
		// p.saveConfig();
	}

	public static World getGameWorld(int game) {
		if (SettingsManager.getInstance().getSystemConfig().getString("sg-system.arenas." + game + ".world") == null) {
			//LobbyManager.getInstance().error(true);
			return null;

		}

		return p.getServer().getWorld(SettingsManager.getInstance().getSystemConfig().getString("sg-system.arenas." + game + ".world"));
	}

	public void reloadConfig(){
		p.reloadConfig();
	}
	
	public boolean moveFile(File ff){
		SurvivalGames.info(0, "Moving outdated config file. " + f.getName());
		String name = ff.getName();
		File ff2 = new File(SurvivalGames.getPluginDataFolder(), getNextName(name, 0));
		return ff.renameTo(ff2);
	}
	
	public String getNextName(String name, int n){
		File ff = new File(SurvivalGames.getPluginDataFolder(), name+".old"+n);
		if(!ff.exists()){
			return ff.getName();
		}
		else{
			return getNextName(name, n+1);
		}
	}

	public void reloadSpawns() {
		spawns = YamlConfiguration.loadConfiguration(f);
		if(spawns.getInt("version", 0) != SPAWN_VERSION){
			moveFile(f);
			reloadSpawns();
		}
		spawns.set("version", SPAWN_VERSION);
		saveSpawns();
	}

	public void reloadDMSpawns() {
		dmspawns = YamlConfiguration.loadConfiguration(f5);
		if(dmspawns.getInt("version", 0) != DMSPAWN_VERSION){
			moveFile(f5);
			reloadDMSpawns();
		}
		dmspawns.set("version", DMSPAWN_VERSION);
		saveDMSpawns();
	}

	public void reloadSystem() {
		system = YamlConfiguration.loadConfiguration(f2);
		if(system.getInt("version", 0) != SYSTEM_VERSION){
			moveFile(f2);
			reloadSystem();
		}
		system.set("version", SYSTEM_VERSION);
		saveSystemConfig();
	}
	
	public void reloadMessages() {
		messages = YamlConfiguration.loadConfiguration(f4);
		if(messages.getInt("version", 0) != MESSAGE_VERSION){
			moveFile(f4);
			loadFile("messages.yml");
			reloadKits();
		}
		messages.set("version", MESSAGE_VERSION);
		saveMessages();
	}

	public void saveSystemConfig() {
		try {
            this.system.save(this.f2);
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void reloadKits() {
		this.kits = YamlConfiguration.loadConfiguration(this.kitFile);
		if(this.kits.getInt("version", 0) != KIT_VERSION){
			this.moveFile(this.kitFile);
            this.loadFile("kits.yml");
            this.reloadKits();
		}

	}

	public void saveSpawns() {
		try {
            this.spawns.save(this.f);
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void saveDMSpawns() {
		try {
            this.dmspawns.save(this.f5);
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void saveKits() {
		try {
			for (Kit kit : GameManager.instance.kits) {
				ConfigurationSection kits = this.kits.getConfigurationSection("kits");
				ConfigurationSection kitConfig;
				if (kits.contains(kit.getName())) {
					kitConfig = kits.getConfigurationSection(kit.getName());
				} else {
					kitConfig = kits.createSection(kit.getName());
				}

				if (kit.getIcon() != null) {
					kitConfig.set("icon", kit.getIcon().getType().name());
				} else {
					kitConfig.set("icon", "null");
				}
				kitConfig.set("cost", kit.getCost());
				kitConfig.set("kitInventory", kit.getKitInventory());
				kits.set(kit.getName(), kitConfig);
			}
			this.kits.save(this.kitFile);
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void saveMessages() {
		try {
            this.messages.save(this.f4);
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void saveChest() {
		
	}

	public int getSpawnCount(final int gameid) {
		return this.spawns.getInt("spawns." + gameid + ".count");
	}

	public int getDMSpawnCount(final int gameid) {
		return this.dmspawns.getInt("dmspawns." + gameid + ".count", 0);
	}

	//TODO: Implement per-arena settings aka flags
	public HashMap < String, Object > getGameFlags(final int a) {
        final HashMap<String, Object> flags = new HashMap<>();

		flags.put("AUTOSTART_PLAYERS", this.system.getInt("sg-system.arenas." + a + ".flags.autostart"));
		flags.put("AUTOSTART_VOTE", this.system.getInt("sg-system.arenas." + a + ".flags.vote"));
		flags.put("ENDGAME_ENABLED", this.system.getBoolean("sg-system.arenas." + a + ".flags.endgame-enabled"));
		flags.put("ENDGAME_PLAYERS", this.system.getInt("sg-system.arenas." + a + ".flags.endgame-players"));
		flags.put("ENDGAME_CHEST", this.system.getBoolean("sg-system.arenas." + a + ".flags.endgame-chest"));
		flags.put("ENDGAME_LIGHTNING", this.system.getBoolean("sg-system.arenas." + a + ".flags.endgame-lightning"));
		flags.put("DUEL_PLAYERS", this.system.getInt("sg-system.arenas." + a + ".flags.endgame-duel-players"));
		flags.put("DUEL_TIME", this.system.getInt("sg-system.arenas." + a + ".flags.endgame-duel-time"));
		flags.put("DUEL_ENABLED", this.system.getBoolean("sg-system.arenas." + a + ".flags.endgame-duel"));
		flags.put("ARENA_NAME", this.system.getString("sg-system.arenas." + a + ".flags.arena-name"));
		flags.put("ARENA_COST", this.system.getInt("sg-system.arenas." + a + ".flags.arena-cost"));
		flags.put("ARENA_REWARD", this.system.getInt("sg-system.arenas." + a + ".flags.arena-reward"));
		flags.put("ARENA_MAXTIME", this.system.getInt("sg-system.arenas." + a + ".flags.arena-maxtime"));
		flags.put("SPONSOR_ENABLED", this.system.getBoolean("sg-system.arenas." + a + ".flags.sponsor-enabled"));
		flags.put("SPONSOR_MODE", this.system.getInt("sg-system.arenas." + a + ".flags.sponsor-mode"));

		return flags;

	}
	public void saveGameFlags(final HashMap < String, Object > flags, final int a) {
        
        this.system.set("sg-system.arenas." + a + ".flags.autostart", flags.get("AUTOSTART_PLAYERS"));
        this.system.set("sg-system.arenas." + a + ".flags.vote", flags.get("AUTOSTART_VOTE"));
        this.system.set("sg-system.arenas." + a + ".flags.endgame-enabled", flags.get("ENDGAME_ENABLED"));
        this.system.set("sg-system.arenas." + a + ".flags.endgame-players", flags.get("ENDGAME_PLAYERS"));
        this.system.set("sg-system.arenas." + a + ".flags.endgame-chest", flags.get("ENDGAME_CHEST"));
        this.system.set("sg-system.arenas." + a + ".flags.endgame-lightning", flags.get("ENDGAME_LIGHTNING"));
        this.system.set("sg-system.arenas." + a + ".flags.endgame-duel-players", flags.get("DUEL_PLAYERS"));
        this.system.set("sg-system.arenas." + a + ".flags.endgame-duel-time", flags.get("DUEL_TIME"));
        this.system.set("sg-system.arenas." + a + ".flags.endgame-duel", flags.get("DUEL_ENABLED"));
        this.system.set("sg-system.arenas." + a + ".flags.arena-name", flags.get("ARENA_NAME"));
        this.system.set("sg-system.arenas." + a + ".flags.arena-cost", flags.get("ARENA_COST"));
        this.system.set("sg-system.arenas." + a + ".flags.arena-reward", flags.get("ARENA_REWARD"));
        this.system.set("sg-system.arenas." + a + ".flags.arena-maxtime", flags.get("ARENA_MAXTIME"));
        this.system.set("sg-system.arenas." + a + ".flags.sponsor-enabled", flags.get("SPONSOR_ENABLED"));
        this.system.set("sg-system.arenas." + a + ".flags.sponsor-mode", flags.get("SPONSOR_MODE"));
        
        this.saveSystemConfig();

	}

	public Location getLobbySpawn() {
		try{
			return new Location(Bukkit.getWorld(this.system.getString("sg-system.lobby.spawn.world")),
                    this.system.getDouble("sg-system.lobby.spawn.x"),
                    this.system.getDouble("sg-system.lobby.spawn.y"),
                    this.system.getDouble("sg-system.lobby.spawn.z"),
				(float) this.system.getDouble("sg-system.lobby.spawn.yaw"),
				(float) this.system.getDouble("sg-system.lobby.spawn.pitch")
				);
		}catch(final Exception e){
			return null;
		}
	}

	public Location getSpawnPoint(final int gameid, final int spawnid) {
		return new Location(getGameWorld(gameid),
                this.spawns.getDouble("spawns." + gameid + "." + spawnid + ".x"),
                this.spawns.getDouble("spawns." + gameid + "." + spawnid + ".y"),
                this.spawns.getDouble("spawns." + gameid + "." + spawnid + ".z"),
				(float) this.spawns.getDouble("spawns." + gameid + "." + spawnid + ".yaw"),
				(float) this.spawns.getDouble("spawns." + gameid + "." + spawnid + ".pitch")
				);
	}
	
	public Location getDMSpawnPoint(final int gameid, final int dmspawnid) {
		return new Location(getGameWorld(gameid),
                this.dmspawns.getDouble("dmspawns." + gameid + "." + dmspawnid + ".x"),
                this.dmspawns.getDouble("dmspawns." + gameid + "." + dmspawnid + ".y"),
                this.dmspawns.getDouble("dmspawns." + gameid + "." + dmspawnid + ".z"),
				(float) this.dmspawns.getDouble("dmspawns." + gameid + "." + dmspawnid + ".yaw"),
				(float) this.dmspawns.getDouble("dmspawns." + gameid + "." + dmspawnid + ".pitch")
				);
	}

	public void setLobbySpawn(final Location l) {
        this.system.set("sg-system.lobby.spawn.world", l.getWorld().getName());
        this.system.set("sg-system.lobby.spawn.x", l.getX());
        this.system.set("sg-system.lobby.spawn.y", l.getY());
        this.system.set("sg-system.lobby.spawn.z", l.getZ());
        this.system.set("sg-system.lobby.spawn.yaw", l.getYaw());
        this.system.set("sg-system.lobby.spawn.pitch", l.getPitch());
	}

	public void setSpawn(final int gameid, final int spawnid, final Location l) {
        this.spawns.set("spawns." + gameid + "." + spawnid + ".x", l.getX());
        this.spawns.set("spawns." + gameid + "." + spawnid + ".y", l.getY());
        this.spawns.set("spawns." + gameid + "." + spawnid + ".z", l.getZ());
        this.spawns.set("spawns." + gameid + "." + spawnid + ".yaw", l.getYaw());
        this.spawns.set("spawns." + gameid + "." + spawnid + ".pitch", l.getPitch());
		
		if (spawnid > this.spawns.getInt("spawns." + gameid + ".count")) {
            this.spawns.set("spawns." + gameid + ".count", spawnid);
		}
		try {
            this.spawns.save(this.f);
		} catch (final IOException e) {
			SurvivalGames.info(0, "ERROR: Unable to save spawns file!");
			e.printStackTrace();
		}
		GameManager.getInstance().getGame(gameid).addSpawn();
		
		LobbyManager.getInstance().updateWall(gameid);
	}

	public void setDMSpawn(final int gameid, final int spawnid, final Location l) {
        this.dmspawns.set("dmspawns." + gameid + "." + spawnid + ".x", l.getX());
        this.dmspawns.set("dmspawns." + gameid + "." + spawnid + ".y", l.getY());
        this.dmspawns.set("dmspawns." + gameid + "." + spawnid + ".z", l.getZ());
        this.dmspawns.set("dmspawns." + gameid + "." + spawnid + ".yaw", l.getYaw());
        this.dmspawns.set("dmspawns." + gameid + "." + spawnid + ".pitch", l.getPitch());

		if (spawnid > this.dmspawns.getInt("dmspawns." + gameid + ".count")) {
            this.dmspawns.set("dmspawns." + gameid + ".count", spawnid);
		}
		try {
            this.dmspawns.save(this.f5);
		} catch (final IOException e) {
			SurvivalGames.info(0, "ERROR: Unable to save dmspawns file!");
			e.printStackTrace();
		}
	}

	public static String getSqlPrefix() {
		return getInstance().getConfig().getString("sql.prefix");
	}

	public void loadFile(final String file){
		final File t = new File(p.getDataFolder(), file);
		System.out.println("Writing new file: "+ t.getAbsolutePath());
			
			try {
				t.createNewFile();
				final FileWriter out = new FileWriter(t);
				System.out.println(file);
				final InputStream is = this.getClass().getResourceAsStream("/"+file);
				final InputStreamReader isr = new InputStreamReader(is);
				final BufferedReader br = new BufferedReader(isr);
				String line;
				while ((line = br.readLine()) != null) {
					out.write(line+"\n");
					System.out.println(line);
				}
				out.flush();
				is.close();
				isr.close();
				br.close();
				out.close();
			} catch (final IOException e) {
				e.printStackTrace();
			}
		
	}
}