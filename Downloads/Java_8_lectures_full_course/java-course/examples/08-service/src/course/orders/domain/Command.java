package course.orders.domain;

public record Command(String requestId, String sku, int quantity) {
    public Command {
        if (requestId == null || !requestId.matches("[A-Za-z0-9_-]{1,64}"))
            throw new IllegalArgumentException("Invalid request id");
        if (sku == null || !sku.matches("[A-Z][A-Z0-9_-]{0,31}"))
            throw new IllegalArgumentException("Invalid sku");
        if (quantity < 1 || quantity > 5) throw new IllegalArgumentException("Quantity must be 1..5");
    }
}
