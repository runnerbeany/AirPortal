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
	
	HashMap<String, Location> vortexs = new HashMap<String, Location>();
	
	public void updateVortexs(){
		ConfigurationSection cSelection = plugin.mainConf.getConfigurationSection("vortex_locs");
		if(cSelection == null) return;
		Set<String> vortex_list = cSelection.getKeys(false);
		if(vortex_list == null) return;
		for(String s: vortex_list){
			
			String[] l = plugin.mainConf.getString("vortex_locs." + s + ".loc").split(":");
			if(l.length == 4){
				World w = Bukkit.getWorld(l[0]);
				if(w == null) return;
				int x = Integer.parseInt(l[1]);
				int y = Integer.parseInt(l[2]);
				int z = Integer.parseInt(l[3]);
				vortexs.put(s, new Location(w, x, y, z));
			}
		}
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event){
		final Player player = event.getPlayer();
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
			for(Entry<String, Location> entry: vortexs.entrySet()){
				String name = entry.getKey();
				Location loc = entry.getValue();
				if(to.getWorld() == loc.getWorld()
						&& (loc.getBlockX() - 2) <= to.getBlockX() && to.getBlockX() <= (loc.getBlockX() + 2)
						&& (loc.getBlockZ() - 2) <= to.getBlockZ() && to.getBlockZ() <= (loc.getBlockZ() + 2)
						&& (loc.getBlockY() - 1) <= to.getBlockY() && to.getBlockY() <= (loc.getBlockY() + 1)
						&& from.getBlockY() == to.getBlockY()){

					final String teleport = plugin.mainConf.getString("vortex_locs." + name + ".teleport");
					

					//cancel if the player is waiting and is still in a vortex
					if(plugin.vortex_waiting.containsKey(player) && plugin.vortex_waiting.get(player) != null)
						return;

					final int multiplier;
					String height = plugin.mainConf.getString("vortex_locs." + name + ".height");
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
								if(!vortexs.containsKey(teleport)) return;
								final Location tl = vortexs.get(teleport).clone();
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
