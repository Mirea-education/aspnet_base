package course.orders.app;

import course.orders.application.OrderService;
import course.orders.domain.Command;
import course.orders.infrastructure.InMemoryOrderStore;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

public final class DemoMain {
    public static void main(String[] args) {
        var store = new InMemoryOrderStore(3);
        var service = new OrderService(store, Clock.fixed(Instant.parse("2026-01-15T09:00:00Z"), ZoneOffset.UTC));
        var command = new Command("demo-1", "BOOK", 2);
        var first = service.place(command);
        var again = service.place(command);
        System.out.println("ORDER=" + first.order().id());
        System.out.println("TOTAL_CENTS=" + first.order().totalCents());
        System.out.println("REPLAY=" + again.replay());
        System.out.println("STOCK=" + store.available("BOOK"));
    }
}
