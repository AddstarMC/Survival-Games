package org.mcsg.survivalgames.lobbysigns;

import org.bukkit.ChatColor;
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
		ChatColor col = ChatColor.DARK_BLUE;
		if (getGame().getActivePlayers() == 0) { col = ChatColor.BLACK; }
		else if (getGame().getActivePlayers() == 2) { col = ChatColor.DARK_RED; }

		Sign sign = getSign();
		sign.setLine(2, "" + col + getGame().getActivePlayers() + ChatColor.BLACK + "/" + ChatColor.DARK_PURPLE + SettingsManager.getInstance().getSpawnCount(gameId));
		sign.update();
	}

	@Override
	public String[] setSignContent(String[] lines) {
		ChatColor col = ChatColor.DARK_BLUE;
		if (getGame().getActivePlayers() == 0) { col = ChatColor.BLACK; }
		else if (getGame().getActivePlayers() == 2) { col = ChatColor.DARK_RED; }

		lines[1] = "Active Players";
		lines[2] = "" + col + getGame().getActivePlayers() + ChatColor.BLACK + "/" + ChatColor.DARK_PURPLE + SettingsManager.getInstance().getSpawnCount(gameId);
		return lines;
	}
}
