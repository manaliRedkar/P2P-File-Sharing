package com.redfish.router.annotations;

import java.lang.annotation.Retention;  
import java.lang.annotation.RetentionPolicy;  

/**
 * Util class -- allows Route annotation to be used multiple times. Should not be used directly.
 */
@Retention(RetentionPolicy.RUNTIME)  
public @interface Routes {
	Route[] value();
}