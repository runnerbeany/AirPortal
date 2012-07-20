package me.sorroko.vortex.listeners;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import me.sorroko.vortex.AirPortal;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;


public class PlayerListener implements Listener {

	AirPortal plugin;
	public PlayerListener(AirPortal instance) {
		plugin = instance;
	}
	
	public void updateVortexs(){
	plugin.VortexManager.loadVortexii();
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event){
		final Player player = event.getPlayer();
		if(!plugin.hasPermission(player, "use")) return;
		Location to = event.getTo();
		Location from = event.getFrom();
		if(to.getBlockX() != from.getBlockX() 
				|| to.getBlockY() != from.getBlockY() 
				|| to.getBlockZ() != from.getBlockZ()){
			
			if(to.getBlockY() == from.getBlockY() 
					&& !to.getBlock().getType().equals(Material.AIR) 
					&& plugin.vortex_players.containsKey(player)){
				plugin.vortex_players.remove(player);
			}
			for(Entry<String, me.sorroko.vortex.Vortex> entry: plugin.VortexManager.getVortexEntries()){
				String name = entry.getKey();
				Location loc = entry.getValue().location;
				if(to.getWorld() == loc.getWorld()
						&& (loc.getBlockX() - 2) <= to.getBlockX() && to.getBlockX() <= (loc.getBlockX() + 2)
						&& (loc.getBlockZ() - 2) <= to.getBlockZ() && to.getBlockZ() <= (loc.getBlockZ() + 2)
						&& (loc.getBlockY() - 1) <= to.getBlockY() && to.getBlockY() <= (loc.getBlockY() + 1)
						&& from.getBlockY() == to.getBlockY()){

					final String teleport = plugin.VortexManager.getVortex(name).destination;
					

					//cancel if the player is waiting and is still in a vortex
					if(plugin.vortex_waiting.containsKey(player) && plugin.vortex_waiting.get(player) != null)
						return;

					final int multiplier;
					String height = plugin.VortexManager.getVortex(name).height;
					if(height != null){
						if(height.equalsIgnoreCase("low")){
							multiplier = 2;
						} else if(height.equalsIgnoreCase("space")){
							multiplier = 10;
						} else {
							multiplier = 6;
						}
					} else {
						multiplier = 6;
					}
					//schedule new vortex
					player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 200, 5));
					plugin.vortex_waiting.put(player, Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){
						
						@Override
						public void run() {

							player.setVelocity(player.getVelocity().add(new Vector(0, 1, 0)).multiply(multiplier));
							plugin.vortex_waiting.remove(player);
							plugin.vortex_players.put(player, true);
							if(teleport != null){
								if(!plugin.VortexManager.vortexExists(teleport)) return;
								final Location tl = plugin.VortexManager.getVortex(teleport).location.clone();
								Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){

									@Override
									public void run() {
										player.teleport(tl.add(0, 4, 0));
										player.setVelocity(new Vector(0, 0, 0));
									}
								}, 10L);
							}
						}
						
					}, plugin.mainConf.getInt("launchDelay", 1) * 20L));
					return;
				}
			}
			
			//loop didnt return, cancel waiting (if waiting)
			if(plugin.vortex_waiting.containsKey(player)){
				if(plugin.vortex_waiting.get(player) != null){
					int i = plugin.vortex_waiting.get(player);
					Bukkit.getScheduler().cancelTask(i);
					plugin.vortex_waiting.put(player, null);
					player.removePotionEffect(PotionEffectType.CONFUSION);
				}
			}
		}
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event){
		if(event.getEntity() instanceof Player){
			Player player = (Player) event.getEntity();
			if(event.getCause().equals(DamageCause.FALL)){
				if(plugin.vortex_players.containsKey(player.getName()) 
						&& plugin.vortex_players.get(player.getName())){
					event.setCancelled(true);
				}
			}
		}
	}

}
