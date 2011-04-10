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
    		player.put("x", (double)p.getLocation().getX());
    		player.put("y", (double)p.getLocation().getY());
    		player.put("z", (double)p.getLocation().getZ());
    		player.put("holding", (int)p.getItemInHand().getTypeId());
    		player.put("health", (int)p.getHealth());
    		player.put("air", (int)p.getRemainingAir());
    		player.put("dead", (boolean)p.isDead());
    		
    		if(inventory)
    		{
    			player.put("inventory", this.Inventories(p));
    			System.out.println("[SHEEPINFO] Herp Last");
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
    		if(i.getTypeId() > 0)
    		{
    			System.out.println("[SHEEPINFO] Herp 1");
    			item.put("id", (int)i.getTypeId());
    			System.out.println("[SHEEPINFO] Herp 2");
    			item.put("amount", (int)i.getAmount());
    			System.out.println("[SHEEPINFO] Herp 3");
    			item.put("durability", (int)i.getDurability());
    			System.out.println("[SHEEPINFO] Herp 4");
    			inventory.add(item);
    			System.out.println("[SHEEPINFO] Herp 5");
    			item.clear();
    		}
    		System.out.println("[SHEEPINFO] Herp 6 Repeat");
    	}
    	System.out.println("[SHEEPINFO] Herp 7");
    	return inventory;
    }
}