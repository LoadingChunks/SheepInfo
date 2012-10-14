package net.loadingchunks.plugins.SheepInfo;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.json.simple.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import net.loadingchunks.plugins.SheepInfo.SheepInfo;

public class SIHTTPD {
    private final SheepInfo mPlugin;
    private final SIJSON mInfoget;
    private HttpServer mServer;

    public SIHTTPD (SheepInfo instance) {
        mPlugin = instance;
        mInfoget = new SIJSON(mPlugin);
    }
    
    public boolean listen() {
		try {
			mServer = HttpServer.create(new InetSocketAddress(mPlugin.getConfig().getInt("sheep.bind.port")), 0);
		}
		catch (NumberFormatException e) {
			e.printStackTrace();
			return false;
		}
		catch (IOException e) {
			e.printStackTrace();
			return false;
		}
    	mServer.createContext("/entities", new SIEntityHandler(this));
    	mServer.createContext("/inventories", new SIInventoriesHandler(this));
    	mServer.createContext("/inventory", new SIInventoryHandler(this));
    	mServer.createContext("/stats", new SIStatsHandler(this));
    	mServer.createContext("/player", new SIPlayerHandler(this));
    	mServer.createContext("/players", new SIPlayersHandler(this));
    	mServer.createContext("/worlds", new SIWorldsHandler(this));
    	mServer.createContext("/", new SIDefaultHandler());
    	mServer.setExecutor(null);
    	mServer.start();
    	return true;
    }
    
    public void kill() {
    	mServer.stop(0);
    }
    
    static void ServeJSON(HttpExchange t, String response) throws IOException {
		ServeResponse(t, response, "application/json");
    }
    
    static void ServeResponse(HttpExchange t, String response, String content_type) throws IOException {
		com.sun.net.httpserver.Headers h = t.getResponseHeaders();
		h.add("Content-Type", content_type);

		t.sendResponseHeaders(200, response.length());
		OutputStream os = t.getResponseBody();
		os.write(response.getBytes());
		os.close();
    }
    
    static class SIEntityHandler implements HttpHandler {
    	private final SIHTTPD mHttpd;
    	
    	public SIEntityHandler(SIHTTPD instance) {
    		mHttpd = instance;
    	}
    	
    	public void handle(HttpExchange t) throws IOException {
    		JSONObject response_object = new JSONObject();
    		
    		for (World w : mHttpd.mPlugin.getServer().getWorlds()) {
    			response_object.put(w.getName(), mHttpd.mInfoget.getEntities(w.getEntities()));
    		}

    		SIHTTPD.ServeJSON(t, response_object.toJSONString());
    	}
    }
    
    static class SIInventoriesHandler implements HttpHandler {
    	private final SIHTTPD mHttpd;
    	
    	public SIInventoriesHandler(SIHTTPD instance) {
    		mHttpd = instance;
    	}
    	
    	public void handle(HttpExchange t) throws IOException {
    		JSONObject response_object = new JSONObject();
    		
    		for (Player p : mHttpd.mPlugin.getServer().getOnlinePlayers()) {
    			response_object.put(p.getName(), mHttpd.mInfoget.getInventory(p.getInventory()));
    		}

    		SIHTTPD.ServeJSON(t, response_object.toJSONString());
    	}
    }
    
    static class SIInventoryHandler implements HttpHandler {
    	private final SIHTTPD mHttpd;
    	
    	public SIInventoryHandler(SIHTTPD instance) {
    		mHttpd = instance;
    	}
    	
    	public void handle(HttpExchange t) throws IOException {
    		final String PARAM_PLAYER = "player";
    		
    		JSONObject response_object = new JSONObject();
    		
    		Map<String, Object> params = parseQuery(t.getRequestURI().getQuery());
    		
    		if (params.containsKey(PARAM_PLAYER) && params.get(PARAM_PLAYER) != null) {
    			Player p = mHttpd.mPlugin.getServer().getPlayerExact((String)params.get(PARAM_PLAYER));
    			if (p != null) {
    				response_object.put("inventory", mHttpd.mInfoget.getInventory(p.getInventory()));
    			}
    			else {
        			response_object.put("error", "That player has never visited this server.");
    			}
    		}
    		else {
    			response_object.put("error", "Missing required parameter `" + PARAM_PLAYER + "`.");
    		}

    		SIHTTPD.ServeJSON(t, response_object.toJSONString());
    	}
    }
    
    static class SIStatsHandler implements HttpHandler {
    	private final SIHTTPD mHttpd;
    	
    	public SIStatsHandler(SIHTTPD instance) {
    		mHttpd = instance;
    	}
    	
    	public void handle(HttpExchange t) throws IOException {
    		JSONObject response_object = mHttpd.mInfoget.getStats();

    		SIHTTPD.ServeJSON(t, response_object.toJSONString());
    	}
    }
    
