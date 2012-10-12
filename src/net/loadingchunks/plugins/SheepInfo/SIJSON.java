package net.loadingchunks.plugins.SheepInfo;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.json.simple.*;

import net.loadingchunks.plugins.SheepInfo.SheepInfo;


public class SIJSON {
    private final SheepInfo plugin;

    public SIJSON(SheepInfo instance) {
        this.plugin = instance;
    }
    
    public JSONArray Worlds(Collection<World> worlds) {
    	JSONArray json = new JSONArray();
    	try {
    		for (World w : worlds)
    			json.add(this.World(w));
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
    	return json;
    }
    
    public JSONObject World(World world) {
    	JSONObject json = new JSONObject();
    	try {
    		json.put("name", world.getName());
    		json.put("chunks", world.getLoadedChunks().length);
    		json.put("entities", world.getLivingEntities().size());
    		json.put("players", world.getPlayers().size());
    		json.put("time", world.getTime());
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
    	return json;
    }
    
    public JSONObject Stats() {
    	JSONObject json = new JSONObject();
    	try {
    		json.put("players", this.plugin.getOnlinePlayers().length);
    		json.put("max_players", this.plugin.getServer().getMaxPlayers());
    		json.put("free_mem", Runtime.getRuntime().freeMemory());
    		json.put("max_mem", Runtime.getRuntime().maxMemory());
    		json.put("motd", this.plugin.getServer().getMotd());
    		json.put("version", this.plugin.getServer().getVersion());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
    	return json;
    }
    
    public JSONArray Players(Boolean inventory) {
    	JSONArray json = new JSONArray();
    	try {
    		for (Player p : this.plugin.getOnlinePlayers()) {
    			JSONObject p_json = this.Player(p, inventory);
    			if (p_json != null)
    				json.add(p_json);
    		}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
    	return json;
    }
    
    public JSONObject Player(Player player, Boolean inventory) {
    	JSONObject json = new JSONObject();
		try {
			player.getAddress().getHostName();
		}
		catch (NullPointerException e) {
			return null;
		}
		try {
			json.put("name", player.getName());
			json.put("nickname", ChatColor.stripColor(player.getDisplayName()));
			if (this.plugin.getConfig().getBoolean("sheep.keys.ip"))
				json.put("ip", player.getAddress().getHostName());
			json.put("x", player.getLocation().getX());
			json.put("y", player.getLocation().getY());
			json.put("z", player.getLocation().getZ());
			json.put("holding", player.getItemInHand().getTypeId());
			json.put("holding_dmg", player.getItemInHand().getDurability());
			json.put("health", player.getHealth());
			json.put("hunger", player.getFoodLevel());
			json.put("air", player.getRemainingAir());
			json.put("dead", player.isDead());
			json.put("level", player.getLevel());
			json.put("exp", player.getExp());
			json.put("total_exp", player.getTotalExperience());
			json.put("potion_fx", this.PotionEffects(player.getActivePotionEffects()));
			json.put("world", player.getWorld().getName());
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		if (inventory) {
			json.put("inventory", this.Inventory(player.getInventory()));
		}
		
    	return json;
    }
    
    public JSONArray Inventory(PlayerInventory inventory) {
    	JSONArray json = new JSONArray();
    	try {
	    	ItemStack[] list = inventory.getContents();
	    	
	    	for (int j = 0; j < list.length; j++) {
	    		if (list[j] != null) {
	    			JSONObject json_item = new JSONObject();
	    			if (list[j].getTypeId() > 0) {
	    				json_item.put("id", list[j].getTypeId());
	    				json_item.put("amount", list[j].getAmount());
	    				json_item.put("durability", list[j].getDurability());
	    				json_item.put("slot", j);
	    				json_item.put("enchantments", this.Enchantments(list[j].getEnchantments()));
	    				json.add(json_item);
					}
	    		}
	    	}
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
    	return json;
    }
    
    private JSONObject Enchantments(Map<Enchantment, Integer> enchantments) {
    	JSONObject json = new JSONObject();
    	try {
			Set<Enchantment> enchant_list = enchantments.keySet();
			for (Enchantment key : enchant_list) {
				json.put(key.getId(), enchantments.get(key));
			}
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
		return json ;
    }
    
    private JSONArray PotionEffects(Collection<PotionEffect> potion_effects) {
    	JSONArray json = new JSONArray();
    	try {
    		for (PotionEffect pe : potion_effects) {
    			JSONObject json_effect = new JSONObject();
    			json_effect.put("id", pe.getType().getId());
    			json_effect.put("duration", pe.getDuration());
    			json_effect.put("amplifier", pe.getAmplifier());
    			json.add(json_effect);
    		}
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
    	return json;
    }
    
    public JSONObject Entities(Collection<Entity> entities) {
    	JSONObject json = new JSONObject();
    	try {
	    	Map<Short, Integer> counts = new HashMap<Short, Integer>();
	    	for (EntityType et : EntityType.values())
	    		counts.put(et.getTypeId(), 0);
	   	
	    	for (Entity e : entities) {
	    		short key = e.getType().getTypeId();
    			counts.put(key, counts.get(key) + 1);
	    	}
	    	
	    	for (Map.Entry<Short, Integer> entry : counts.entrySet()) {
	    		EntityType et = EntityType.fromId(entry.getKey());
	    		if (et == null)
	    			continue;
    			json.put(et.name().toLowerCase(), entry.getValue());
	        }
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
    	return json;
    }
}