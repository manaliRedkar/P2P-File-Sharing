package com.redfish.router.annotations;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;  
import java.lang.annotation.RetentionPolicy;  

import com.redfish.HttpMethod;

/**
 * Annotation to mark an HttpHandler for auto-registration.
 * Can be applied to the same class multiple times.
 * Classes using this annotation must have a no-arg constructor.
 * 
 * @param method The HttpMethod for the route.
 * @param path The path (including route params) for the route.
 */
@Retention(RetentionPolicy.RUNTIME) // Persists annotation to runtime (otherwise it disappears).
@Repeatable(Routes.class) // Allows the annotation to be applied multiple times.
public @interface Route {
	HttpMethod method();
	String path();	 
}

