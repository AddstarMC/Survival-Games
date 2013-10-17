package org.mcsg.survivalgames.lobbysigns;

import java.io.IOException;

import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.mcsg.survivalgames.MessageManager;

public class LobbySignWinner extends LobbySign {
	
	private String m_lastWinnerName = null;

	public LobbySignWinner(Sign sign, int gameId) {
		super(sign.getLocation(), gameId, LobbySignType.Winner);				
	}

	public LobbySignWinner(int gameId) {
		super(gameId, LobbySignType.Winner);
	}
	
	@Override
	public void save(FileConfiguration config) {
		super.save(config);
		config.set("lobby.sign.winnerName", m_lastWinnerName);
	}
	
	@Override
	public void load(FileConfiguration config) {
		super.load(config);
		m_lastWinnerName = config.getString("lobby.sign.winnerName", null);
	}
	
	@Override
	public void postCreationFixup() {
		Block block = this.getLocation().getBlock();
		block.setType(Material.SKULL);
		
		Skull skull = (Skull)block.getState();
		skull.setSkullType(SkullType.CREEPER);
		skull.update();
	}

	@Override
	public void execute(Player player) {
		if (m_lastWinnerName == null)
			return;
		
		MessageManager.getInstance().sendMessage(
				MessageManager.PrefixType.INFO, 
				"The last player to win '" + this.getGame().getName() + "' was " + m_lastWinnerName, 
				player);
	}

	@Override
	public void update() {
		
		if (m_lastWinnerName == null) {
			return;
		}
		
		// Change the player head to the last known winner
		Block block = this.getLocation().getBlock();
		block.setType(Material.SKULL);
		
		Skull skull = (Skull)block.getState();
		skull.setSkullType(SkullType.PLAYER);
		skull.setOwner(m_lastWinnerName);
		skull.update();
	}

	@Override
	public String[] setSignContent(String[] lines) {
		return lines;
	}
	
	public void setWinner(String winner) {
		m_lastWinnerName = winner;
		
		FileConfiguration config = YamlConfiguration.loadConfiguration(this.getSaveFile());
		this.save(config);
		try {
			config.save(this.getSaveFile());
		} catch (IOException e) {}
	}


}
