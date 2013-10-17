package org.mcsg.survivalgames.lobbysigns;

import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.mcsg.survivalgames.SettingsManager;

public class LobbySignPlayers extends LobbySign {

	public LobbySignPlayers(Sign sign, int gameId) {
		super(sign.getLocation(), gameId, LobbySignType.Players);
	}

	public LobbySignPlayers(int gameId) {
		super(gameId, LobbySignType.Players);
	}

	@Override
	public void execute(Player player) {

	}

	@Override
	public void update() {
		Sign sign = getSign();		
		sign.setLine(2, getGame().getActivePlayers() + "/" + SettingsManager.getInstance().getSpawnCount(gameId));
		sign.update();
	}

	@Override
	public String[] setSignContent(String[] lines) {
		lines[1] = "Active Players";
		lines[2] = getGame().getActivePlayers() + "/" + SettingsManager.getInstance().getSpawnCount(gameId);
		return lines;
	}
}
