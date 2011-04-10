package net.loadingchunks.plugins.SheepInfo;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executor;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.json.simple.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import net.loadingchunks.plugins.SheepInfo.SheepInfo;


public class SIJSON {
    private final SheepInfo plugin;

    public SIJSON (SheepInfo plugin) {
        this.plugin = plugin;
    }
    
    public JSONObject Info(World w)
    {
    	JSONObject object = new JSONObject();
    	object.put("name", w.getName());
    	object.put("players", new Integer(w.getPlayers().size()));
    	object.put("chunks", new Integer(w.getLoadedChunks().length));
    	object.put("entities", new Integer(w.getLivingEntities().size()));
    	object.put("max_mem", new Long( Runtime.getRuntime().maxMemory()));
    	object.put("free_mem", new Long(Runtime.getRuntime().freeMemory()));
    	object.put("time", new Long(w.getTime()));
    	return object;
    }
    
    public JSONArray Players(World w, Boolean inventory)
    {
    	JSONArray object = new JSONArray();
    	JSONObject player = new JSONObject();
    	
    	for ( Player p : w.getPlayers())
    	{
    		player.clear();
    		player.put("name", p.getName());
    		player.put("nickname", ChatColor.stripColor(p.getDisplayName()));
    		player.put("ip", p.getAddress().getHostName());
    		player.put("x", p.getLocation().getX());
    		player.put("y", p.getLocation().getY());
    		player.put("z", p.getLocation().getZ());
    		player.put("holding", p.getItemInHand().getTypeId());
    		player.put("health", p.getHealth());
    		player.put("air", p.getRemainingAir());
    		player.put("dead", p.isDead());
    		
    		if(inventory)
    		{
    			System.out.println("[SHEEPINFO] Step 1...");
    			player.put("inventory", this.Inventories(p));
    		}
    		
    		object.add(player);
    	}
    	return object;
    }
    
    public JSONArray Inventories(Player p)
    {
    	JSONArray inventory = new JSONArray();
    	JSONObject item = new JSONObject();

    	for ( ItemStack i : p.getInventory().getContents() )
   		{
   			item.clear();
   			item.put("id", i.getTypeId());
   			item.put("amount", i.getAmount());
    		item.put("durability", i.getDurability());
    		System.out.println("[SHEEPINFO] Step 2...");
    		inventory.add(item);
    	}
    	System.out.println("[SHEEPINFO] Step 3...");
    	return inventory;
    }
}