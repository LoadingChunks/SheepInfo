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

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.json.simple.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import net.loadingchunks.plugins.SheepInfo.SheepInfo;

public class SIHTTPD {
    private final SheepInfo plugin;
    private final SIJSON infoget;
    private HttpServer server;

    public SIHTTPD (SheepInfo instance) {
        this.plugin = instance;
        this.infoget = new SIJSON(this.plugin);
    }
    
    public boolean listen() {
		try {
			this.server = HttpServer.create(new InetSocketAddress(this.plugin.getConfig().getInt("sheep.bind.port")), 0);
		}
		catch (NumberFormatException e) {
			e.printStackTrace();
			return false;
		}
		catch (IOException e) {
			e.printStackTrace();
			return false;
		}
    	this.server.createContext("/entities", new SIEntityHandler(this));
    	this.server.createContext("/inventories", new SIInventoriesHandler(this));
    	this.server.createContext("/inventory", new SIInventoryHandler(this));
    	this.server.createContext("/memory", new SIMemoryHandler(this));
    	this.server.createContext("/players", new SIPlayersHandler(this));
    	this.server.createContext("/worlds", new SIWorldsHandler(this));
    	this.server.createContext("/", new SIDefaultHandler());
    	this.server.setExecutor(null);
    	this.server.start();
    	return true;
    }
    
    public void kill() {
    	this.server.stop(0);
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
    	private final SIHTTPD httpd;
    	
    	public SIEntityHandler(SIHTTPD instance) {
    		this.httpd = instance;
    	}
    	
    	public void handle(HttpExchange t) throws IOException {
    		JSONObject response_object = new JSONObject();
    		
    		for (World w : this.httpd.plugin.getWorlds()) {
    			response_object.put(w.getName(), this.httpd.infoget.Entities(w.getEntities()));
    		}

    		SIHTTPD.ServeJSON(t, response_object.toJSONString());
    	}
    }
    
    static class SIInventoriesHandler implements HttpHandler {
    	private final SIHTTPD httpd;
    	
    	public SIInventoriesHandler(SIHTTPD instance) {
    		this.httpd = instance;
    	}
    	
    	public void handle(HttpExchange t) throws IOException {
    		JSONObject response_object = new JSONObject();
    		
    		for (Player p : this.httpd.plugin.getOnlinePlayers()) {
    			response_object.put(p.getName(), this.httpd.infoget.Inventory(p.getInventory()));
    		}

    		SIHTTPD.ServeJSON(t, response_object.toJSONString());
    	}
    }
    
    static class SIInventoryHandler implements HttpHandler {
    	private final SIHTTPD httpd;
    	
    	public SIInventoryHandler(SIHTTPD instance) {
    		this.httpd = instance;
    	}
    	
    	public void handle(HttpExchange t) throws IOException {
    		final String PARAM_PLAYER = "player";
    		
    		JSONObject response_object = new JSONObject();
    		
    		Map<String, Object> params = parseQuery(t.getRequestURI().getQuery());
    		
    		if (params.containsKey(PARAM_PLAYER) && params.get(PARAM_PLAYER) != null) {
    			Player p = this.httpd.plugin.getServer().getPlayerExact((String)params.get(PARAM_PLAYER));
    			if (p != null) {
    				response_object.put("inventory", this.httpd.infoget.Inventory(p.getInventory()));
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
    
    static class SIMemoryHandler implements HttpHandler {
    	private final SIHTTPD httpd;
    	
    	public SIMemoryHandler(SIHTTPD instance) {
    		this.httpd = instance;
    	}
    	
    	public void handle(HttpExchange t) throws IOException {
    		JSONObject response_object = this.httpd.infoget.Memory();

    		SIHTTPD.ServeJSON(t, response_object.toJSONString());
    	}
    }
    
    static class SIPlayersHandler implements HttpHandler {
    	private final SIHTTPD httpd;
    	
    	public SIPlayersHandler(SIHTTPD instance) {
    		this.httpd = instance;
    	}
    	
    	public void handle(HttpExchange t) throws IOException {
    		JSONObject response_object = new JSONObject();
    		
    		response_object.put("players", this.httpd.infoget.Players(false));

    		SIHTTPD.ServeJSON(t, response_object.toJSONString());
    	}
    }

	static class SIWorldsHandler implements HttpHandler {
		private final SIHTTPD httpd;
		
		public SIWorldsHandler(SIHTTPD instance) {
			this.httpd = instance;
		}
		
		public void handle(HttpExchange t) throws IOException {
			JSONObject response_object = new JSONObject();
			
			response_object.put("worlds", this.httpd.infoget.Worlds(this.httpd.plugin.getServer().getWorlds()));
	
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
    		response += "/memory\n";
			response += "    returns free and total memory of the server\n\n";
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
