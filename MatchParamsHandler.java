import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

@SuppressWarnings({"restriction", "unchecked"})
public class MatchParamsHandler implements HttpHandler {
    private static final int HTTP_OK_STATUS = 200;

    public void handle(HttpExchange t) throws IOException {
        String response = "";
        // Retrieve the special attribute for route params.
        Map<String,String> map = (Map<String,String>) t.getAttribute(RoutingHandler.ROUTE_PARAMS);

        // Iterate over each key/value and append to response string.
        for (Map.Entry<String, String> entry : map.entrySet())
            response += entry.getKey() + ": " + entry.getValue() + "\n";

        // Log to console.
        System.out.println("Response:\n---------\n" + response);

        //Set the response header status and length
        t.sendResponseHeaders(HTTP_OK_STATUS, response.getBytes().length);
        //Write the response string
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}