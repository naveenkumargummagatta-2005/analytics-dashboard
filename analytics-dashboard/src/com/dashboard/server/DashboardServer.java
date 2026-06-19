package com.dashboard.server;

import com.dashboard.DataPipeline;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;

/**
 * Serves two things on one port:
 *   - GET /api/data         -> the generated dashboard-data.json
 *   - GET /api/refresh      -> re-runs the pipeline against the DB, then returns "ok"
 *   - GET /  and any other  -> static files from the web/ folder (the frontend)
 *
 * Using Java's built-in HttpServer avoids pulling in Spring or any other
 * framework, keeping the whole project to "plain Java + JDBC".
 */
public class DashboardServer {

    private static final int PORT = 8080;
    private static final Path WEB_ROOT = Paths.get("../web").toAbsolutePath().normalize();
    private static final Path JSON_FILE = Paths.get("output/dashboard-data.json").toAbsolutePath().normalize();

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/api/data", new ApiDataHandler());
        server.createContext("/api/refresh", new ApiRefreshHandler());
        server.createContext("/", new StaticFileHandler());

        server.setExecutor(null); // default executor is fine for this scale
        server.start();

        System.out.println("Dashboard server running at http://localhost:" + PORT);
        System.out.println("Serving static files from: " + WEB_ROOT);
        System.out.println("Serving JSON from:          " + JSON_FILE);
    }

    /** Returns the contents of dashboard-data.json with the right content type. */
    static class ApiDataHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!Files.exists(JSON_FILE)) {
                String msg = "{\"error\":\"dashboard-data.json not found. Run the pipeline first.\"}";
                sendResponse(exchange, 404, "application/json", msg.getBytes());
                return;
            }
            byte[] bytes = Files.readAllBytes(JSON_FILE);
            sendResponse(exchange, 200, "application/json", bytes);
        }
    }

    /** Re-runs the Java/JDBC pipeline on demand, so the frontend can trigger a refresh. */
    static class ApiRefreshHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                DataPipeline.run();
                sendResponse(exchange, 200, "application/json", "{\"status\":\"ok\"}".getBytes());
            } catch (SQLException e) {
                String msg = "{\"error\":\"" + e.getMessage().replace("\"", "'") + "\"}";
                sendResponse(exchange, 500, "application/json", msg.getBytes());
            }
        }
    }

    /** Serves index.html, css, and js files out of the web/ directory. */
    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String requestPath = exchange.getRequestURI().getPath();
            if (requestPath.equals("/")) {
                requestPath = "/index.html";
            }
            Path filePath = WEB_ROOT.resolve(requestPath.substring(1)).normalize();

            // Basic safety check: never serve files outside web/
            if (!filePath.startsWith(WEB_ROOT) || !Files.exists(filePath)) {
                sendResponse(exchange, 404, "text/plain", "404 Not Found".getBytes());
                return;
            }

            byte[] bytes = Files.readAllBytes(filePath);
            sendResponse(exchange, 200, contentTypeFor(filePath), bytes);
        }

        private String contentTypeFor(Path path) {
            String name = path.toString();
            if (name.endsWith(".html")) return "text/html";
            if (name.endsWith(".css")) return "text/css";
            if (name.endsWith(".js")) return "application/javascript";
            if (name.endsWith(".json")) return "application/json";
            return "application/octet-stream";
        }
    }

    private static void sendResponse(HttpExchange exchange, int statusCode, String contentType, byte[] body) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(statusCode, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }
    }
}
