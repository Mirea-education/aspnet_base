import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.UUID;

public class JdbcDemo {
    static void reserve(Connection connection, UUID requestId, String sku, int quantity) throws SQLException {
        if (quantity < 1) throw new IllegalArgumentException("quantity must be positive");
        // Метод владеет транзакцией только на отдельном соединении с autoCommit=true.
        if (!connection.getAutoCommit()) throw new IllegalStateException("Already in transaction");
        connection.setAutoCommit(false);
        try {
            try (var update = connection.prepareStatement(
                    "UPDATE course_stock SET available = available - ? WHERE sku = ? AND available >= ?")) {
                update.setInt(1, quantity); update.setString(2, sku); update.setInt(3, quantity);
                if (update.executeUpdate() != 1) throw new SQLException("Stock unavailable");
            }
            try (var insert = connection.prepareStatement(
                    "INSERT INTO course_orders(request_id, sku, quantity) VALUES (?, ?, ?)")) {
                insert.setObject(1, requestId); insert.setString(2, sku); insert.setInt(3, quantity);
                insert.executeUpdate();
            }
            connection.commit();
        } catch (SQLException | RuntimeException e) {
            try { connection.rollback(); } catch (SQLException rollbackError) { e.addSuppressed(rollbackError); }
            throw e;
        }
        // Соединение закрывается вызывающим кодом. Не возвращаем его в пул.
    }
    public static void main(String[] args) throws Exception {
        String url = System.getenv("COURSE_JDBC_URL");
        if (url == null || url.isBlank()) {
            System.out.println("JDBC_SKIPPED=Set COURSE_JDBC_URL, COURSE_JDBC_USER, COURSE_JDBC_PASSWORD");
            return;
        }
        int quantity = args.length == 0 ? 1 : Integer.parseInt(args[0]);
        try (Connection connection = DriverManager.getConnection(url,
                System.getenv("COURSE_JDBC_USER"), System.getenv("COURSE_JDBC_PASSWORD"))) {
            reserve(connection, UUID.randomUUID(), "BOOK", quantity);
            System.out.println("TRANSACTION=COMMITTED");
        }
    }
}
