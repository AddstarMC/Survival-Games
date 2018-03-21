package org.mcsg.survivalgames.lobbysigns;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.Vector;
import org.mcsg.survivalgames.GameManager;
import org.mcsg.survivalgames.SurvivalGames;

public class LobbySignManager {

    private HashMap<Vector, LobbySign> signs = new HashMap<>();
	private int noofPlayerListSigns = 0;
	private File signFolder = null;
	
	public LobbySignManager() {
		signFolder = new File(GameManager.getInstance().getPlugin().getDataFolder() + File.separator + "signs");
		signFolder.mkdirs();
	}

	public void addSign(LobbySign newLobbySign) {
		
		if (newLobbySign.getType() == LobbySignType.PlayerList) {
			((LobbySignPlayerList)newLobbySign).setRange(noofPlayerListSigns * 4);
			noofPlayerListSigns++;
		}
		
		Vector location = newLobbySign.getLocation().toVector();
		signs.put(location, newLobbySign);
		saveSigns();
	}
	
	public void removeSign(LobbySign lobbySign) {
		Vector location = lobbySign.getLocation().toVector();
		if (signs.containsKey(location)) {		
			LobbySign sign = signs.get(location);
			signs.remove(location);
			
			if (sign.getType() == LobbySignType.PlayerList) {
				noofPlayerListSigns = 0;
				for (LobbySign playerListSign : signs.values()) {
					if (playerListSign.getType() == LobbySignType.PlayerList) {
						((LobbySignPlayerList)playerListSign).setRange(noofPlayerListSigns * 4);
						noofPlayerListSigns++;
					}
				}
			}
			
			sign.getSaveFile().delete();
		}
	}

	public LobbySign getSign(Location location) {
		return signs.get(location.toVector());
	}
	
	private void saveSigns() {

		for (LobbySign sign : signs.values()) {
			
			File signFile = sign.getSaveFile();
			if (signFile != null) {
				FileConfiguration config = YamlConfiguration.loadConfiguration(signFile);
				sign.save(config);
				try {
					config.save(signFile);
                } catch (IOException ignored) {
                }
				continue;
			}
			
			String fileName = signFolder + File.separator + "sign-game" + sign.getGame().getID() + "-" + sign.getType();
			signFile = new File(fileName + ".yml");
			
			int index = 1;
			while (signFile.exists()) {
				fileName = signFolder + File.separator + "sign-game" + sign.getGame().getID() + "-" + sign.getType() + "-" + index;
				signFile = new File(fileName + ".yml");
				index++;
			}

			try {
				signFile.createNewFile();
			} catch(Exception ex) {
				SurvivalGames.$(0, Level.SEVERE, "Could not create sign at '" + signFile.getAbsolutePath() + "'.");
				return;
			}
			
			FileConfiguration config = YamlConfiguration.loadConfiguration(signFile);
			sign.save(config);
			sign.setSaveFile(signFile);
			try {
				config.save(signFile);
            } catch (IOException ignored) {
            }
		}
	}
	
	public void loadSigns() {
		signs.clear();
		
		File[] signFiles = signFolder.listFiles();
		for (File file : signFiles) {
			String fileName = file.getName();
			fileName = fileName.replace("sign-game", "");
			
			String gameIdString = fileName.substring(0, fileName.indexOf("-"));
			fileName = fileName.replace(gameIdString + "-", "");
			
			int trimIndex = fileName.indexOf("-");
			String signTypeString = fileName.substring(0, trimIndex == -1 ? fileName.indexOf(".") : trimIndex);

			int gameId = -1;
			try {
				gameId = Integer.parseInt(gameIdString);
			} catch(Exception ex) {
				SurvivalGames.$(0, Level.SEVERE, "Failed to '" + file.getName() + "' invalid game id.");
				continue;
			}
			
			LobbySignType signType = LobbySignType.valueOf(signTypeString);
			
			LobbySign sign = null;

            switch (signType) {
                case Join:
                    sign = new LobbySignJoin(gameId);
                    break;
                case PlayerList:
                    sign = new LobbySignPlayerList(gameId);
                    break;
                case Players:
                    sign = new LobbySignPlayers(gameId);
                    break;
                case State:
                    sign = new LobbySignState(gameId);
                    break;
                case Winner:
                    sign = new LobbySignWinner(gameId);
                    break;
                case WinnerSign:
                    sign = new LobbySignWinnerSign(gameId);
                    break;
                default:
                    SurvivalGames.$(0, Level.SEVERE, "Invalid sign type! " + file.getName());
                    continue;
			}
			
			FileConfiguration config = YamlConfiguration.loadConfiguration(file);
			sign.load(config);
			sign.setSaveFile(file);
			
			signs.put(sign.getLocation().toVector(), sign);
		}
		
	}
	
	public void updateSigns() {
		for (LobbySign sign : signs.values()) {
			sign.update();
		}
	}
	
	public void updateSigns(int gameId) {
		for (LobbySign sign : signs.values()) {
			if (sign.getGame().getID() == gameId) {
				sign.update();
			}
		}
	}

	public void removeArena(int arena) {
		for (LobbySign sign : signs.values()) {
			if (sign.getGame().getID() == arena) {
				removeSign(sign);
				sign.getLocation().getBlock().breakNaturally();
			}
		}
	}

	public List<LobbySign> getSignsByType(int gameID, LobbySignType type) {
        ArrayList<LobbySign> typeSigns = new ArrayList<>();
		
		for (LobbySign sign : signs.values()) {
			if (sign.getGame().getID() == gameID && sign.getType() == type) {
				typeSigns.add(sign);
			}
		}
		
		return typeSigns;
	}

}
