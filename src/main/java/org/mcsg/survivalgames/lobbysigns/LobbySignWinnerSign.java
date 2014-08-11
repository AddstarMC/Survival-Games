package org.mcsg.survivalgames.lobbysigns;

import java.io.IOException;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.mcsg.survivalgames.GameManager;
import org.mcsg.survivalgames.MessageManager;

public class LobbySignWinnerSign extends LobbySign {

    private String m_lastWinnerName = null;

    public LobbySignWinnerSign(Sign sign, int gameId) {
        super(sign.getLocation(), gameId, LobbySignType.WinnerSign);                
    }

    public LobbySignWinnerSign(int gameId) {
        super(gameId, LobbySignType.WinnerSign);
    }
    
    @Override
    public void save(FileConfiguration config) {
        super.save(config);
        config.set("lobby.sign.winnerName", m_lastWinnerName);
    }
    
    @Override
    public void load(FileConfiguration config) {
        super.load(config);
        m_lastWinnerName = config.getString("lobby.sign.winnerName", null);
    }

    @Override
    public void execute(Player player) {
        if (m_lastWinnerName == null)
            return;
        
        MessageManager.getInstance().sendMessage(
                MessageManager.PrefixType.INFO, 
                "The last player to win '" + this.getGame().getName() + "' was " + m_lastWinnerName, 
                player);
    }

    @Override
    public void update() {
        
        if (m_lastWinnerName == null) {
            return;
        }
        
        // Change the player head to the last known winner
        Block block = this.getLocation().getBlock();
        BlockState state = block.getState();
        
        if (!(state instanceof Sign)) {
            return;
        }
        
        Sign sign = getSign();
        sign.setLine(3, m_lastWinnerName);
        sign.update();
    }

    @Override
    public String[] setSignContent(String[] lines) {
        lines[0] = "The last winner on";
        lines[1] = GameManager.getInstance().getGame(gameId).getName();
        lines[2] = "was";
        lines[3] = ChatColor.DARK_GREEN + m_lastWinnerName.substring(14);
        return lines;
    }
    
    public void setWinner(String winner) {
        m_lastWinnerName = winner;
        
        FileConfiguration config = YamlConfiguration.loadConfiguration(this.getSaveFile());
        this.save(config);
        try {
            config.save(this.getSaveFile());
        } catch (IOException e) {}
    }
}
