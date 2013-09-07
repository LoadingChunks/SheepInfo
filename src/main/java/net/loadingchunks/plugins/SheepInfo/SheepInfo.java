package net.loadingchunks.plugins.SheepInfo;

import java.util.HashMap;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * SheepInfo stats reporting system plugin for Bukkit
 *
 * @author Cue, origamiguy
 */
public class SheepInfo extends JavaPlugin {
	private final HashMap<String, String> mSIConfig = new HashMap<String, String>();
	private final SIHTTPD mHttpd = new SIHTTPD(this);

    public void onEnable() {
        // Get the config.
        
    	getLogger().info("Loading config file plugins/SheepInfo/config.yml...");
        
        getConfig();
        
        // Although we're not actually checking... GG
        getLogger().info("Loaded SheepInfo Config Successfully!");
        
        // IT LIES!
        getLogger().info("SheepInfo Config saved to memory.");
        
        PluginDescriptionFile pdfFile = getDescription();
        getLogger().info(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
        
        getLogger().info("Starting SheepInfo HTTPD on " + getConfig().getString("sheep.bind.addr") + ":" + getConfig().getInt("sheep.bind.port"));
        if(!mHttpd.listen())
        	getLogger().info("Error starting SheepInfo HTTPD!");
        else
        	getLogger().info("SheepInfo HTTPD successfully listening!");
    }

    public void onDisable() {
    	getLogger().info("Stopping...");
        mHttpd.kill();
        getLogger().info("Killed HTTPD Server.");
    }
}
