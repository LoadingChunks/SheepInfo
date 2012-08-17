
package net.loadingchunks.plugins.SheepInfo;

import java.util.HashMap;
import java.util.List;

import org.bukkit.World;
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
	public List<World> worlds;
	public boolean disableMoney = false;
	public boolean disableGroups = false;

    public void onDisable() {
        System.out.println("[SHEEPINFO] Stopping...");
        this.httpd.Kill();
        System.out.println("[SHEEPINFO] Killed HTTPD Server.");
    }

    public void onEnable() {
        // Get the config.
        
        System.out.println("[SHEEPINFO] Loading config file plugins/SheepInfo/config.yml...");
        
        this.getConfig();
        
        // Although we're not actually checking... GG
        System.out.println("Loaded SheepInfo Config Successfully!");

        // IT LIES!
        System.out.println("SheepInfo Config saved to memory.");
        
        worlds = this.getServer().getWorlds();
        System.out.println("[SHEEPINFO] Generated Worlds List.");

        PluginDescriptionFile pdfFile = this.getDescription();
        System.out.println(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
        
        System.out.println("[SHEEPINFO] Starting SheepInfo HTTPD on " + this.getConfig().getString("sheep.bind.addr") + ":" + this.getConfig().getInt("sheep.bind.port"));
        if(!httpd.Listen())
        	System.out.println("[SHEEPINFO] Error starting SheepInfo HTTPD!");
        else
        	System.out.println("[SHEEPINFO] SheepInfo HTTPD successfully listening!");
    }
}
