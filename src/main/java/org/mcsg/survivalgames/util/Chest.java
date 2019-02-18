package org.mcsg.survivalgames.util;

import java.util.List;
import java.util.Random;

import org.bukkit.inventory.ItemStack;

public class Chest {
	
	private double m_chance = 1.0;
	private List<ItemStack> m_contents = null;
	
	public Chest() {
		
	}

	public void setChance(double chance) {
		m_chance = chance;
	}

	public void setContents(List<ItemStack> chestContents) {
		m_contents = chestContents;
	}

	public ItemStack getRandomItem(Random random) {
		return m_contents.get(random.nextInt(m_contents.size()));
	}

	public boolean useThisChest(Random random) {
		int x = (int) ((1.0 + m_chance) / m_chance);		// Convert the float to a chance integer  
		return random.nextInt(x) == 0;
	}

}
