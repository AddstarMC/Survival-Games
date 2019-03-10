package org.mcsg.survivalgames.util;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import static org.junit.Assert.*;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 6/03/2019.
 */
public class ItemReaderTest {

    public void read() {
        String name = "Archer";
        File resourcesDirectory = new File("src/test/resources");
        File kits = new File(resourcesDirectory, "kits.yml");
        FileConfiguration f = YamlConfiguration.loadConfiguration(kits);
        Double cost = f.getDouble("kits." + name + ".cost", 0);
        assertNotNull(cost);
        ItemStack icon = ItemReader.read(f.getString("kits." + name + ".icon"));
        assertNotNull(icon);
    }

    @Test
    public void getFriendlyItemName() {
    }
}