package course.orders.application;

import course.orders.domain.Command;
import course.orders.domain.Order;
import course.orders.domain.Receipt;
import course.orders.domain.OrderFailure;
import java.time.Clock;
import java.util.Objects;

public final class OrderService {
    private final OrderStore store;
    private final Clock clock;
    public OrderService(OrderStore store, Clock clock) {
        this.store = Objects.requireNonNull(store);
        this.clock = Objects.requireNonNull(clock);
    }
    public Receipt place(Command command) { return store.place(Objects.requireNonNull(command), clock.instant()); }
    public Order find(String id) {
        return store.find(id).orElseThrow(() -> new OrderFailure(OrderFailure.Kind.NOT_FOUND));
    }
}
