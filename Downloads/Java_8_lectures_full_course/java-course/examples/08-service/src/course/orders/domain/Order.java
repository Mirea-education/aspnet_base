package course.orders.domain;

import java.time.Instant;
import java.util.Objects;

public record Order(String id, Command command, long totalCents, Instant createdAt) {
    public Order {
        Objects.requireNonNull(id);
        Objects.requireNonNull(command);
        Objects.requireNonNull(createdAt);
        if (!id.matches("[A-Za-z0-9_-]+") || totalCents < 0) throw new IllegalArgumentException("Invalid order");
    }
}
