import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class webserver {
    public static void main(String[] args) throws IOException {
        // Create an HttpServer instance on port 5256
        HttpServer server = HttpServer.create(new java.net.InetSocketAddress(5256), 0);
        server.createContext("/", new FileHandler());
        server.setExecutor(null); // creates a default executor
        System.out.println("Starting server on port 5256...");
        server.start();
    }

    static class FileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String requestedFile = exchange.getRequestURI().getPath();
            if (requestedFile.equals("/")) {
                requestedFile = "/index.html"; // Default file
            }

            // Define the path to the public directory located one level up
            Path filePath = Paths.get("..", "public", requestedFile);
            File file = filePath.toFile();

            if (file.exists() && !file.isDirectory()) {
                // Set the response headers
                exchange.getResponseHeaders().add("Content-Type", Files.probeContentType(filePath));
                exchange.sendResponseHeaders(200, file.length());
                try (FileInputStream fis = new FileInputStream(file);
                     OutputStream os = exchange.getResponseBody()) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                }
            } else {
// 404
                String response = "404 (Not Found)";
                exchange.sendResponseHeaders(404, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            }
        }
    }
}