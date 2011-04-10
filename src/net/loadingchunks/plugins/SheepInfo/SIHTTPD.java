package net.loadingchunks.plugins.SheepInfo;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executor;

import org.bukkit.World;
import org.json.simple.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.xml.internal.ws.transport.Headers;

import net.loadingchunks.plugins.SheepInfo.SheepInfo;

public class SIHTTPD {
    private final SheepInfo plugin;
    private final SIJSON infoget = new SIJSON(this.plugin);
    private HttpServer server;

    public SIHTTPD (SheepInfo plugin) {
        this.plugin = plugin;
    }
    
    public boolean Listen()
    {
		try {
			this.server = HttpServer.create(new InetSocketAddress(Integer.parseInt(this.plugin.siConfig.get("bind_port"))), 0);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
    	this.server.createContext("/info", new SInfoHandler(this));
    	this.server.createContext("/inventory", new SInventoryHandler(this));
    	this.server.createContext("/", new SIDefaultHandler());
    	this.server.setExecutor(null);
    	this.server.start();
    	return true;
    }
    
    public void Kill()
    {
    	this.server.stop(0);
    }
    
    static class SInfoHandler implements HttpHandler {
    	private final SIHTTPD httpd;
    	
    	public SInfoHandler(SIHTTPD instance)
    	{
    		this.httpd = instance;
    	}
    	
    	public void handle(HttpExchange t) throws IOException {
    		JSONObject response_object = new JSONObject();
    		JSONObject worlds_object = new JSONObject();
    		
    		for( World w : this.httpd.plugin.worlds)
    		{
    			worlds_object.clear();
    			worlds_object.put("core", this.httpd.infoget.Info(w));
    			worlds_object.put("players", this.httpd.infoget.Players(w, false));
    			response_object.put(w.getName(), worlds_object);
    		}
    		
    		String response = response_object.toJSONString();
    		
    		com.sun.net.httpserver.Headers h = t.getResponseHeaders();
    		h.add("Content-Type", "application/json");

    		t.sendResponseHeaders(200, response.length());
    		OutputStream os = t.getResponseBody();
    		os.write(response.getBytes());
    		os.close();
    	}
    }
    
    static class SInventoryHandler implements HttpHandler {
    	private final SIHTTPD httpd;
    	
    	public SInventoryHandler(SIHTTPD instance)
    	{
    		this.httpd = instance;
    	}
    	
    	public void handle(HttpExchange t) throws IOException {
    		JSONObject response_object = new JSONObject();
    		JSONObject worlds_object = new JSONObject();
    		
    		for ( World w : this.httpd.plugin.worlds )
    		{
    			response_object.put(w.getName(), this.httpd.infoget.Players(w, true));
    		}
    		
    		String response = response_object.toJSONString();

    		com.sun.net.httpserver.Headers h = t.getResponseHeaders();
    		h.add("Content-Type", "application/json");
    		
    		t.sendResponseHeaders(200, response.length());
    		OutputStream os = t.getResponseBody();
    		os.write(response.getBytes());
    		os.close();
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
