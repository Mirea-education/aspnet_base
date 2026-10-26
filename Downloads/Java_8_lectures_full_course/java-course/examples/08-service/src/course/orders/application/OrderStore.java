package course.orders.application;

import course.orders.domain.Command;
import course.orders.domain.Order;
import course.orders.domain.Receipt;
import java.time.Instant;
import java.util.Optional;

public interface OrderStore {
    // Проверка ключа, резервирование и сохранение образуют одну атомарную операцию.
    Receipt place(Command command, Instant createdAt);
    Optional<Order> find(String id);
}
