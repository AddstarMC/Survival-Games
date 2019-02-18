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
	private Objective sidebarObjective;
	private Team waitingTeam;
	private Team livingTeam;
	private Team deadTeam;

	private final HashMap<String, Scoreboard> originalScoreboard = new HashMap<>();
	private final ArrayList<String> activePlayers = new ArrayList<>();
	
	/**
	 * Class constructor
	 * 
	 * @param gameID	The game id this scoreboard is used within
	 */
	public GameScoreboard(final int gameID) {
		
		final ScoreboardManager manager = Bukkit.getScoreboardManager();
		
		this.gameID = gameID;
		this.scoreboard = manager.getNewScoreboard();
        
        this.reset();
	}
	
	/**
	 * Reset the scoreboard back to its original empty state
	 */
	public void reset() {
		
		// Remove any players still on the scoreboard
		if (!this.activePlayers.isEmpty()) {
			final ArrayList<String> players = new ArrayList<>();
			for (final String playerName : this.activePlayers) {
				players.add(playerName);
			}
			for (final String playerName : players) {
				final Player player = Bukkit.getPlayer(playerName);
				if (player != null) {
                    this.removePlayer(player);
				}
			}
		}
		
		// Unregister the objective
		if (this.sidebarObjective != null) {
			this.sidebarObjective.unregister();
			this.sidebarObjective = null;
		}
		
		// Reset the waiting team
		if (this.waitingTeam != null) {
			this.waitingTeam.unregister();
			this.waitingTeam = null;
		}
		
		// Reset the living team
		if (this.livingTeam != null) {
			this.livingTeam.unregister();
			this.livingTeam = null;
		}
		
		// Reset the dead team
		if (this.deadTeam != null) {
			this.deadTeam.unregister();
			this.deadTeam = null;
		}
		
		// Create the objective
        this.sidebarObjective = this.scoreboard.registerNewObjective("survivalGames-" + this.gameID, "dummy", "");
		this.sidebarObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
		
		// Create the living team
		this.waitingTeam = this.scoreboard.registerNewTeam("Waiting");
		this.waitingTeam.setAllowFriendlyFire(true);
		this.waitingTeam.setCanSeeFriendlyInvisibles(false);
		this.waitingTeam.setPrefix(ChatColor.WHITE.toString());

		// Create the living team
		this.livingTeam = this.scoreboard.registerNewTeam("Living");
		this.livingTeam.setAllowFriendlyFire(true);
		this.livingTeam.setCanSeeFriendlyInvisibles(false);
		this.livingTeam.setPrefix(ChatColor.GREEN.toString());
		
		// Create the dead team
		this.deadTeam = this.scoreboard.registerNewTeam("Dead");
		this.deadTeam.setAllowFriendlyFire(true);
		this.deadTeam.setCanSeeFriendlyInvisibles(false);
		this.deadTeam.setPrefix(ChatColor.RED.toString() + ChatColor.STRIKETHROUGH);
	}
	
	/**
	 * Add a player to the scoreboard
	 * 
	 * @param player	The player to add to the scoreboard
	 */
	public void addPlayer(final Player player) {
		
		// Store the current scoreboard for the player
		final Scoreboard original = player.getScoreboard();
		if (original != null) {
			this.originalScoreboard.put(player.getName(), original);
		}
		
		this.activePlayers.add(player.getName());
		
		// Set the players scoreboard and and them too the team
		player.setScoreboard(this.scoreboard);
		this.waitingTeam.addEntry(player.getName());
		
		// Set the players score to zero, then increase it
		final Score score = this.sidebarObjective.getScore(player.getDisplayName());
		score.setScore(1);
		
		final Objective sidebarObjective = this.sidebarObjective;
        Bukkit.getScheduler().runTaskLater(GameManager.getInstance().getPlugin(), () -> sidebarObjective.getScore(player.getDisplayName()).setScore(0), 1L);
        
        this.updateSidebarTitle();
	}
	
	/**
	 * Remove a player from the scoreboard
	 * 
	 * @param player	The player to remove from the scoreboard
	 */
	public void removePlayer(final Player player) {
		
		// remove the player from the team
        this.waitingTeam.removeEntry(player.getName());
        this.livingTeam.removeEntry(player.getName());
        this.deadTeam.removeEntry(player.getName());
        this.scoreboard.resetScores(player.getDisplayName());
		
		// Restore the players scoreboard
		final Scoreboard original = this.originalScoreboard.get(player.getName());
		if (original != null) {
			player.setScoreboard(original);
            this.originalScoreboard.remove(player.getName());
		}
        
        this.activePlayers.remove(player.getName());
        
        this.updateSidebarTitle();
	}
	
	/**
	 * Add a scoreboard for a player
	 * 
	 * @param player	The player to add a scoreboard to
	 */
	public void addScoreboard(final Player player) {
		// Store the current scoreboard for the player
		final Scoreboard original = player.getScoreboard();
		if (original != null) {
			this.originalScoreboard.put(player.getName(), original);
		}
		
		// Set the players scoreboard and and them too the team
		player.setScoreboard(this.scoreboard);
	}
	
	/**
	 * Remove a scoreboard for a player
	 * 
	 * @param player	The player to remove a scoreboard from
	 */
	public void removeScoreboard(final Player player) {
		// Restore the players scoreboard
		final Scoreboard original = this.originalScoreboard.get(player.getName());
		if (original != null) {
			player.setScoreboard(original);
            this.originalScoreboard.remove(player.getName());
		}
	}

	/**
	 * Update the title of the sidebar objective
	 */
	private void updateSidebarTitle() {
		final int noofPlayers = this.activePlayers.size();
		final int maxPlayers = SettingsManager.getInstance().getSpawnCount(this.gameID);
		final String gameName = GameManager.getInstance().getGame(this.gameID).getName();
        
        this.sidebarObjective.setDisplayName(ChatColor.GOLD + gameName + " (" + noofPlayers + "/" + maxPlayers + ")");
	}

	/**
	 * Increase a player's score on the scoreboard
	 * 
	 * @param player	The player to increase the score of
	 */
	public void incScore(final Player player) {
		// Set the players score to zero, then increase it
		final Score score = this.sidebarObjective.getScore(player.getDisplayName());
		if (score != null) {
			score.setScore(score.getScore() + 1);
		}
	}
	
	public void playerLiving(final Player player) {
        this.waitingTeam.removeEntry(player.getName());
        this.deadTeam.removeEntry(player.getName());
        this.livingTeam.addEntry(player.getName());
	}

	public void playerDead(final Player player) {
		// If we have too many players on scoreboard, it doesn't show
		// So keep removing players until we are below the limit
		final int players = this.waitingTeam.getSize() + this.livingTeam.getSize() + this.deadTeam.getSize();
		if (players > 15) {
			// Remove the player completely from all teams
            this.removePlayer(player);
		} else {
			// Move player to "dead" team
            this.waitingTeam.removeEntry(player.getName());
            this.livingTeam.removeEntry(player.getName());
            this.deadTeam.addEntry(player.getName());
			
			// Restore the players scoreboard
			final Scoreboard original = this.originalScoreboard.get(player.getName());
			if (original != null) {
				player.setScoreboard(original);
                this.originalScoreboard.remove(player.getName());
			}
            this.activePlayers.remove(player.getName());
            this.updateSidebarTitle();
		}
	}
}
