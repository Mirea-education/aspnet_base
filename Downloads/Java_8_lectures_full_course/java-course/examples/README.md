# Запускаемые примеры

Все команды ниже выполняются из корня `java-course`. `Run.java` вызывает компилятор текущего JDK, создаёт каталог `build`, затем запускает нужный класс. Исходники не требуют предварительного выполнения предыдущих занятий.

| Команда | Что наблюдаем | Основной файл |
|---|---|---|
| `java tools/Run.java 01` | Сумма, скидка, массив и цикл | [01-start/Main.java](01-start/Main.java) |
| `java tools/Run.java 02` | Защитное копирование и состояния | [02-oop/Main.java](02-oop/Main.java) |
| `java tools/Run.java 03` | Каталог, generics и перегрузка remove | [03-collections/Main.java](03-collections/Main.java) |
| `java tools/Run.java 04` | TSV, BigDecimal, Clock и временный файл | [04-io/Main.java](04-io/Main.java) |
| `java tools/Run.java 05` | Фильтрация, группировка и ленивость | [05-streams/Main.java](05-streams/Main.java) |
| `java tools/Run.java 06` | Потеря обновления, атомарность и future | [06-concurrency/Main.java](06-concurrency/Main.java) |
| `java tools/Run.java 07` | Локальный HTTP-клиент и сервер | [07-integration/Main.java](07-integration/Main.java) |
| `java tools/Run.java 08` | Атомарный резерв и повтор команды | [08-service/src/course/orders/app/DemoMain.java](08-service/src/course/orders/app/DemoMain.java) |
| `java tools/Run.java metadata` | Аннотация и вызов через reflection | [MetadataDemo.java](08-service/src/course/orders/app/MetadataDemo.java) |
| `java tools/Run.java test` | 20 проверок предметного сценария и HTTP | [ServiceTest.java](08-service/tests/ServiceTest.java) |
| `java tools/Run.java server` | Работающий локальный HTTP API | [ServerMain.java](08-service/src/course/orders/app/ServerMain.java) |
| `java tools/Run.java jar` | Исполняемый и модульный JAR | [module-info.java](08-service/src/module-info.java) |
| `java tools/Run.java bonus25` | Virtual threads, ScopedValue, FFM, patterns | [ModernConcurrency.java](06-concurrency/bonus25/ModernConcurrency.java), [NativeMemory.java](08-service/bonus25/NativeMemory.java), [ModernModel.java](08-service/bonus25/ModernModel.java) |

Последняя команда требует JDK 25. Все остальные основные команды используют API Java 17+. В основном маршруте нет намеренно некомпилируемых файлов. Ошибочные строки заданий находятся в Markdown и явно отмечены.

[Полная проверка](../docs/VERIFICATION.md) · [JDBC с PostgreSQL](../docs/JDBC-LAB.md)
