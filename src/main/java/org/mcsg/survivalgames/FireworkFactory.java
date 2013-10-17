package org.mcsg.survivalgames;

import java.util.ArrayList;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

public class FireworkFactory {

	/*
	 * Launch a firework at a given location with specified properties
	 */
	static public void LaunchFirework(Location spawnLocation, FireworkEffect.Type type, int power, ArrayList<Color> colors, ArrayList<Color> fadecolors, boolean flicker, boolean trail) {

        Firework firework = (Firework) spawnLocation.getWorld().spawnEntity(spawnLocation, EntityType.FIREWORK);
        FireworkMeta metadata = firework.getFireworkMeta();
        
        Builder builder = FireworkEffect.builder();
        builder.with(type);
        builder.flicker(flicker);
        builder.trail(trail);
        builder.withColor(colors);
        builder.withFade(fadecolors);
        
        FireworkEffect effect = builder.build();
        metadata.addEffect(effect);
        metadata.setPower(power);
        
        firework.setFireworkMeta(metadata);
	}
	
	/*
	 * Launch a firework at a given location with specified properties
	 * Will use same fade color as color and have trail and flicker enabled
	 */
	static public void LaunchFirework(Location spawnLocation, FireworkEffect.Type type, int power, Color color) {
		ArrayList<Color> colors = new ArrayList<Color>();
		colors.add(color);
		
		LaunchFirework(spawnLocation, type, power, colors, colors, true, true);
	}
	
}
