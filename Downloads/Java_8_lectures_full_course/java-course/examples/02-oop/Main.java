import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Main {
    enum Status { NEW, PAID, CANCELLED }

    record Line(String sku, long unitPrice, int quantity) {
        Line {
            Objects.requireNonNull(sku);
            if (sku.isBlank() || unitPrice < 0 || quantity < 1)
                throw new IllegalArgumentException("Invalid line");
        }
        long total() { return Math.multiplyExact(unitPrice, quantity); }
    }

    interface DiscountPolicy { long discount(long gross); }

    static final class ThresholdDiscount implements DiscountPolicy {
        @Override public long discount(long gross) {
            return gross >= 200_000 ? gross / 20 : 0;
        }
    }

    static final class Order {
        private final List<Line> lines;
        private Status status = Status.NEW;
        Order(List<Line> lines) {
            this.lines = List.copyOf(lines);
            if (this.lines.isEmpty()) throw new IllegalArgumentException("Empty order");
        }
        Status status() { return status; }
        long payable(DiscountPolicy policy) {
            long total = 0;
            for (Line line : lines) total = Math.addExact(total, line.total());
            long discount = policy.discount(total);
            if (discount < 0 || discount > total) throw new IllegalArgumentException("Bad policy");
            return total - discount;
        }
        void pay() {
            if (status != Status.NEW) throw new IllegalStateException("Only NEW can be paid");
            status = Status.PAID;
        }
        void cancel() {
            if (status != Status.NEW) throw new IllegalStateException("Only NEW can be cancelled");
            status = Status.CANCELLED;
        }
        int size() { return lines.size(); }
    }

    static void reassign(Order order) { order = new Order(List.of(new Line("OTHER", 1, 1))); }

    public static void main(String[] args) {
        final var original = new ArrayList<Line>();
        original.add(new Line("BOOK", 129_900, 2));
        var order = new Order(original);
        original.clear();
        System.out.println("LINES=" + order.size());
        System.out.println("PAY_CENTS=" + order.payable(new ThresholdDiscount()));
        reassign(order);
        System.out.println("BEFORE=" + order.status());
        order.pay();
        System.out.println("AFTER=" + order.status());
        try { order.pay(); }
        catch (IllegalStateException e) { System.out.println("SECOND_PAYMENT=REJECTED"); }
        final var tags = new ArrayList<String>();
        tags.add("paid");
        System.out.println("FINAL_LIST_SIZE=" + tags.size());
    }
}
