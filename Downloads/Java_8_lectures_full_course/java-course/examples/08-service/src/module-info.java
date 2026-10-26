module course.orders {
    requires jdk.httpserver;
    requires java.sql;
    exports course.orders.domain;
    exports course.orders.application;
    exports course.orders.infrastructure;
    exports course.orders.http;
}
