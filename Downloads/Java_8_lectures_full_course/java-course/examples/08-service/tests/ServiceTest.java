import course.orders.application.OrderService;
import course.orders.domain.Command;
import course.orders.domain.OrderFailure;
import course.orders.http.ApiServer;
import course.orders.infrastructure.InMemoryOrderStore;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ServiceTest {
    static int passed;
    static final Clock CLOCK = Clock.fixed(Instant.parse("2026-01-15T09:00:00Z"), ZoneOffset.UTC);
    static void check(boolean condition, String name) {
        if (!condition) throw new AssertionError(name);
        passed++; System.out.println("PASS " + name);
    }
    static void failure(Runnable action, OrderFailure.Kind kind, String name) {
        try { action.run(); throw new AssertionError(name + " did not throw"); }
        catch (OrderFailure e) { check(e.kind() == kind, name); }
    }
    static HttpResponse<String> request(HttpClient client, int port, String method, String path, String body) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path))
                .timeout(Duration.ofSeconds(3));
        if (method.equals("POST")) builder.header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body));
        else builder.method(method, HttpRequest.BodyPublishers.noBody());
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }
    public static void main(String[] args) throws Exception {
        var store = new InMemoryOrderStore(3);
        var service = new OrderService(store, CLOCK);
        var command = new Command("same", "BOOK", 2);
        var first = service.place(command);
        check(first.order().totalCents() == 299_980 && !first.replay(), "price and first result");
        check(first.order().createdAt().equals(CLOCK.instant()), "injected clock");
        var replay = service.place(command);
        check(replay.replay() && replay.order().id().equals(first.order().id()) && store.available("BOOK") == 1,
                "retry does not reserve twice");
        failure(() -> service.place(new Command("same", "BOOK", 1)), OrderFailure.Kind.KEY_CONFLICT,
                "same key with different payload");
        failure(() -> service.place(new Command("other", "BOOK", 2)), OrderFailure.Kind.NO_STOCK, "insufficient stock");
        check(store.available("BOOK") == 1, "failed order does not change stock");
        failure(() -> service.place(new Command("unknown", "MISSING", 1)), OrderFailure.Kind.NOT_FOUND, "unknown sku");
        try { new Command("bad", "BOOK", 0); throw new AssertionError("invalid command accepted"); }
        catch (IllegalArgumentException e) { check(true, "quantity validation"); }

        var concurrentStore = new InMemoryOrderStore(1);
        var concurrentService = new OrderService(concurrentStore, CLOCK);
        var pool = Executors.newFixedThreadPool(2);
        var start = new CountDownLatch(1);
        try {
            var a = pool.submit(() -> attempt(concurrentService, start, "a"));
            var b = pool.submit(() -> attempt(concurrentService, start, "b"));
            start.countDown();
            check(a.get(3, TimeUnit.SECONDS) + b.get(3, TimeUnit.SECONDS) == 1 && concurrentStore.available("BOOK") == 0,
                    "concurrent reservation of last item");
        } finally { pool.shutdownNow(); pool.awaitTermination(5, TimeUnit.SECONDS); }

        try (var api = new ApiServer(new OrderService(new InMemoryOrderStore(5), CLOCK), 0)) {
            api.start();
            var client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
            int port = api.port();
            check(request(client, port, "GET", "/health", "").statusCode() == 200, "health");
            var created = request(client, port, "POST", "/orders", "key=web&sku=BOOK&quantity=2");
            check(created.statusCode() == 201 && created.body().contains("\"totalCents\":299980"), "HTTP create");
            var retry = request(client, port, "POST", "/orders", "key=web&sku=BOOK&quantity=2");
            check(retry.statusCode() == 200 && retry.body().equals(created.body()), "HTTP retry");
            check(request(client, port, "GET", "/orders/O1", "").statusCode() == 200, "HTTP read");
            check(request(client, port, "POST", "/orders", "key=web&sku=BOOK&quantity=1").statusCode() == 409,
                    "HTTP conflicting key");
            check(request(client, port, "POST", "/orders", "key=x&sku=BOOK&quantity=0").statusCode() == 400,
                    "HTTP invalid quantity");
            check(request(client, port, "POST", "/orders", "key=x&sku=BOOK&quantity=1&quantity=2").statusCode() == 400,
                    "HTTP duplicate field");
            var wrongMethod = request(client, port, "GET", "/orders", "");
            check(wrongMethod.statusCode() == 405 && wrongMethod.headers().firstValue("Allow").orElse("").equals("POST"),
                    "HTTP method and Allow header");
            check(request(client, port, "GET", "/missing", "").statusCode() == 404, "HTTP missing path");
            check(request(client, port, "POST", "/orders", "x".repeat(1025)).statusCode() == 413, "HTTP body limit");
            var noType = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/orders"))
                    .timeout(Duration.ofSeconds(3)).POST(HttpRequest.BodyPublishers.ofString("a=b")).build();
            check(client.send(noType, HttpResponse.BodyHandlers.discarding()).statusCode() == 415, "HTTP content type");
        }
        System.out.println("TESTS_PASSED=" + passed);
    }
    static int attempt(OrderService service, CountDownLatch start, String key) throws Exception {
        if (!start.await(2, TimeUnit.SECONDS)) throw new IllegalStateException("No start signal");
        try { service.place(new Command(key, "BOOK", 1)); return 1; }
        catch (OrderFailure e) { if (e.kind() != OrderFailure.Kind.NO_STOCK) throw e; return 0; }
    }
}
