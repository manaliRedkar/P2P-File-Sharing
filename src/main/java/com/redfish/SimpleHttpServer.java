package com.redfish;

import java.io.IOException;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;
import com.redfish.router.Router;

public class SimpleHttpServer {
 
    private HttpServer httpServer;
 
    /**
     * Instantiates a new simple http server.
     *
     * @param port the port
     * @param handlerPackages List of package import lists from which to pull handlers
     */
    public SimpleHttpServer(int port, String[] handlerPackages) throws Exception {

        Router router = new Router(); // Initialize the router
        // For each set of handlers
        for (int i = 0; i < handlerPackages.length; ++i) {
            // Open the auto-import file for reading
            FileInputStream file = new FileInputStream("src/main/resources/autoimports/" + handlerPackages[i]);
            BufferedReader packageReader = new BufferedReader(new InputStreamReader(file));
            // Iterate over the auto-import file
            for (String className = packageReader.readLine(); className != null; className = packageReader.readLine())
                // Use the qualified classname to pull the class spec and register with the router.
                router.registerFromClassSpec(Class.forName(className));
            packageReader.close(); // Close file reader
        }

        try {
            //Create HttpServer which is listening on the given port 
            httpServer = HttpServer.create(new InetSocketAddress(port), 0);
            //Create a new context for the given context and handler
            httpServer.createContext("/", router);
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