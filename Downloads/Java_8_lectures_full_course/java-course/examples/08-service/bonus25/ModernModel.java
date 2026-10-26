import java.util.Objects;

public class ModernModel {
    sealed interface Action permits Reserve, Cancel {}
    record Reserve(String sku, int quantity) implements Action {}
    record Cancel(String id) implements Action {}

    static String describe(Action action) {
        Objects.requireNonNull(action);
        return switch (action) {
            case Reserve(var sku, var quantity) -> sku + " x" + quantity;
            case Cancel(var id) -> "cancel " + id;
        };
    }
    public static void main(String[] args) { System.out.println("ACTION=" + describe(new Reserve("BOOK", 2))); }
}
