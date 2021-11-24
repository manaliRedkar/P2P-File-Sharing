import java.io.IOException;
import java.io.FileInputStream;
import java.io.File;
import java.io.OutputStream;
import java.net.URI;
 
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.Headers;

@SuppressWarnings("restriction")
//getting all properties from HttpHandler and overiding its handle()
//Interface: method signature, constants
class HtmlHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange t) throws IOException {
        ///
    }
}
    