package org.mcsg.survivalgames.lobbysigns;

import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.mcsg.survivalgames.Game;
import org.mcsg.survivalgames.MessageManager;
import org.mcsg.survivalgames.Game.GameMode;

public class LobbySignState extends LobbySign {

	public LobbySignState(Sign sign, int gameId) {
		super(sign.getLocation(), gameId, LobbySignType.State);
	}

	public LobbySignState(int gameId) {
		super(gameId, LobbySignType.State);
	}

	@Override
	public void execute(Player player) {
		// output some help about the state and what it means
		GameMode gameMode = getGame().getGameMode();
		String infoMessage = "Unknown arena state";

        switch (gameMode) {
            case DISABLED:
                infoMessage = "This arena is currently disabled. This is normally due to arena maintenance.";
                break;
            case ERROR:
                infoMessage = "There is an error with this arena! Please inform a Staff memeber.";
                break;
            case FINISHING:
                infoMessage = "This arena is finishing. A new game will be ready shortly...";
                break;
            case INACTIVE:
                infoMessage = "This arena is currently inactive. Please play a different arena.";
                break;
            case INGAME:
                infoMessage = "A game has already started. You can join the arena queue to take part in the next game.";
                break;
            case LOADING:
                infoMessage = "This arena is loading. A game will be ready shortly...";
                break;
            case RESETING:
                infoMessage = "This arena is resetting. A new game will be ready shortly...";
                break;
            case STARTING:
                infoMessage = "A game in this arena is about to start! Join quickly!";
                break;
            case WAITING:
                infoMessage = "This arena is waiting to start. You can join this arena and use '/sg vote' to ready up!";
                break;
        }
		
		MessageManager.getInstance().sendMessage(MessageManager.PrefixType.INFO, infoMessage, player);
	}

	@Override
	public void update() {
		Sign sign = getSign();		
		GameMode gameMode = getGame().getGameMode();
		sign.setLine(2, "" + Game.GetColorPrefix(gameMode) + ChatColor.BOLD + gameMode.toString());
		sign.update();
	}

	@Override
	public String[] setSignContent(String[] lines) {
		GameMode gameMode = getGame().getGameMode();
		lines[1] = "Arena State";
		lines[2] = "" + Game.GetColorPrefix(gameMode) + ChatColor.BOLD + gameMode.toString();
		return lines;
	}
	
}
