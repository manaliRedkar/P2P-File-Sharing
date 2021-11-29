import java.io.IOException;
import java.io.OutputStream;
import com.sun.net.httpserver.HttpExchange;

/**
 * Utils class for Http responses
 */
class ResponseUtils {
	/**
	 * Helper function for returning an error message. This is equivalent to raising an exception,
	 * but does not actually break execution.
	 * 
	 * @param exchange The HttpExchange for which to return an error.
	 * @param code The error code to return.
	 * @param message The error message to return.
	 * @throws IOException If error occurs while writing to output stream.
	 * 
	 */
	static void reject(HttpExchange exchange, int code, String message) throws IOException {
		byte [] bytes = message.getBytes();
		// Set response headers
		exchange.sendResponseHeaders(code, bytes.length);
		// Write response
		OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
	}
}