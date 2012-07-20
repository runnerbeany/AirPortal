package me.sorroko.vortex;

import me.sorroko.vortex.commands.VortexCommand;
import me.sorroko.vortex.listeners.PlayerListener;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.HashMap;


public class AirPortal extends JavaPlugin {
	public YamlConfiguration mainConf;
	
	public boolean debug = false;
	
	private final ConfigUtil configUtil;
	private final Util util;
	public VortexManager VortexManager=new VortexManager(this);
	public HashMap<Player, Integer> vortex_waiting = new HashMap<Player, Integer>();
	public HashMap<Player, Boolean> vortex_players = new HashMap<Player, Boolean>();
	
	public AirPortal() {
        configUtil = new ConfigUtil(this);
        util = new Util(this);
    }
	
	public PlayerListener pListener;
	
	@Override
	public void onDisable() {
		Util.log(Util.pdfFile.getName() + " has been disabled");
		try{
		mainConf.save("config");
		}catch(IOException e){
			Util.log("[" + Util.pdfFile.getName() + "]" + "Error saving config.");
		}
	}
	
	@Override
	public void onEnable() { //enable
		Util.pdfFile = getDescription();
		Util.log("----------- " + Util.pdfFile.getName() + " has been enabled" + " -----------");
		Util.log(Util.pdfFile.getName() + " Version " + Util.pdfFile.getVersion());
		Util.log(Util.pdfFile.getName() + " By " + Util.pdfFile.getAuthors().get(0));
		
		ConfigUtil.loadConfig("config", "config");
		mainConf = ConfigUtil.getConfig("config");
		
		debug = mainConf.getBoolean("debug", false);
		
		PluginManager pm = getServer().getPluginManager();
		pListener = new PlayerListener(this);
		pm.registerEvents(pListener, this);
		
		registerCommands();
		pListener.updateVortexs();
		Util.log("Succesfully loaded.");
	}
	
	private void registerCommands(){
		getCommand("airportal").setExecutor(new VortexCommand(this));
	}

	private String basePerm = "airportal";
	
	public boolean hasPermission(String name, String perm) {
		Player player = Bukkit.getPlayer(name);
		return hasPermission(player, perm);
	}
	public boolean hasPermission(Player player, String perm) {
		if(player.hasPermission(basePerm + "." + perm) || player.hasPermission(basePerm + ".*")){
			Util.debug("Has permission for player: " + player.getName() + " and perm: " + basePerm + "." + perm);
			return true;
		}
		return false;
	}
}

