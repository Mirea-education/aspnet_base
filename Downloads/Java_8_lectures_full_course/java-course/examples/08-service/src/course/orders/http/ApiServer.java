package course.orders.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import course.orders.application.OrderService;
import course.orders.domain.Command;
import course.orders.domain.Order;
import course.orders.domain.OrderFailure;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class ApiServer implements AutoCloseable {
    private final HttpServer server;
    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private final OrderService service;
    private static final System.Logger LOG = System.getLogger(ApiServer.class.getName());

    public ApiServer(OrderService service, int port) throws IOException {
        this.service = service;
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        server.setExecutor(executor);
        server.createContext("/", this::handle);
    }
    public void start() { server.start(); }
    public int port() { return server.getAddress().getPort(); }

    private void handle(HttpExchange exchange) throws IOException {
        try (exchange) {
            try {
                String path = exchange.getRequestURI().getPath();
                String method = exchange.getRequestMethod();
                if (path.equals("/health")) {
                    if (!method.equals("GET")) { methodNotAllowed(exchange, "GET"); return; }
                    send(exchange, 200, "{\"status\":\"UP\"}");
                } else if (path.equals("/orders")) {
                    if (!method.equals("POST")) { methodNotAllowed(exchange, "POST"); return; }
                    String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
                    if (contentType == null || !contentType.split(";", 2)[0].trim()
                            .equalsIgnoreCase("application/x-www-form-urlencoded")) {
                        send(exchange, 415, error("content_type")); return;
                    }
                    byte[] body = exchange.getRequestBody().readNBytes(1025);
                    if (body.length > 1024) { send(exchange, 413, error("body_too_large")); return; }
                    Map<String, String> fields = parse(new String(body, StandardCharsets.UTF_8));
                    if (!fields.keySet().equals(Set.of("key", "sku", "quantity")))
                        throw new IllegalArgumentException("Wrong fields");
                    var command = new Command(fields.get("key"), fields.get("sku"),
                            Integer.parseInt(fields.get("quantity")));
                    var result = service.place(command);
                    exchange.getResponseHeaders().set("Location", "/orders/" + result.order().id());
                    send(exchange, result.replay() ? 200 : 201, json(result.order()));
                } else if (path.matches("/orders/O[0-9]+")) {
                    if (!method.equals("GET")) { methodNotAllowed(exchange, "GET"); return; }
                    send(exchange, 200, json(service.find(path.substring("/orders/".length()))));
                } else { send(exchange, 404, error("not_found")); }
            } catch (IllegalArgumentException e) {
                send(exchange, 400, error("invalid_request"));
            } catch (OrderFailure e) {
                send(exchange, e.kind() == OrderFailure.Kind.NOT_FOUND ? 404 : 409,
                        error(e.kind().name().toLowerCase(java.util.Locale.ROOT)));
            } catch (RuntimeException e) {
                LOG.log(System.Logger.Level.ERROR, "Request failed", e);
                send(exchange, 500, error("internal_error"));
            }
        }
    }
    private static Map<String, String> parse(String body) {
        Map<String, String> fields = new HashMap<>();
        for (String pair : body.split("&", -1)) {
            String[] parts = pair.split("=", 2);
            if (parts.length != 2) throw new IllegalArgumentException("Invalid form");
            String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
            String value = URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
            if (fields.putIfAbsent(key, value) != null) throw new IllegalArgumentException("Duplicate field");
        }
        return fields;
    }
    private static void methodNotAllowed(HttpExchange e, String allow) throws IOException {
        e.getResponseHeaders().set("Allow", allow); send(e, 405, error("method_not_allowed"));
    }
    private static String error(String code) { return "{\"error\":\"" + code + "\"}"; }
    private static String json(Order order) {
        // Все текстовые значения имеют закрытый формат. Это не универсальный JSON-сериализатор.
        return "{\"id\":\"" + order.id() + "\",\"sku\":\"" + order.command().sku()
                + "\",\"quantity\":" + order.command().quantity() + ",\"totalCents\":" + order.totalCents() + "}";
    }
    private static void send(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
    }
    @Override public void close() {
        server.stop(0);
        executor.shutdownNow();
        try { executor.awaitTermination(5, TimeUnit.SECONDS); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
