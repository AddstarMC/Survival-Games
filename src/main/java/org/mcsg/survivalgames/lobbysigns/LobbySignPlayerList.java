package org.mcsg.survivalgames.lobbysigns;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.mcsg.survivalgames.SurvivalGames;

public class LobbySignPlayerList extends LobbySign {
	
	private int playerIndexStart = 0;

	public LobbySignPlayerList(Sign sign, int gameId) {
		super(sign.getLocation(), gameId, LobbySignType.PlayerList);
		
		sign.setLine(0, "");
		sign.setLine(1, "");
		sign.setLine(2, "");
		sign.setLine(3, "");
	}

	public LobbySignPlayerList(int gameId) {
		super(gameId, LobbySignType.PlayerList);
	}

	@Override
	public void save(FileConfiguration config) {
		super.save(config);
		config.set("lobby.sign.startIndex", playerIndexStart);
	}
	
	@Override
	public void load(FileConfiguration config) {
		super.load(config);
		playerIndexStart = config.getInt("lobby.sign.startIndex");
	}
	
	@Override
	public void execute(Player player) {

	}

	@Override
	public void update() {
		Sign sign = getSign();		
		
		ArrayList<Player> players = getGame().getAllPlayers();
		int index = 0;
		for (int playerIndex = playerIndexStart; playerIndex < playerIndexStart + 4; ++playerIndex) {
			if (playerIndex >= players.size()) {
				sign.setLine(index, "");
				continue;
			}
			
			Player player = players.get(playerIndex);
			String prefix = (SurvivalGames.auth.contains(player.getName()) ? ChatColor.DARK_GREEN : "") + (getGame().isPlayerinactive(player) ? "" + ChatColor.STRIKETHROUGH : "");
			sign.setLine(index, prefix + player.getName());
			index++;
		}
		
		sign.update();
	}
	
	public void setRange(int start) {
		playerIndexStart = start;
	}

}
