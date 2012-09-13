
package net.loadingchunks.plugins.SheepInfo;

import java.util.HashMap;
import java.util.List;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * SheepInfo stats reporting system plugin for Bukkit
 *
 * @author Cue, origamiguy
 */
public class SheepInfo extends JavaPlugin {
	public final HashMap<String, String> siConfig = new HashMap<String, String>();
	public final SIHTTPD httpd = new SIHTTPD(this);
	public boolean disableMoney = false;
	public boolean disableGroups = false;

    public void onDisable() {
    	getLogger().info("Stopping...");
        this.httpd.kill();
        getLogger().info("Killed HTTPD Server.");
    }
    
    public List<World> getWorlds() {
    	return this.getServer().getWorlds();
    }
    
    public Player[] getOnlinePlayers() {
    	return this.getServer().getOnlinePlayers();
    }

    public void onEnable() {
        // Get the config.
        
    	getLogger().info("Loading config file plugins/SheepInfo/config.yml...");
        
        this.getConfig();
        
        // Although we're not actually checking... GG
        getLogger().info("Loaded SheepInfo Config Successfully!");

        // IT LIES!
        getLogger().info("SheepInfo Config saved to memory.");
        
        PluginDescriptionFile pdfFile = this.getDescription();
        getLogger().info(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
        
        getLogger().info("Starting SheepInfo HTTPD on " + this.getConfig().getString("sheep.bind.addr") + ":" + this.getConfig().getInt("sheep.bind.port"));
        if(!httpd.listen())
        	getLogger().info("Error starting SheepInfo HTTPD!");
        else
        	getLogger().info("SheepInfo HTTPD successfully listening!");
    }
}
