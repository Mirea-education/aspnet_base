import javax.tools.ToolProvider;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Run {
    static final String[] NAMES = {"01-start", "02-oop", "03-collections", "04-io", "05-streams",
            "06-concurrency", "07-integration", "08-service"};
    static Path javaBinary() {
        return Path.of(System.getProperty("java.home"), "bin",
                System.getProperty("os.name").startsWith("Windows") ? "java.exe" : "java");
    }
    static void compile(Path source, Path output, int release, String classpath) throws Exception {
        Files.createDirectories(output);
        var compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) throw new IllegalStateException("Install a JDK, not only a JRE");
        List<String> options = new ArrayList<>(List.of("-encoding", "UTF-8", "--release",
                Integer.toString(release), "-d", output.toString()));
        if (classpath != null) options.addAll(List.of("-classpath", classpath));
        try (var paths = Files.walk(source)) {
            paths.filter(p -> p.toString().endsWith(".java"))
                    .filter(p -> !p.toString().contains("bonus25") || release >= 25)
                    .sorted().forEach(p -> options.add(p.toString()));
        }
        int exit = compiler.run(null, System.out, System.err, options.toArray(String[]::new));
        if (exit != 0) throw new IllegalStateException("Compilation failed, exit " + exit);
    }
    static Path build(int number) throws Exception {
        String name = NAMES[number - 1];
        Path source = Path.of("examples", name);
        if (number == 8) source = source.resolve("src");
        Path output = Path.of("build", String.format("%02d", number));
        if (Files.exists(output)) {
            try (var paths = Files.walk(output)) {
                for (Path path : paths.sorted(java.util.Comparator.reverseOrder()).toList()) Files.delete(path);
            }
        }
        compile(source, output, 17, null);
        return output;
    }
    static void launch(String classpath, String main, List<String> args) throws Exception {
        List<String> command = new ArrayList<>(List.of(javaBinary().toString(), "-Dfile.encoding=UTF-8",
                "-cp", classpath, main));
        command.addAll(args);
        int exit = new ProcessBuilder(command).inheritIO().start().waitFor();
        if (exit != 0) throw new IllegalStateException("Program failed, exit " + exit);
    }
    static void run(int number) throws Exception {
        System.out.println("[LECTURE " + String.format("%02d", number) + "]");
        launch(build(number).toString(), number == 8 ? "course.orders.app.DemoMain" : "Main", List.of());
    }
    public static void main(String[] args) throws Exception {
        if (!Files.isDirectory(Path.of("lectures"))) throw new IllegalStateException("Run from java-course directory");
        if (Runtime.version().feature() < 17) throw new IllegalStateException("JDK 17 or newer is required");
        String mode = args.length == 0 ? "help" : args[0];
        switch (mode) {
            case "all" -> { for (int n = 1; n <= 8; n++) run(n); }
            case "test" -> {
                Path app = build(8);
                Path tests = Path.of("build", "tests");
                compile(Path.of("examples", NAMES[7], "tests"), tests, 17, app.toString());
                launch(app + java.io.File.pathSeparator + tests, "ServiceTest", List.of());
            }
            case "server" -> launch(build(8).toString(), "course.orders.app.ServerMain",
                    args.length > 1 ? List.of(args[1]) : List.of());
            case "metadata" -> launch(build(8).toString(), "course.orders.app.MetadataDemo", List.of());
            case "jar" -> {
                Path app = build(8);
                var jar = java.util.spi.ToolProvider.findFirst("jar").orElseThrow();
                int exit = jar.run(System.out, System.err, "--create", "--file", "build/orders.jar",
                        "--main-class", "course.orders.app.DemoMain", "-C", app.toString(), ".");
                if (exit != 0) throw new IllegalStateException("jar failed");
                System.out.println("JAR=build/orders.jar");
            }
            case "bonus25" -> {
                if (Runtime.version().feature() < 25) throw new IllegalStateException("Bonus requires JDK 25+");
                Path output = Path.of("build", "bonus25");
                compile(Path.of("examples", NAMES[5], "bonus25"), output, 25, null);
                compile(Path.of("examples", NAMES[7], "bonus25"), output, 25, null);
                launch(output.toString(), "ModernConcurrency", List.of());
                launch(output.toString(), "NativeMemory", List.of());
                launch(output.toString(), "ModernModel", List.of());
            }
            default -> {
                if (mode.matches("0?[1-8]")) run(Integer.parseInt(mode));
                else System.out.println("Use all, 01..08, test, server [port], jar, metadata, bonus25");
            }
        }
    }
}
