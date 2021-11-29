import java.io.IOException;
import java.lang.Exception;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * Handler to allow special route-param style matching (i.e. /user/:userid).
 * This is not possible with the out-of-box functionality because it requires
 * inexact matching (i.e. /user/mredkar != /user/:userid).
 */
@SuppressWarnings("restriction")
public class RoutingHandler implements HttpHandler {
	private static final String PATH_DELIMITER = "/"; // Splitter for URI segments
	public static final String ROUTE_PARAMS = "routeParams"; // Special constant to save colon-segment parameters.
	private Node root = new Node(); // Root node for matcher graph.

	@Override
    public void handle(HttpExchange exchange) throws IOException {
		Node n = root; // Will be used to find exact match for node.
		HashMap<String, String> routeParams = new HashMap<String, String>(); // Will contain all route parameters.

		StringTokenizer tokenizer = new StringTokenizer(exchange.getRequestURI().getPath(), PATH_DELIMITER); // Split path by '/' character.
		while (n != null && tokenizer.hasMoreElements()) // Iterate over all segments.
			n = n.match(tokenizer.nextToken(), routeParams); // Go to the correct child node.

		// Get node handler if node exists.
		HttpHandler handler = n != null ? n.getHandler() : null;
		
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
	public void register(String path, HttpHandler handler) throws Exception {
		Node n = root; // Tracks current ndoe
		HashSet<String> seenParams = new HashSet<String>(); // Tracks list of seen route parameter names.
		
		StringTokenizer tokenizer = new StringTokenizer(path, PATH_DELIMITER); // Split URI into segments using slashes.
		while (tokenizer.hasMoreElements()) // Iterate over tokenizer.
			n = n.route(tokenizer.nextToken(), seenParams); // Create the appropriate child route.
		if (n.getHandler() != null) // If the terminal node already has a handler registered then raise an exception.
			throw new Exception("Conflicting registration");
		n.setHandler(handler); // Otherwise set the handler for the terminal node.
	}

	private static class Node {
		private static final String PARAM_SEGMENT = ":"; // Constant for segment (i.e. /app/:name indicates a "name" param)
		
		private String paramName; // Name of route parameter
		private HttpHandler handler; // Handler affiliated with this endpoint (if one exists).
		private HashMap<String, Node> children; // Nodes corresponding to any segments after this one.

		/**
		 * Helper function to add a child to the current route node. If segment is route-based (i.e. /:id) the
		 * child will be added as ":".
		 * 
		 * @param segment One chunk of a request URI (area between slashes).
		 * @param seenParams Names for any route parameter that have already been seen.
		 * @throws Exception If the name for a route parameter has been repeated. TODO(atul) Create a custom exception type.
		 * @return The new child node, or one which already exists.
		 */
		Node route(String segment, HashSet<String> seenParams) throws Exception {
			// If this node has not been configured for children, need to create the map.
			if (this.children == null)
				this.children = new HashMap<String, Node>();
			// If the segment starts with ":", then need to apply special logic.
			if (segment.startsWith(PARAM_SEGMENT)) {
				String paramName = segment.substring(PARAM_SEGMENT.length()); // Pull the name of the route param.
				if (paramName.length() == 0) // Route param name must not be empty.
					throw new Exception("Route param must have name");
				if (this.paramName != null && !this.paramName.equals(paramName)) // Route param name must be the same for all routes.
					throw new Exception("Route params with identical prefixes must have the same name");
				if (!seenParams.add(paramName)) // If route param is already seen, through an exception (executed at startup).
					throw new Exception("Repeated match parameter");
				this.paramName = paramName; // Save parameter name.
				segment = PARAM_SEGMENT; // All parameter segments should be mapped to the same PARAM_SEGMENT child.
			}
			Node n = new Node(); // Create new child node
			Node inserted = this.children.put(segment.toLowerCase(), n); // Assign new node to uri segment (lowercase for case-insensitivity).
			return inserted == null ? n : inserted; // Note: "put" returns an existing value or "null". Return either the existing value or the new node.
		}

		/**
		 * Helper function to get the child node corresponding to the next uri segment.
		 * 
		 * @param segment URI path segment to match
		 * @param routeParams List of seen routeParams -- will add to this if the child is a colon-style parameter.
		 * @return The next child node (may be null if no match is found).
		 */
		Node match(String segment, HashMap<String, String> routeParams) {
			Node n = this.children.get(segment.toLowerCase()); // Try to find exact match.
			if (n == null && this.paramName != null) { // If no match AND paramName was initialized
				routeParams.put(this.paramName, segment); // Save segment to route params.
				n = this.children.get(PARAM_SEGMENT); // And overwrite return value with special param child.
			}
			return n; // Return node
		}

		// Getter/setter methods for the node handler.
		HttpHandler getHandler() { return this.handler; }
		void setHandler(HttpHandler handler) { this.handler = handler; }
	}
}