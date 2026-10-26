import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    static final class BrokenCounter {
        volatile int value;
        final CyclicBarrier barrier = new CyclicBarrier(2);
        void increment() {
            int snapshot = value;
            try { barrier.await(2, TimeUnit.SECONDS); }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(e);
            } catch (java.util.concurrent.BrokenBarrierException | java.util.concurrent.TimeoutException e) {
                throw new IllegalStateException(e);
            }
            value = snapshot + 1;
        }
    }
    static final class Stock {
        private int available = 1;
        synchronized boolean reserve() {
            if (available == 0) return false;
            available--;
            return true;
        }
    }
    public static void main(String[] args) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            var broken = new BrokenCounter();
            var a = pool.submit(broken::increment);
            var b = pool.submit(broken::increment);
            a.get(3, TimeUnit.SECONDS); b.get(3, TimeUnit.SECONDS);
            System.out.println("BROKEN=" + broken.value);
            var safe = new AtomicInteger();
            var c = pool.submit(() -> safe.incrementAndGet());
            var d = pool.submit(() -> safe.incrementAndGet());
            c.get(3, TimeUnit.SECONDS); d.get(3, TimeUnit.SECONDS);
            System.out.println("ATOMIC=" + safe.get());
            var stock = new Stock();
            var e = pool.submit(stock::reserve);
            var f = pool.submit(stock::reserve);
            int accepted = (e.get() ? 1 : 0) + (f.get() ? 1 : 0);
            System.out.println("RESERVED=" + accepted);
            var price = CompletableFuture.supplyAsync(() -> 1000, pool);
            var delivery = CompletableFuture.supplyAsync(() -> 200, pool);
            System.out.println("QUOTE=" + price.thenCombine(delivery, Integer::sum)
                    .orTimeout(2, TimeUnit.SECONDS).join());
        } finally {
            pool.shutdownNow();
            if (!pool.awaitTermination(5, TimeUnit.SECONDS)) throw new IllegalStateException("Pool did not stop");
        }
    }
}
