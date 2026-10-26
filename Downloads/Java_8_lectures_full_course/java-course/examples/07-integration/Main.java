import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws Exception {
        var executor = Executors.newFixedThreadPool(2);
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.setExecutor(executor);
        server.createContext("/quote", exchange -> {
            try (exchange) {
                int status;
                String body;
                if (!exchange.getRequestURI().getPath().equals("/quote")) {
                    status = 404; body = "{\"error\":\"not_found\"}";
                } else if (!exchange.getRequestMethod().equals("GET")) {
                    status = 405; body = "{\"error\":\"method_not_allowed\"}";
                    exchange.getResponseHeaders().set("Allow", "GET");
                } else { status = 200; body = "{\"totalCents\":120000}"; }
                byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
                exchange.sendResponseHeaders(status, bytes.length);
                exchange.getResponseBody().write(bytes);
            }
        });
        server.start();
        try {
            URI uri = URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/quote");
            var client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
            var request = HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(3)).GET().build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            System.out.println("HTTP=" + response.statusCode());
            System.out.println("BODY=" + response.body());
            var post = HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(3))
                    .POST(HttpRequest.BodyPublishers.noBody()).build();
            System.out.println("POST=" + client.send(post, HttpResponse.BodyHandlers.discarding()).statusCode());
        } finally {
            server.stop(0);
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }
}
