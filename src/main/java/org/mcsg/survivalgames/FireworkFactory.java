package org.mcsg.survivalgames;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.Bukkit;
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
	static public void LaunchFirework(final Location spawnLocation, final FireworkEffect.Type type, final int power, final ArrayList<Color> colors, final ArrayList<Color> fadecolors, final boolean flicker, final boolean trail, final int launchdelay, final int detonatedelay) {
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(GameManager.getInstance().getPlugin(), () -> {
			final Firework firework = (Firework) spawnLocation.getWorld().spawnEntity(spawnLocation, EntityType.FIREWORK);
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
			if (detonatedelay > 0) {
				// Detonate next tick
				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(GameManager.getInstance().getPlugin(), firework::detonate, detonatedelay);
			}
		}, launchdelay);
	}
	
	/*
	 * Launch a firework at a given location with specified properties
	 * Will use same fade color as color and have trail and flicker enabled
	 */
	static public void LaunchFirework(Location spawnLocation, FireworkEffect.Type type, int power, Color color, int launchdelay, int detonatedelay) {
        ArrayList<Color> colors = new ArrayList<>();
		colors.add(color);
		
		LaunchFirework(spawnLocation, type, power, colors, colors, true, true, launchdelay, detonatedelay);
	}
	
	static public void LaunchFirework(Location spawnLocation, FireworkEffect.Type type, int power, Color[] colors, boolean flicker, boolean trail, int launchdelay, int detonatedelay) {
        ArrayList<Color> colorlist = new ArrayList<>(Arrays.asList(colors));
		LaunchFirework(spawnLocation, type, power, colorlist, colorlist, flicker, trail, launchdelay, detonatedelay);
	}
}