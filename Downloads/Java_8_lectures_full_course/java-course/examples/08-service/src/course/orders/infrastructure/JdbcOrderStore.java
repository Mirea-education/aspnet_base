package course.orders.infrastructure;

import course.orders.application.OrderStore;
import course.orders.domain.Command;
import course.orders.domain.Order;
import course.orders.domain.OrderFailure;
import course.orders.domain.Receipt;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.Optional;

// Дополнительный адаптер PostgreSQL. Требует отдельной БД, схемы и JDBC-драйвера.
public final class JdbcOrderStore implements OrderStore {
    private final String url;
    private final String user;
    private final String password;
    public JdbcOrderStore(String url, String user, String password) {
        this.url = Objects.requireNonNull(url);
        this.user = Objects.requireNonNull(user);
        this.password = Objects.requireNonNull(password);
    }
    private Connection open() throws SQLException { return DriverManager.getConnection(url, user, password); }
    private static Order readOrder(ResultSet rows) throws SQLException {
        return new Order("O" + rows.getLong("id"),
                new Command(rows.getString("request_key"), rows.getString("sku"), rows.getInt("quantity")),
                rows.getLong("total_cents"), rows.getObject("created_at", OffsetDateTime.class).toInstant());
    }
    private static Optional<Order> byKey(Connection connection, String key) throws SQLException {
        try (var statement = connection.prepareStatement("SELECT * FROM course8_orders WHERE request_key = ?")) {
            statement.setString(1, key);
            try (var rows = statement.executeQuery()) { return rows.next() ? Optional.of(readOrder(rows)) : Optional.empty(); }
        }
    }
    private static Receipt replay(Order existing, Command command) {
        if (!existing.command().equals(command)) throw new OrderFailure(OrderFailure.Kind.KEY_CONFLICT);
        return new Receipt(existing, true);
    }
    @Override public Receipt place(Command command, Instant createdAt) {
        try (Connection connection = open()) {
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            connection.setAutoCommit(false);
            try {
                var existing = byKey(connection, command.requestId());
                if (existing.isPresent()) {
                    Receipt result = replay(existing.get(), command);
                    connection.commit(); return result;
                }
                long price;
                try (var query = connection.prepareStatement("SELECT price_cents FROM course8_stock WHERE sku = ?")) {
                    query.setString(1, command.sku());
                    try (var rows = query.executeQuery()) {
                        if (!rows.next()) throw new OrderFailure(OrderFailure.Kind.NOT_FOUND);
                        price = rows.getLong(1);
                    }
                }
                long total = Math.multiplyExact(price, command.quantity());
                Order inserted = null;
                try (var insert = connection.prepareStatement("INSERT INTO course8_orders " +
                        "(request_key, sku, quantity, total_cents, created_at) VALUES (?, ?, ?, ?, ?) " +
                        "ON CONFLICT (request_key) DO NOTHING RETURNING *")) {
                    insert.setString(1, command.requestId()); insert.setString(2, command.sku());
                    insert.setInt(3, command.quantity()); insert.setLong(4, total);
                    insert.setObject(5, createdAt.atOffset(ZoneOffset.UTC));
                    try (var rows = insert.executeQuery()) { if (rows.next()) inserted = readOrder(rows); }
                }
                if (inserted == null) {
                    // При READ COMMITTED новый SELECT видит победившую завершённую транзакцию.
                    Receipt result = replay(byKey(connection, command.requestId()).orElseThrow(), command);
                    connection.commit(); return result;
                }
                try (var update = connection.prepareStatement(
                        "UPDATE course8_stock SET available = available - ? WHERE sku = ? AND available >= ?")) {
                    update.setInt(1, command.quantity()); update.setString(2, command.sku());
                    update.setInt(3, command.quantity());
                    if (update.executeUpdate() != 1) throw new OrderFailure(OrderFailure.Kind.NO_STOCK);
                }
                connection.commit();
                return new Receipt(inserted, false);
            } catch (SQLException | RuntimeException e) {
                try { connection.rollback(); } catch (SQLException rollbackError) { e.addSuppressed(rollbackError); }
                throw e;
            }
        } catch (SQLException e) { throw new IllegalStateException("Storage operation failed", e); }
    }
    @Override public Optional<Order> find(String id) {
        if (id == null || !id.matches("O[0-9]+")) return Optional.empty();
        long numeric;
        try { numeric = Long.parseLong(id.substring(1)); }
        catch (NumberFormatException e) { return Optional.empty(); }
        try (Connection connection = open();
             var query = connection.prepareStatement("SELECT * FROM course8_orders WHERE id = ?")) {
            query.setLong(1, numeric);
            try (var rows = query.executeQuery()) { return rows.next() ? Optional.of(readOrder(rows)) : Optional.empty(); }
        } catch (SQLException e) { throw new IllegalStateException("Storage operation failed", e); }
    }
}
