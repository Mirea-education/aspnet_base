package course.orders.infrastructure;

import course.orders.application.OrderStore;
import course.orders.domain.Command;
import course.orders.domain.Order;
import course.orders.domain.OrderFailure;
import course.orders.domain.Receipt;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class InMemoryOrderStore implements OrderStore {
    private final Map<String, Integer> stock = new HashMap<>();
    private final Map<String, Order> byId = new HashMap<>();
    private final Map<String, Order> byKey = new HashMap<>();
    private long nextId = 1;

    public InMemoryOrderStore(int books) {
        if (books < 0) throw new IllegalArgumentException("Negative stock");
        stock.put("BOOK", books);
    }
    @Override public synchronized Receipt place(Command command, Instant createdAt) {
        Order existing = byKey.get(command.requestId());
        if (existing != null) {
            if (!existing.command().equals(command)) throw new OrderFailure(OrderFailure.Kind.KEY_CONFLICT);
            return new Receipt(existing, true);
        }
        Integer available = stock.get(command.sku());
        if (available == null) throw new OrderFailure(OrderFailure.Kind.NOT_FOUND);
        if (available < command.quantity()) throw new OrderFailure(OrderFailure.Kind.NO_STOCK);
        long total = Math.multiplyExact(149_990L, command.quantity());
        var order = new Order("O" + nextId++, command, total, createdAt);
        stock.put(command.sku(), available - command.quantity());
        byId.put(order.id(), order);
        byKey.put(command.requestId(), order);
        return new Receipt(order, false);
    }
    @Override public synchronized Optional<Order> find(String id) { return Optional.ofNullable(byId.get(id)); }
    public synchronized int available(String sku) { return stock.getOrDefault(sku, 0); }
}
