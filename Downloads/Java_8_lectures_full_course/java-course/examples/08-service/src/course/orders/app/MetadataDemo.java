package course.orders.app;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public final class MetadataDemo {
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Route { String value(); }

    public static final class Endpoint {
        @Route("/health") public String health() { return "UP"; }
    }
    public static void main(String[] args) throws ReflectiveOperationException {
        var endpoint = new Endpoint();
        for (var method : Endpoint.class.getDeclaredMethods()) {
            Route route = method.getAnnotation(Route.class);
            if (route != null) {
                System.out.println("ROUTE=" + route.value() + " RESULT=" + method.invoke(endpoint));
            }
        }
    }
}
