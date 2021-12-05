package com.redfish;

import java.io.IOException;
import java.net.InetSocketAddress;
 
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import com.redfish.handlers.*;
 
@SuppressWarnings("restriction")
public class SimpleHttpServer {
 
    private HttpServer httpServer;
 
    /**
     * Instantiates a new simple http server.
     *
     * @param port the port
     * @param context the context
     * @param handler the handler
     */
    public SimpleHttpServer(int port) throws Exception {
        try {
            //Create HttpServer which is listening on the given port 
            httpServer = HttpServer.create(new InetSocketAddress(port), 0);
            //Create a new context for the given context and handler
            RoutingHandler h = new RoutingHandler();
            httpServer.createContext("/", h);
            
            h.register("/app", HttpMethod.GET, new HtmlHandler());
            h.register("/test", HttpMethod.GET, new HttpRequestHandler());
            h.register("/submit_file", HttpMethod.POST, new FileSubmissionHandler());
            h.register("/first/:first/last/:last", HttpMethod.GET, new MatchParamsHandler());
            //Create a default executor
            httpServer.setExecutor(null);
        } catch (IOException e) {
            e.printStackTrace();
        }
 
    }
 
    /**
     * Start.
     */
    public void start() {
        this.httpServer.start();
    }
 
}