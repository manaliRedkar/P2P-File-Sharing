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

        //read more about http headers: kind of to set rules about how the response should be handled
        Headers h = t.getResponseHeaders();

        h.add("Content-Type", "text/html");
        //HttpExchange t: same varible 
        try {
            //opening the existing file
            File newFile = new File("index.html");
            //starting sending message now a ok message and hwo long the response is
            t.sendResponseHeaders(200, newFile.length());

            FileInputStream fis = new FileInputStream(newFile);
            byte [] bytes = new byte[1024];
            
            OutputStream os = t.getResponseBody();
            int numBytes;
            while((numBytes = fis.read(bytes)) != -1) {
                os.write(bytes, 0, numBytes);
            }

            fis.close();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
    