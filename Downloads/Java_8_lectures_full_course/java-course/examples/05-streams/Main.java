import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Main {
    record Sale(String segment, String sku, int quantity, long unitPrice, boolean paid) {
        long total() { return Math.multiplyExact(quantity, unitPrice); }
    }
    public static void main(String[] args) {
        List<Sale> sales = List.of(
                new Sale("B2B", "BOOK", 2, 150_000, true),
                new Sale("B2C", "HUB", 1, 250_000, true),
                new Sale("B2B", "BOOK", 1, 150_000, false),
                new Sale("B2C", "CABLE", 3, 25_000, true));
        long total = sales.stream().filter(Sale::paid).mapToLong(Sale::total).sum();
        var bySegment = sales.stream().filter(Sale::paid).collect(Collectors.groupingBy(
                Sale::segment, TreeMap::new, Collectors.summingLong(Sale::total)));
        System.out.println("PAID_CENTS=" + total);
        System.out.println("SEGMENTS=" + bySegment);
        System.out.println("SKUS=" + sales.stream().map(Sale::sku).distinct().sorted().toList());
        var calls = new AtomicInteger();
        // Счётчик нужен только для наблюдения учебного эксперимента.
        var pipeline = List.of(1, 2, 3).stream().mapToInt(n -> { calls.incrementAndGet(); return n * 2; });
        System.out.println("CALLS_BEFORE=" + calls.get());
        System.out.println("SUM=" + pipeline.sum());
        System.out.println("CALLS_AFTER=" + calls.get());
        try { pipeline.sum(); }
        catch (IllegalStateException e) { System.out.println("REUSE=REJECTED"); }
    }
}
