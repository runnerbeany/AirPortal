package me.sorroko.vortex;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

public class VortexManager {
	AirPortal plugin;
	public VortexManager(AirPortal instance){
		plugin=instance;
	}
private HashMap<String,Vortex> vortexii=new HashMap<String,Vortex>();

public Vortex getVortex(String name){
	return vortexii.get(name);
}

public Set<Entry<String,Vortex>> getVortexEntries(){
	return vortexii.entrySet();
}

public void setVortex(String name,Vortex v){
	vortexii.put(name, v);
}

public boolean vortexExists(String name){
	return vortexii.containsKey(name);
}

public void removeVortex(String name){
	vortexii.remove(name);
}

public void loadVortexii(){
	if(!plugin.mainConf.isSet("vortex_locs")) plugin.mainConf.createSection("vortex_locs");
	for(String k:plugin.mainConf.getKeys(false)){
		vortexii.put(k, new Vortex(plugin.mainConf.getConfigurationSection("vortex_locs").getConfigurationSection(k)));
	}
}

public void saveConfig(){
	for(String k:vortexii.keySet()){
		plugin.mainConf.getConfigurationSection("vortex_locs").createSection(k);
		plugin.mainConf.getConfigurationSection("vortex_locs").set(k, vortexii.get(k).save(plugin.mainConf.getConfigurationSection("vortex_locs").getConfigurationSection(k)));
	}
		
}

}