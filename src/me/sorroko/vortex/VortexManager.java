package me.sorroko.vortex;

import java.util.HashMap;

public class VortexManager {
	AirPortal plugin;
	public VortexManager(AirPortal instance){
		plugin=instance;
	}
private HashMap<String,vortex> vortexii=new HashMap<String,vortex>();

public vortex getVortex(String name){
	return vortexii.get(name);
}

public void setVortex(String name,vortex v){
	vortexii.put(name, v);
}

public boolean vortexExists(String name){
	return vortexii.containsKey(name);
}

public void loadVortexii(){
	if(!plugin.mainConf.isSet("vortex_locs")) plugin.mainConf.createSection("vortex_locs");
	for(String k:plugin.mainConf.getKeys(false)){
		vortexii.put(k, new vortex(plugin.mainConf.getConfigurationSection("vortex_locs").getConfigurationSection(k)));
	}
}

public void saveConfig(){
	for(String k:vortexii.keySet()){
		plugin.mainConf.getConfigurationSection("vortex_locs").createSection(k);
		plugin.mainConf.getConfigurationSection("vortex_locs").set(k, vortexii.get(k).save(plugin.mainConf.getConfigurationSection("vortex_locs").getConfigurationSection(k)));
	}
		
}

}