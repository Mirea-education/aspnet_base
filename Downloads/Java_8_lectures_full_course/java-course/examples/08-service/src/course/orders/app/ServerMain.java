package course.orders.app;

import course.orders.application.OrderService;
import course.orders.application.OrderStore;
import course.orders.http.ApiServer;
import course.orders.infrastructure.InMemoryOrderStore;
import course.orders.infrastructure.JdbcOrderStore;
import java.time.Clock;
import java.util.concurrent.CountDownLatch;

public final class ServerMain {
    public static void main(String[] args) throws Exception {
        int port = args.length == 0 ? 8080 : Integer.parseInt(args[0]);
        String url = System.getenv("COURSE_JDBC_URL");
        OrderStore store = url == null || url.isBlank() ? new InMemoryOrderStore(5)
                : new JdbcOrderStore(url, System.getenv("COURSE_JDBC_USER"), System.getenv("COURSE_JDBC_PASSWORD"));
        var server = new ApiServer(new OrderService(store, Clock.systemUTC()), port);
        Runtime.getRuntime().addShutdownHook(new Thread(server::close));
        server.start();
        System.out.println("STORE=" + (store instanceof JdbcOrderStore ? "postgresql" : "memory"));
        System.out.println("Open http://127.0.0.1:" + server.port() + "/health");
        System.out.println("Stop with Ctrl+C");
        new CountDownLatch(1).await();
    }
}
