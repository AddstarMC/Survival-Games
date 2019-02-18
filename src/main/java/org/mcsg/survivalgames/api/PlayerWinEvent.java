package org.mcsg.survivalgames.api;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.mcsg.survivalgames.Game;

public class PlayerWinEvent extends Event{
	private static final HandlerList handlers = new HandlerList();
    private Player winner;
    private Player killed;
    private Game game;
    private String message;

    public PlayerWinEvent(Game g, Player w, Player k, String m) {
        winner = w;
        game = g;
        killed = k;
        message = m;
    }

    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		// Set this to NULL or empty to prevent the broadcast
		this.message = message;
	}

	public Player getKilled() {
		return killed;
	}

	public void setKilled(Player killed) {
		this.killed = killed;
	}

	public Player getWinner() {
		return winner;
	}

	public void setWinner(Player winner) {
		this.winner = winner;
	}

	public Game getGame() {
		return game;
	}

	public void setGame(Game game) {
		this.game = game;
	}
}
