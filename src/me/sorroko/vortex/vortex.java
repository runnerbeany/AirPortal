package me.sorroko.vortex;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

public class Vortex {
public Location location;
public String height;
public String destination;
public Vortex(Location loc,String portalheight,String destinationname){
	location=loc;
	height=portalheight;
	destination=destinationname;
}
public Vortex(ConfigurationSection csec){
	height=csec.getString("height");
	destination=csec.getString("teleport");
	String[] l = csec.getString("loc").split(":");
	if(l.length == 4){
		World w = Bukkit.getWorld(l[0]);
		location= new Location(w, Integer.parseInt(l[1]), Integer.parseInt(l[2]), Integer.parseInt(l[3]));
	}
}

public ConfigurationSection save(ConfigurationSection csec){
	csec.set("height", height);
	csec.set("loc", location.getWorld().getName() + ":" + location.getBlockX() + ":" + location.getBlockY() + ":" + location.getBlockZ());
	csec.set("teleport", destination);
	return csec;
}
}
