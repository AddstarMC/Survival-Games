package org.mcsg.survivalgames;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

public class GameScoreboard {
	
	private final int gameID;
	private final Scoreboard scoreboard;
	private Objective sidebarObjective = null;
	private Team livingTeam = null;
	
	private HashMap<String, Scoreboard> originalScoreboard = new HashMap<String, Scoreboard>();
	private ArrayList<String> activePlayers = new ArrayList<String>();
	
	/**
	 * Class constructor
	 * 
	 * @param gameID	The game id this scoreboard is used within
	 */
	public GameScoreboard(int gameID) {
		
		ScoreboardManager manager = Bukkit.getScoreboardManager();
		
		this.gameID = gameID;
		this.scoreboard = manager.getNewScoreboard();
			
		reset();
	}
	
	/**
	 * Reset the scoreboard back to its original empty state
	 */
	public void reset() {
		
		// Remove any players still on the scoreboard
		if (!this.activePlayers.isEmpty()) {
			ArrayList<String> players = new ArrayList<String>();
			for (String playerName : this.activePlayers) {
				players.add(playerName);
			}
			for (String playerName : players) {
				Player player = Bukkit.getPlayer(playerName);
				if (player != null) {
					removePlayer(player);
				}
			}
		}
		
		// Unregister the objective
		if (this.sidebarObjective != null) {
			this.sidebarObjective.unregister();
			this.sidebarObjective = null;
		}
		
		// Reset the living team
		if (this.livingTeam != null) {
			this.livingTeam.unregister();
			this.livingTeam = null;
		}
		
		// Create the objective
		this.sidebarObjective = this.scoreboard.registerNewObjective("survivalGames-" + this.gameID, "dummy");
		this.sidebarObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
		
		// Create the living team
		this.livingTeam = this.scoreboard.registerNewTeam("Living");
		this.livingTeam.setAllowFriendlyFire(true);
		this.livingTeam.setCanSeeFriendlyInvisibles(false);
		
	}
	
	/**
	 * Add a player to the scoreboard
	 * 
	 * @param player	The player to add to the scoreboard
	 */
	public void addPlayer(final Player player) {
		
		// Store the current scoreboard for the player
		Scoreboard original = player.getScoreboard();
		if (original != null) {
			this.originalScoreboard.put(player.getName(), original);
		}
		
		this.activePlayers.add(player.getName());
		
		// Set the players scoreboard and andd them too the team
		player.setScoreboard(this.scoreboard);
		this.livingTeam.addPlayer(player);
		
		// Set the players score to zero, then increase it
		Score score = this.sidebarObjective.getScore(player);
		score.setScore(1);
		
		final Objective sidebarObjective = this.sidebarObjective;
		Bukkit.getScheduler().runTaskLater(GameManager.getInstance().getPlugin(), new Runnable() {
            public void run() {
            	sidebarObjective.getScore(player).setScore(0);
            }
        }, 1L);
		
		updateSidebarTitle();	
	}
	
	/**
	 * Remove a player from the scoreboard
	 * 
	 * @param player	The player to remove from the scoreboard
	 */
	public void removePlayer(Player player) {
		
		// remove the player from the team
		this.livingTeam.removePlayer(player);
		this.scoreboard.resetScores(player);
		
		// Restore the players scoreboard
		Scoreboard original = this.originalScoreboard.get(player.getName());
		if (original != null) {
			player.setScoreboard(original);
			this.originalScoreboard.remove(player.getName());
		}
		this.activePlayers.remove(player.getName());
				
		updateSidebarTitle();
	}
	
	/**
	 * Update the title of the sidebar objective
	 */
	private void updateSidebarTitle() {
		final int noofPlayers = this.activePlayers.size();
		final int maxPlayers = SettingsManager.getInstance().getSpawnCount(gameID);
		final String gameName = GameManager.getInstance().getGame(gameID).getName();
		
		this.sidebarObjective.setDisplayName(ChatColor.GOLD + gameName + " (" + noofPlayers + "/" + maxPlayers + ")");
	}

}
