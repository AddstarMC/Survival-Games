package org.mcsg.survivalgames.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;


import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.mcsg.survivalgames.SettingsManager;




public class DatabaseManager {
    private  Connection conn;
    private  Logger log;
    private  static DatabaseManager instance = new DatabaseManager();

    private DatabaseManager(){

    }

    public static DatabaseManager getInstance(){
        return instance;
    }


    public boolean setup(Plugin p) {
        log = p.getLogger();
        return connect();
    }


    public  Connection getMysqlConnection()
    {
        return conn;
    }

    public boolean connectToDB(String host, int port, String db, Properties props) {
        String url = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            url = "jdbc:mysql://" + host + ":" + port + "/" + db;
            conn = DriverManager.getConnection(url, props);
            if(conn.isValid(30)) {
                return true;
            } else {
                return false;
            }
        } catch (ClassNotFoundException e) {
            log.warning("Couldn't start MySQL Driver. URL :" + url);
            log.warning("Couldn't start MySQL Driver. Stopping...\n" + e.getMessage());

            return false;
        } catch (SQLException e) {
            log.warning("Couldn't start MySQL Driver. URL :" + url);
            log.warning("Couldn't connect to MySQL database. Stopping...\n" + e.getMessage());
            return false;
        } catch (NullPointerException e) {
            log.warning("Couldn't start MySQL Driver. URL :" + url);
            log.warning("Couldn't start MySQL Driver. Properties :" + props);
            log.warning("Couldn't connect to MySQL database. Stopping...\n" + e.getMessage());
            return false;
        }
    }
    public PreparedStatement createStatement(String query)
    {
        int times = 0;
        PreparedStatement p = null;
            try
            {
                times ++;
                p =  conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            }
            catch (SQLException e)
            {
                if (times == 5){
                   // System.out.println("[SurvivalGames][SQL ERROR] ATTEMPTED TO CONNECT TO DATABASE 5 TIMES AND FAILED! CONNECTION LOST.");
                    return null;
                }
                connect();
            }
        


        return p;


    }

    public Statement createStatement()
    {
        try
        {
            return conn.createStatement();
        }
        catch (SQLException e)
        {
            return null;
        }
    }


    public boolean connect()
    {
        FileConfiguration c = SettingsManager.getInstance().getConfig();

        Properties props = new Properties();
        props.put("user", c.getString("sql.user", "root"));
        props.put("password", c.getString("sql.pass", ""));
        String host =  c.getString("sql.host", "localhost");
        int port    =  c.getInt("sql.port",  3306);
        String db   =  c.getString("sql.database", "SurvivalGames");
        ConfigurationSection dbprops = c.getConfigurationSection("sql.properties");
        if (dbprops != null) {
            for (Map.Entry<String, Object> prop : dbprops.getValues(false).entrySet()) {
                if (prop.getValue() != null) {
                    props.put(prop.getKey(), prop.getValue());
                }
            }
        }
        return this.connectToDB(host, port, db, props);
    }

}
