import java.lang.foreign.Arena;
import java.lang.foreign.ValueLayout;

public class NativeMemory {
    public static void main(String[] args) {
        try (var arena = Arena.ofConfined()) {
            var segment = arena.allocate(ValueLayout.JAVA_INT);
            segment.set(ValueLayout.JAVA_INT, 0, 42);
            System.out.println("NATIVE_VALUE=" + segment.get(ValueLayout.JAVA_INT, 0));
        }
    }
}
