package org.mcsg.survivalgames.lobbysigns;

import java.io.IOException;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.mcsg.survivalgames.MessageManager;

public class LobbySignWinner extends LobbySign {
	
	private String m_lastWinnerName = null;
	private UUID lastWinnerUUID = null;

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
		config.set("lobby.sign.winnerUUID", lastWinnerUUID.toString());
	}
	
	@Override
	public void load(FileConfiguration config) {
		super.load(config);
		m_lastWinnerName = config.getString("lobby.sign.winnerName", null);
		String uuid = config.getString("lobby.sign.winnerUUID", null);
		if (uuid != null) {
			lastWinnerUUID = UUID.fromString(uuid);
		} else {
			//noinspection deprecation //todo
			lastWinnerUUID = Bukkit.getOfflinePlayer(m_lastWinnerName).getUniqueId();
		}
	}
	
	@Override
	public void postCreationFixup() {
		Block block = this.getLocation().getBlock();
		block.setType(Material.SKULL);

		Skull skull = (Skull)block.getState();
		skull.setSkullType(SkullType.CREEPER);
		BlockFace face = getDirectionFacing(block);
		if (face != null) {
			skull.setRotation(face);
			org.bukkit.material.Skull md = (org.bukkit.material.Skull)skull.getData();
			if ((face == BlockFace.EAST) || (face == BlockFace.WEST))
				md.setFacingDirection(face.getOppositeFace());
			else
				// North/South facing seems to be reversed for some reason (CB bug?)
				md.setFacingDirection(face);
		}
		skull.update();
	}

	@Override
	public void execute(Player player) {
		if (lastWinnerUUID == null)
			return;
		
		MessageManager.getInstance().sendMessage(
				MessageManager.PrefixType.INFO,
				"The last player to win '" + this.getGame().getName() + "' was " + Bukkit.getOfflinePlayer(lastWinnerUUID).getName(),
				player);
	}

	@Override
	public void update() {

		if (lastWinnerUUID == null) {
			return;
		}

		// Change the player head to the last known winner
		Block block = this.getLocation().getBlock();
		block.setType(Material.SKULL);

		Skull skull = (Skull)block.getState();
		skull.setSkullType(SkullType.PLAYER);
		skull.setOwningPlayer(Bukkit.getOfflinePlayer(lastWinnerUUID));
		BlockFace face = getDirectionFacing(block);
		if (face != null) {
			skull.setRotation(face);
			org.bukkit.material.Skull md = (org.bukkit.material.Skull)skull.getData();
			if ((face == BlockFace.EAST) || (face == BlockFace.WEST))
				md.setFacingDirection(face.getOppositeFace());
			else
				// North/South facing seems to be reversed for some reason (CB bug?)
				md.setFacingDirection(face);
		}
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
		} catch (IOException ignored) {
		}
	}

	public BlockFace getDirectionFacing(Block b) {
		// Find which face this block is attached to
		BlockFace faces[] =    { BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST };
		for (BlockFace face : faces) {
			Block a = b.getRelative(face);
			if ((a != null) && (a.getType().isBlock()) && (a.getType().isSolid())) {
				// Found a solid block.. assume it's the attached block
				// Return the opposite direction of the attached block
				return face.getOppositeFace();
			}
		}

		// No attached face found
		return null;
	}
}
