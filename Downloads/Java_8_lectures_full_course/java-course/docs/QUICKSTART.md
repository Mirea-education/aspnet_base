# Быстрый запуск преподавателя

## Подготовка

Установите полный JDK. Курс использует документацию Java 25, основной маршрут совместим с JDK 17+. Для дополнительных современных примеров нужен JDK 25. Скачивание JDK можно начать с [официального сайта Java](https://www.oracle.com/java/technologies/downloads/).

Откройте терминал в папке `java-course`.

```bash
java --version
javac --version
java tools/Run.java all
java tools/Run.java test
```

При корректной подготовке завершающая строка тестов — `TESTS_PASSED=20`. Все основные примеры работают локально. Если среда запрещает открывать даже локальные TCP-порты, сетевые примеры 07 и HTTP-проверки 08 потребуют обычного локального окружения.

## Финальный сервер

Для режима памяти переменная `COURSE_JDBC_URL` должна отсутствовать или быть пустой. Если она осталась после занятия с БД, удалите её в текущем терминале.

```bash
unset COURSE_JDBC_URL
java tools/Run.java server
```

В PowerShell используйте соответствующий вариант.

```powershell
Remove-Item Env:COURSE_JDBC_URL -ErrorAction SilentlyContinue
java tools/Run.java server
```

При занятом порте выберите другой.

```bash
java tools/Run.java server 8081
```

Остановка — `Ctrl+C`. Режим памяти после нового запуска снова содержит 5 единиц BOOK. Это отличается от консольного `DemoMain`, где для отдельного эксперимента задан остаток 3.

## Запросы через curl

В новом терминале выполняйте команды по одной. В PowerShell при наличии встроенной подмены имени curl используйте `curl.exe`.

```bash
curl -i http://127.0.0.1:8080/health
curl -i -X POST http://127.0.0.1:8080/orders -H "Content-Type: application/x-www-form-urlencoded" --data "key=demo-web&sku=BOOK&quantity=2"
curl -i -X POST http://127.0.0.1:8080/orders -H "Content-Type: application/x-www-form-urlencoded" --data "key=demo-web&sku=BOOK&quantity=2"
curl -i http://127.0.0.1:8080/orders/O1
```

У свежей памяти ожидаются статусы 200, 201, 200, 200. Первые два заказа в POST-ответах совпадают, потому что второй POST является повтором.

## Запросы через PowerShell

```powershell
Invoke-RestMethod -Uri 'http://127.0.0.1:8080/health'
Invoke-RestMethod -Method Post -Uri 'http://127.0.0.1:8080/orders' -ContentType 'application/x-www-form-urlencoded' -Body 'key=ps-demo&sku=BOOK&quantity=2'
Invoke-RestMethod -Method Post -Uri 'http://127.0.0.1:8080/orders' -ContentType 'application/x-www-form-urlencoded' -Body 'key=ps-demo&sku=BOOK&quantity=2'
```

Если перед этим выполнялся сценарий curl, остаток уже изменён. Для чистого эксперимента остановите и заново запустите сервер памяти. В PostgreSQL перезапуск сервера приложения не восстанавливает данные.

## Что делать при затруднении

| Наблюдение | Следующий шаг |
|---|---|
| `java` не найдена | Проверить установку JDK и PATH |
| Компилятор отсутствует | Использовать JDK с модулем компилятора |
| Не найден входной TSV | Запустить из корня java-course |
| Порт занят | Указать свободный порт в команде server |
| Сообщение о JDK 25 | Не запускать bonus25 на JDK 17 |
| JDBC-драйвер не найден | Проверить jar драйвера и разделитель classpath |
| Схема SQL не найдена | Выполнить нужный schema.sql в выбранной учебной базе |
