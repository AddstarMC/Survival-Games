package org.mcsg.survivalgames.lobbysigns;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.mcsg.survivalgames.Game;
import org.mcsg.survivalgames.GameManager;

public abstract class LobbySign {
	
	protected Location location = null;
	protected LobbySignType type = LobbySignType.Unknown;
	protected int gameId = -1;
	protected File saveFile = null;
	
	public LobbySign(Location location, int gameId, LobbySignType type) {
		this.location = location;
		this.type = type;
		this.gameId = gameId;
	}
	
	public LobbySign(int gameId, LobbySignType type) {
		this.type = type;
		this.gameId = gameId;
	}

	public Location getLocation() {
		return location;
	}
	
	public Sign getSign() {
		return (Sign)location.getBlock().getState();
	}
	
	public LobbySignType getType() {
		return type;
	}
	
	public Game getGame() {
		return GameManager.getInstance().getGame(gameId);
	}
	
	public void setSaveFile(File saveFile) {
		this.saveFile = saveFile;
	}
	
	public File getSaveFile() {
		return saveFile;
	}
	
	public void save(FileConfiguration config) {
		config.set("lobby.sign.location.world", location.getWorld().getName());
		config.set("lobby.sign.location.x", location.getBlockX());
		config.set("lobby.sign.location.y", location.getBlockY());
		config.set("lobby.sign.location.z", location.getBlockZ());
	}
	
	public void load(FileConfiguration config) {
		String worldName = config.getString("lobby.sign.location.world");
		int xPosition = config.getInt("lobby.sign.location.x");
		int yPosition = config.getInt("lobby.sign.location.y");
		int zPosition = config.getInt("lobby.sign.location.z");
		this.location = new Location(Bukkit.getWorld(worldName), xPosition, yPosition, zPosition);
	}
	
	public abstract void execute(Player player);
	
	public abstract void update();

	public abstract String[] setSignContent(String[] lines);

}
