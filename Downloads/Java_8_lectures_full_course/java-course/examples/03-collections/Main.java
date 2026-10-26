import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Main {
    record Product(String sku, String name) {}

    static <T> T first(List<? extends T> items) {
        if (items.isEmpty()) throw new IllegalArgumentException("Empty list");
        return items.get(0);
    }
    static void addQuantities(List<? super Integer> target) { target.add(2); target.add(3); }
    static Optional<Product> find(Map<String, Product> catalog, String sku) {
        return Optional.ofNullable(catalog.get(sku));
    }

    public static void main(String[] args) {
        var products = List.of(new Product("BOOK", "Java"), new Product("HUB", "USB"));
        Map<String, Product> catalog = new LinkedHashMap<>();
        for (Product product : products) {
            if (catalog.putIfAbsent(product.sku(), product) != null)
                throw new IllegalArgumentException("Duplicate sku");
        }
        System.out.println("CATALOG=" + catalog.keySet());
        System.out.println("FOUND=" + find(catalog, "BOOK").orElseThrow().name());
        System.out.println("MISSING=" + find(catalog, "OTHER").isEmpty());
        var ids = new ArrayList<>(List.of(10, 20, 30));
        ids.remove(1);
        ids.remove(Integer.valueOf(30));
        System.out.println("IDS=" + ids);
        System.out.println("UNIQUE=" + new HashSet<>(List.of("A", "A", "B")).size());
        List<Number> quantities = new ArrayList<>();
        addQuantities(quantities);
        System.out.println("FIRST=" + first(quantities));
        try { products.add(new Product("CABLE", "Cable")); }
        catch (UnsupportedOperationException e) { System.out.println("IMMUTABLE_LIST=REJECTED"); }
    }
}
