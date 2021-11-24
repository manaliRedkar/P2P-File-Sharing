/**
 * @author ashraf
 * 
 */
public class HttpServerTest {
 
    private static final int PORT = 8000;
 
    public static void main(String[] args) throws Exception {
 
        // Create a new SimpleHttpServer
        SimpleHttpServer simpleHttpServer = new SimpleHttpServer(PORT);
 
        // Start the server
        simpleHttpServer.start();
        System.out.println("Server is started and listening on port "+ PORT);
    }
 
}