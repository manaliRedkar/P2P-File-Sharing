package com.redfish.handlers;

import java.io.IOException;
 
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import com.redfish.HttpMethod;
import com.redfish.router.annotations.Route;

//getting all properties from HttpHandler and overiding its handle()
//Interface: method signature, constants

@Route(method=HttpMethod.POST, path="/submit_file")
public class FileSubmissionHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange t) throws IOException {
        ///
    }
}
    