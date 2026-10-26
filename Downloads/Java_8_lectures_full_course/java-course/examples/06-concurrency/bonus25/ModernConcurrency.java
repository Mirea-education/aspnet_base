import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class ModernConcurrency {
    private static final ScopedValue<String> REQUEST = ScopedValue.newInstance();
    public static void main(String[] args) throws Exception {
        var slots = new Semaphore(2);
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var task = executor.submit(() -> {
                slots.acquire();
                try {
                    return ScopedValue.where(REQUEST, "demo-42").call(() ->
                            "VIRTUAL=" + Thread.currentThread().isVirtual() + " REQUEST=" + REQUEST.get());
                } finally { slots.release(); }
            });
            System.out.println(task.get());
        }
    }
}
