package course.orders.domain;

public final class OrderFailure extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public enum Kind { NOT_FOUND, NO_STOCK, KEY_CONFLICT }
    private final Kind kind;
    public OrderFailure(Kind kind) { super(kind.name()); this.kind = kind; }
    public Kind kind() { return kind; }
}
