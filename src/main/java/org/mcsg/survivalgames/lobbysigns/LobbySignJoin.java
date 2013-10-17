package org.mcsg.survivalgames.lobbysigns;

import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.mcsg.survivalgames.GameManager;

public class LobbySignJoin extends LobbySign {

	public LobbySignJoin(Sign sign, int gameId) {
		super(sign.getLocation(), gameId, LobbySignType.Join);				
	}

	public LobbySignJoin(int gameId) {
		super(gameId, LobbySignType.Join);
	}

	@Override
	public void execute(Player player) {
		GameManager.getInstance().addPlayer(player, this.gameId);
	}

	@Override
	public void update() {
		Sign sign = getSign();
		sign.setLine(2, getGame().getName());
		sign.update();
	}

	@Override
	public String[] setSignContent(String[] lines) {
		lines[1] = ChatColor.BOLD + "Join Arena";
		lines[2] = getGame().getName();
		return lines;
	}

}
