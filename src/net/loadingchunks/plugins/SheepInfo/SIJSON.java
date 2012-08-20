package net.loadingchunks.plugins.SheepInfo;

import java.util.Map;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingSand;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Item;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.PoweredMinecart;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Squid;
import org.bukkit.entity.StorageMinecart;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.json.simple.*;

import net.loadingchunks.plugins.SheepInfo.SheepInfo;


public class SIJSON {
    private final SheepInfo plugin;

    public SIJSON(SheepInfo instance) {
        this.plugin = instance;
    }
    
    public JSONObject Info(World w, SheepInfo plugin) {
    	JSONObject object = new JSONObject();
    	try {
    		object.put("name", w.getName());
    		// we already have the players array, we don't /need/ this.
    		// object.put("players", (int)w.getPlayers().size());
    		object.put("chunks", (int)w.getLoadedChunks().length);
    		object.put("entities", (int)w.getLivingEntities().size());
    		object.put("max_mem", (long)Runtime.getRuntime().maxMemory());
    		object.put("free_mem", (long)Runtime.getRuntime().freeMemory());
    		object.put("time", (long)w.getTime());
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
    	return object;
    }
    
    public JSONArray Players(World w, Boolean inventory, SheepInfo plugin) {
    	JSONArray object = new JSONArray();
    	JSONObject player;
		for (Player p : w.getPlayers()) {
    		player = new JSONObject();
    		try {
    			p.getAddress().getHostName();
    		}
    		catch (NullPointerException e) {
    			continue;
    		}
    		try {
    			player.put("name", p.getName());
    			player.put("nickname", ChatColor.stripColor(p.getDisplayName()));
    			if(this.plugin.getConfig().getBoolean("sheep.keys.ip"))
    				player.put("ip", p.getAddress().getHostName());
    			player.put("x", (double)p.getLocation().getX());
    			player.put("y", (double)p.getLocation().getY());
    			player.put("z", (double)p.getLocation().getZ());
    			player.put("holding", (int)p.getItemInHand().getTypeId());
    			player.put("holding_dmg", (int)p.getItemInHand().getDurability());
    			player.put("health", (int)p.getHealth());
    			player.put("hunger", (int)p.getFoodLevel());
    			player.put("air", (int)p.getRemainingAir());
    			player.put("dead", (boolean)p.isDead());
    			player.put("level", (int)p.getLevel());
    			player.put("total_exp", (int)p.getTotalExperience());
    			player.put("potion_fx", this.PotionEffects(p));
    		}
    		catch (Exception e) {
    			
    		}

    		if (inventory) {
    			player.put("inventory", this.Inventories(p));
    		}
    		
    		object.add(player);
    	}
    	return object;
    }
    
    public JSONObject Inventories(Player p) {
    	JSONObject inventory = new JSONObject();
    	
    	ItemStack[] list = p.getInventory().getContents();
    	
    	for (int j = 0; j < list.length; j++) {
    		if (list[j] != null) {
    			JSONObject item = new JSONObject();
    			if (list[j].getTypeId() > 0) {
    				item.put("id", (int)list[j].getTypeId());
    				item.put("amount", (int)list[j].getAmount());
    				item.put("durability", (int)list[j].getDurability());
    				//item.put("slot", (int)j);
    				item.put("enchantments", this.Enchantments(list[j]));
        			inventory.put((int)j, item);
				}
    		}
    	}
    	return inventory;
    }
    
    private JSONObject Enchantments(ItemStack item) {
    	JSONObject enchants_json = new JSONObject();
		Map<Enchantment, Integer> enchants = item.getEnchantments();
		Set<Enchantment> enchant_list = enchants.keySet();
		for (Enchantment key : enchant_list) {
			enchants_json.put(key.getId(), enchants.get(key));
		}
		return enchants_json;
    }
    
    private JSONArray PotionEffects(Player p) {
    	JSONArray potion_effects = new JSONArray();
    	for (PotionEffect pe : p.getActivePotionEffects()) {
    		JSONObject effect = new JSONObject();
    		effect.put("id", pe.getType().getId());
    		effect.put("duration", pe.getDuration());
    		effect.put("amplifier", pe.getAmplifier());
    		potion_effects.add(effect);
    	}
    	return potion_effects;
    }
    
    public JSONObject Entities(World w) {
    	JSONObject object = new JSONObject();
    	int creepers = 0,minecarts = 0,boats = 0,zombies = 0,pickups = 0,slimes = 0,arrows = 0,projectiles = 0,active_tnt = 0,falling = 0,squids = 0,wolves = 0,ghasts = 0;
   	
    	for (Entity e : w.getEntities()) {
    		if (e instanceof Creeper)
    			creepers++;
    		else if (e instanceof Minecart || e instanceof PoweredMinecart || e instanceof StorageMinecart)
    			minecarts++;
    		else if (e instanceof Boat)
    			boats++;
    		else if (e instanceof Zombie)
    			zombies++;
    		else if (e instanceof Item)
    			pickups++;
    		else if (e instanceof Slime)
    			slimes++;
    		else if (e instanceof Arrow)
    			arrows++;
    		else if (e instanceof Snowball || e instanceof Egg)
    			projectiles++;
    		else if (e instanceof TNTPrimed)
    			active_tnt++;
    		else if (e instanceof FallingSand)
    			falling++;
    		else if (e instanceof Squid)
    			squids++;
    		else if (e instanceof Wolf)
    			wolves++;
    		else if (e instanceof Ghast)
    			ghasts++;
    	}
    	
    	object.put("creepers", creepers);
    	object.put("minecarts", minecarts);
    	object.put("boats", boats);
    	object.put("zombies", zombies);
    	object.put("pickups", pickups);
    	object.put("slimes", slimes);
    	object.put("arrows", arrows);
    	object.put("projectiles", projectiles);
    	object.put("active_tnt", active_tnt);
    	object.put("falling", falling);
    	object.put("squid", squids);
    	object.put("wolves", wolves);
    	object.put("ghasts", ghasts);
    	return object;
    }
}