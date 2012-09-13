package net.loadingchunks.plugins.SheepInfo;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
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
        infoget = new SIJSON(instance);
    }
    
    public boolean Listen() {
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
    	this.server.createContext("/info", new SIInfoHandler(this));
    	this.server.createContext("/memory", new SIMemoryHandler(this));
    	this.server.createContext("/inventory", new SInventoryHandler(this));
    	this.server.createContext("/entities", new SIEntityHandler(this));
    	this.server.createContext("/players", new SIPlayerHandler(this));
    	this.server.createContext("/", new SIDefaultHandler());
    	this.server.setExecutor(null);
    	this.server.start();
    	return true;
    }
    
    public void Kill() {
    	this.server.stop(0);
    }
    
    static void GenericJSONHandle(HttpExchange t, String response) throws IOException {
		com.sun.net.httpserver.Headers h = t.getResponseHeaders();
		h.add("Content-Type", "application/json");

		t.sendResponseHeaders(200, response.length());
		OutputStream os = t.getResponseBody();
		os.write(response.getBytes());
		os.close();
    }
    
    static class SIInfoHandler implements HttpHandler {
    	private final SIHTTPD httpd;
    	
    	public SIInfoHandler(SIHTTPD instance) {
    		this.httpd = instance;
    	}
    	
    	public void handle(HttpExchange t) throws IOException {
    		// I feel dirty doing this...
    		JSONArray response_object = new JSONArray();
    		
    		for (World w : this.httpd.plugin.worlds) {
    			JSONObject world_object = new JSONObject();
    			world_object.put("core", this.httpd.infoget.Info(w, this.httpd.plugin));
    			world_object.put("players", this.httpd.infoget.Players(w, false, this.httpd.plugin));
    			response_object.add(world_object);
    		}

    		SIHTTPD.GenericJSONHandle(t, response_object.toJSONString());
    	}
    }
    
    static class SIMemoryHandler implements HttpHandler {
    	private final SIHTTPD httpd;
    	
    	public SIMemoryHandler(SIHTTPD instance) {
    		this.httpd = instance;
    	}
    	
    	public void handle(HttpExchange t) throws IOException {
    		JSONObject response_object = this.httpd.infoget.Memory(this.httpd.plugin);

    		SIHTTPD.GenericJSONHandle(t, response_object.toJSONString());
    	}
    }
    
    static class SIPlayerHandler implements HttpHandler {
    	private final SIHTTPD httpd;
    	
    	public SIPlayerHandler(SIHTTPD instance) {
    		this.httpd = instance;
    	}
    	
    	public void handle(HttpExchange t) throws IOException {
    		JSONArray response_array = new JSONArray();
    		
    		for (World w : this.httpd.plugin.worlds) {
    			for (Player p : w.getPlayers()) {
    				try {
    					p.getAddress().getHostName();
    				}
    				catch (NullPointerException n) {
    					continue;
    				}
    				response_array.add(p.getName());
    			}
    		}
    		
    		SIHTTPD.GenericJSONHandle(t, response_array.toJSONString());
    	}
    }
    
    static class SIEntityHandler implements HttpHandler {
    	private final SIHTTPD httpd;
    	
    	public SIEntityHandler(SIHTTPD instance) {
    		this.httpd = instance;
    	}
    	
    	public void handle(HttpExchange t) throws IOException {
    		JSONObject response_object = new JSONObject();
    		
    		for (World w : this.httpd.plugin.worlds) {
    			response_object.put(w.getName(), this.httpd.infoget.Entities(w));
    		}

    		SIHTTPD.GenericJSONHandle(t, response_object.toJSONString());
    	}
    }
    
    static class SInventoryHandler implements HttpHandler {
    	private final SIHTTPD httpd;
    	
    	public SInventoryHandler(SIHTTPD instance) {
    		this.httpd = instance;
    	}
    	
    	public void handle(HttpExchange t) throws IOException {
    		JSONObject response_object = new JSONObject();
    		
    		for (World w : this.httpd.plugin.worlds) {
    			for (Player p : w.getPlayers()) {
    				response_object.put(p.getName(), this.httpd.infoget.Inventories(p));
    			}
    		}

    		SIHTTPD.GenericJSONHandle(t, response_object.toJSONString());
    	}
    }
    
    static class SIDefaultHandler implements HttpHandler {
    	public void handle(HttpExchange t) throws IOException {
    		String response = "Try /info to get MC Server Info.";
    		t.sendResponseHeaders(200, response.length());
    		OutputStream os = t.getResponseBody();
    		os.write(response.getBytes());
    		os.close();
    	}
    }
}
