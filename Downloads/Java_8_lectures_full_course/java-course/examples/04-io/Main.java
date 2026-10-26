import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Main {
    record Line(String sku, int quantity, BigDecimal unitPrice) {
        Line {
            if (!sku.matches("[A-Z][A-Z0-9_-]{0,31}") || quantity < 1 || quantity > 1000
                    || unitPrice.signum() < 0 || unitPrice.precision() > 12)
                throw new IllegalArgumentException("Invalid line");
            unitPrice = unitPrice.setScale(2, java.math.RoundingMode.UNNECESSARY);
        }
        BigDecimal total() { return unitPrice.multiply(BigDecimal.valueOf(quantity)); }
    }

    // Это ограниченный учебный TSV без кавычек и переносов внутри полей.
    static List<Line> read(Path path) throws IOException {
        if (Files.size(path) > 65_536) throw new IOException("Input exceeds 64 KiB");
        var result = new ArrayList<Line>();
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String row;
            int number = 0;
            while ((row = reader.readLine()) != null) {
                number++;
                if (number > 1000) throw new IOException("Too many rows");
                String[] cells = row.split("\t", -1);
                try {
                    if (cells.length != 3) throw new IllegalArgumentException("Expected 3 columns");
                    result.add(new Line(cells[0], Integer.parseInt(cells[1]), new BigDecimal(cells[2])));
                } catch (IllegalArgumentException | ArithmeticException e) {
                    throw new IOException("Invalid row " + number, e);
                }
            }
        }
        if (result.isEmpty()) throw new IOException("Empty import");
        return List.copyOf(result);
    }

    public static void main(String[] args) throws IOException {
        Path input = args.length == 0 ? Path.of("data/order-lines.tsv") : Path.of(args[0]);
        List<Line> lines = read(input);
        BigDecimal total = new BigDecimal("0.00");
        for (Line line : lines) total = total.add(line.total());
        Clock clock = Clock.fixed(Instant.parse("2026-01-15T09:00:00Z"), ZoneOffset.UTC);
        String report = "TOTAL_RUB=" + total.toPlainString() + "\n" +
                "CREATED=" + DateTimeFormatter.ISO_INSTANT.format(clock.instant()) + "\n";
        Path temp = Files.createTempFile("java-course-report-", ".txt");
        try {
            Files.writeString(temp, report, StandardCharsets.UTF_8);
            System.out.print(Files.readString(temp, StandardCharsets.UTF_8));
        } finally { Files.deleteIfExists(temp); }
        System.out.println("DECIMAL_EQUALS=" + new BigDecimal("1.0").equals(new BigDecimal("1.00")));
        System.out.println("DECIMAL_COMPARE=" + new BigDecimal("1.0").compareTo(new BigDecimal("1.00")));
    }
}
