package com.redfish.router;

import java.util.HashMap;
import java.util.HashSet;

import com.sun.net.httpserver.HttpHandler;

import com.redfish.HttpMethod;

class RouteNode {
	private static final String PARAM_SEGMENT = ":"; // Constant for segment (i.e. /app/:name indicates a "name" param)
	
	private String paramName; // Name of route parameter
	private HttpHandler[] handlers; // Handlers affiliated with this endpoint (if one exists). One per method.
	private HashMap<String, RouteNode> children; // Nodes corresponding to any segments after this one.

	/**
	 * Helper function to add a child to the current route node. If segment is route-based (i.e. /:id) the
	 * child will be added as ":".
	 * 
	 * @param segment One chunk of a request URI (area between slashes).
	 * @param seenParams Names for any route parameter that have already been seen.
	 * @throws Exception If the name for a route parameter has been repeated. TODO(atul) Create a custom exception type.
	 * @return The new child node, or one which already exists.
	 */
	RouteNode route(String segment, HashSet<String> seenParams) throws Exception {
		// If this node has not been configured for children, need to create the map.
		if (this.children == null)
			this.children = new HashMap<String, RouteNode>();
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
		RouteNode n = new RouteNode(); // Create new child node
		RouteNode inserted = this.children.put(segment.toLowerCase(), n); // Assign new node to uri segment (lowercase for case-insensitivity).
		return inserted == null ? n : inserted; // Note: "put" returns an existing value or "null". Return either the existing value or the new node.
	}

	/**
	 * Helper function to get the child node corresponding to the next uri segment.
	 * 
	 * @param segment URI path segment to match
	 * @param routeParams List of seen routeParams -- will add to this if the child is a colon-style parameter.
	 * @return The next child node (may be null if no match is found).
	 */
	RouteNode match(String segment, HashMap<String, String> routeParams) {
		RouteNode n = this.children.get(segment.toLowerCase()); // Try to find exact match.
		if (n == null && this.paramName != null) { // If no match AND paramName was initialized
			routeParams.put(this.paramName, segment); // Save segment to route params.
			n = this.children.get(PARAM_SEGMENT); // And overwrite return value with special param child.
		}
		return n; // Return node
	}

	// Getter/setter methods for the node handler.
	HttpHandler getHandler(HttpMethod method) { return (this.handlers == null) ? null : this.handlers[method.ordinal()]; }
	void setHandler(HttpMethod method, HttpHandler handler) {
		if (this.handlers == null) // Lazy initialization
			this.handlers = new HttpHandler[HttpMethod.values().length];
		this.handlers[method.ordinal()] = handler;
	}
}
