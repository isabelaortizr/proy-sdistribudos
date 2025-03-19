/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package httpserver;


import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
/**
 *
 * @author rlaredo
 */
public class ApacheServer {
    private HttpServer server = null;
    private boolean isServerDone = false;

    public ApacheServer(){
    }
    
    public boolean start() {
        try {
            this.server = HttpServer.create(new InetSocketAddress(1914), 0);
            this.server.createContext("/", exchange -> {
                Headers headers = exchange.getResponseHeaders();
                headers.add("Access-Control-Allow-Origin", "*");
                headers.add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
                headers.add("Access-Control-Allow-Headers", "Content-Type, Authorization");
                new RootHandler().handle(exchange);
            });

            this.server.createContext("/api/v1/insertCandidato", new InsertCandidatoHandler());
            this.server.createContext("/api/v1/insertVotante", new InsertVotanteHandler());
            this.server.createContext("/api/v1/registerVote", new RegisterVoteHandler());
            this.server.createContext("/api/v1/getAllVotantes", new GetAllVotantesHandler());
            this.server.createContext("/api/v1/getAllCandidatos", new GetAllCandidatosHandler());
            this.server.createContext("/api/v1/getAllBloques", new GetAllBloquesHandler());
            this.server.setExecutor(Executors.newFixedThreadPool(2));
            this.server.start();
        
        return true;
        } catch (IOException e) {
            this.server = null;
            //System.exit(-1);
        }
        return false;
    }
    
    public void stop(){
        this.server.stop(0);
        this.server = null;
    }

  
    
}