    static class SIPlayerHandler implements HttpHandler {
    	private final SIHTTPD mHttpd;
    	
    	public SIPlayerHandler(SIHTTPD instance) {
    		mHttpd = instance;
    	}
    	
    	public void handle(HttpExchange t) throws IOException {
    		final String PARAM_PLAYER = "id";
    		
    		JSONObject response_object = new JSONObject();
    		
    		Map<String, Object> params = parseQuery(t.getRequestURI().getQuery());
    		
    		if (params.containsKey(PARAM_PLAYER) && params.get(PARAM_PLAYER) != null) {
    			String player_name = (String)params.get(PARAM_PLAYER);
    			Player p = mHttpd.mPlugin.getServer().getPlayerExact(player_name);
    			boolean offline = false;
    			if (p == null) {
    				OfflinePlayer[] offlinePlayers = mHttpd.mPlugin.getServer().getOfflinePlayers();
    				for (OfflinePlayer op : offlinePlayers)
        				if (op.getName().equalsIgnoreCase(player_name))
    						offline = true;
    			}
    			if (p != null) {
    				JSONObject p_json = mHttpd.mInfoget.getPlayer(p, false); 
    				if (p_json != null)
    					response_object = p_json;
    				else
    					response_object.put("error", "Unspecified error.");
    			}
    			else {
    				if (offline)
    					response_object.put("error", "Player is offline.");
    				else
    					response_object.put("error", "Player does not exist.");
    			}
    		}
    		else {
    			response_object.put("error", "Missing required parameter `" + PARAM_PLAYER + "`.");
    		}

    		SIHTTPD.ServeJSON(t, response_object.toJSONString());
    	}
    }
    
    static class SIPlayersHandler implements HttpHandler {
    	private final SIHTTPD mHttpd;
    	
    	public SIPlayersHandler(SIHTTPD instance) {
    		mHttpd = instance;
    	}
    	
    	public void handle(HttpExchange t) throws IOException {
    		JSONObject response_object = new JSONObject();
    		
    		response_object.put("players", mHttpd.mInfoget.getPlayers(false));

    		SIHTTPD.ServeJSON(t, response_object.toJSONString());
    	}
    }

	static class SIWorldsHandler implements HttpHandler {
		private final SIHTTPD mHttpd;
		
		public SIWorldsHandler(SIHTTPD instance) {
			mHttpd = instance;
		}
		
		public void handle(HttpExchange t) throws IOException {
			JSONObject response_object = new JSONObject();
			
			response_object.put("worlds", mHttpd.mInfoget.getWorlds(mHttpd.mPlugin.getServer().getWorlds()));
	
			SIHTTPD.ServeJSON(t, response_object.toJSONString());
		}
	}
    
    static class SIDefaultHandler implements HttpHandler {
    	public void handle(HttpExchange t) throws IOException {
    		String response = "";
    		response += "SheepInfo API:\n\n";
    		response += "/entities\n";
			response += "    provides a count of each type of entity\n\n";
    		response += "/inventories\n";
			response += "    returns an associative array of inventories of online players\n\n";
    		response += "/inventory?player=<player username>\n";
			response += "    returns the given player's inventory\n\n";
    		response += "/stats\n";
			response += "    returns stats of the server\n\n";
    		response += "/player?id=<player username>\n";
			response += "    returns details of the given player\n\n";
    		response += "/players\n";
			response += "    returns an array of online players\n\n";
    		response += "/worlds\n";
			response += "    returns an array of worlds\n\n";

			ServeResponse(t, response, "text/plain");
    	}
    }
    
    // adapted from http://leonardom.wordpress.com/2009/08/06/getting-parameters-from-httpexchange/
    private static Map<String, Object> parseQuery(String query) throws UnsupportedEncodingException {
    	Map<String, Object> parameters = new HashMap<String, Object>();
    	if (query != null) {
    		String pairs[] = query.split("[&]");

			for (String pair : pairs) {
				String param[] = pair.split("[=]");
	
				String key = null;
				String value = null;
				if (param.length > 0) {
					key = URLDecoder.decode(param[0],
					System.getProperty("file.encoding"));
				}
		
				if (param.length > 1) {
					value = URLDecoder.decode(param[1], System.getProperty("file.encoding"));
				}
		
		        if (parameters.containsKey(key)) {
		        	Object obj = parameters.get(key);
		        	if(obj instanceof List<?>) {
		        		List<String> values = (List<String>)obj;
		                values.add(value);
		        	}
		        	else if(obj instanceof String) {
		    			List<String> values = new ArrayList<String>();
		    			values.add((String)obj);
		    			values.add(value);
		    			parameters.put(key, values);
		            }
		        }
		        else {
		        	parameters.put(key, value);
		        }
	        }
    	}
    	return parameters;
    }
}
