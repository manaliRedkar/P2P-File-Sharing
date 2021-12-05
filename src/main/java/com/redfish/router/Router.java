package com.redfish.router;

import java.io.IOException;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import com.redfish.HttpMethod;
import com.redfish.ResponseUtils;
import com.redfish.router.annotations.Route;

/**
 * Handler to allow special route-param style matching (i.e. /user/:userid).
 * This is not possible with the out-of-box functionality because it requires
 * inexact matching (i.e. /user/mredkar != /user/:userid).
 */
public class Router implements HttpHandler {
	private static final String PATH_DELIMITER = "/"; // Splitter for URI segments
	public static final String ROUTE_PARAMS = "routeParams"; // Special constant to save colon-segment parameters.
	private RouteNode root = new RouteNode(); // Root node for matcher graph.

	@Override
    public void handle(HttpExchange exchange) throws IOException {
		RouteNode n = root; // Will be used to find exact match for node.
		HashMap<String, String> routeParams = new HashMap<String, String>(); // Will contain all route parameters.

		StringTokenizer tokenizer = new StringTokenizer(exchange.getRequestURI().getPath(), PATH_DELIMITER); // Split path by '/' character.
		while (n != null && tokenizer.hasMoreElements()) // Iterate over all segments.
			n = n.match(tokenizer.nextToken(), routeParams); // Go to the correct child node.

		// Get node handler if node exists.
		HttpHandler handler = n != null ? n.getHandler(HttpMethod.valueOf(exchange.getRequestMethod())) : null;
		
		if (handler == null) // Return error code if no handler (either route wasn't matched or matched to a nontemrinal path w/o a handler).
			ResponseUtils.reject(exchange, 404, "No matching route");
		else {
			if (!routeParams.isEmpty()) // Add routeParams attribute if any matches were found.
				exchange.setAttribute(ROUTE_PARAMS, routeParams);
			handler.handle(exchange); // Invoke corresponding handler.
		}
	}

	/**
	 * Register an HttpHandler (equivalent to createContext).
	 * 
	 * @param path Path for which to add context (may include colon-style route parameters).
	 * @param handler Handler for the corresponding path.
	 * @throws Exception If invalid registration (this method should be called at startup).
	 */
	public void register(String path, HttpMethod method, HttpHandler handler) throws Exception {
		RouteNode n = root; // Tracks current ndoe
		HashSet<String> seenParams = new HashSet<String>(); // Tracks list of seen route parameter names.
		
		StringTokenizer tokenizer = new StringTokenizer(path, PATH_DELIMITER); // Split URI into segments using slashes.
		while (tokenizer.hasMoreElements()) // Iterate over tokenizer.
			n = n.route(tokenizer.nextToken(), seenParams); // Create the appropriate child route.
		if (n.getHandler(method) != null) // If the terminal node already has a handler registered then raise an exception.
			throw new Exception("Conflicting registration");
		n.setHandler(method, handler); // Otherwise set the handler for the terminal node.
	}

	public void registerFromClassSpec(Class<?> cls) throws Exception {
		// Make sure class type is HttpHandler
		if (!HttpHandler.class.isAssignableFrom(cls))
			throw new IllegalArgumentException(String.format("Class %s does not implement HttpHandler", cls.getName()));

		// Pull all @Route annotations from class spec
		Route[] routes = (Route[]) cls.getAnnotationsByType(Route.class);
		if (routes == null || routes.length == 0) // Skip handlers without annotations
			System.out.println("Skipping handler (no routes detected): " + cls.getName());
		else {
			// Create an instance of the handler
			HttpHandler instance = (HttpHandler) cls.getDeclaredConstructor().newInstance();
			// Register each route with the current router
			for (Route route : routes)
				this.register(route.path(), route.method(), instance);
		}
	}
}