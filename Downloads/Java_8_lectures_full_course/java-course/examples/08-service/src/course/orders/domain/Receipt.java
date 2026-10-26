package course.orders.domain;

public record Receipt(Order order, boolean replay) {}
