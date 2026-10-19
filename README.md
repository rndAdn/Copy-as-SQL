# Copy-as-SQL
IntelliJ Plugin for Ultimate Edition and DataGrip that allows you to copy SQL query results as SQL clauses and put them in the clipboard.

## Description
This plugin adds five options to the context menu when you right-click on selected data in a query result:
- Copy as SQL WHERE Clause
- Copy as SQL IN Clause
- Copy as SQL INSERT Clause
- Copy as SQL UPDATE Clause
- Copy as SQL DELETE Clause

## Installation
- In IntelliJ IDEA Ultimate or DataGrip, go to Settings/Preferences > Plugins
- Click "Marketplace" and search for "Copy as SQL"
- Click "Install" and restart the IDE

## Requirements
- IntelliJ IDEA Ultimate Edition or DataGrip
- Database plugin enabled
- Declared IDE range: 2022.2 through 2026.2 (`222`–`262.*`)

## SQL Values
Values are formatted using the database tools' SQL formatter and the connection's dialect. SQL `NULL` is kept distinct from the text `'NULL'`; predicates use `IS NULL` where needed.
MySQL/MariaDB and ClickHouse text escaping follows their default backslash-escaping mode. Review literals before using them with non-default session SQL modes.

## Usage

### Copy as WHERE Clause
Select data in your query results and right-click to copy as a WHERE clause.

![WHERE clause](WHERE_Clause.gif)

### Copy as IN Clause
Select data in your query results and right-click to copy as an IN clause.

![IN clause](IN_Clause.gif)

### Copy as INSERT Clause
Select data in your query results and right-click to copy as an INSERT clause. You will be prompted to enter a table name for the INSERT statement.

![INSERT clause](INSERT_Clause.gif)

### Copy as UPDATE Clause
Select data in your query results and right-click to copy as an UPDATE clause. Enter the table name, then the new **SQL value** for the last selected column, for example `'Bob'`, `42`, `''`, or `NULL`. The same new value is used for every selected row.

All selected columns, including the old value of the last column, are included in the WHERE clause. Only the last selected column is changed:

```sql
UPDATE people SET name = 'Bob'
WHERE id = 1 AND status = 'active' AND name = 'Alice';
```

Cancel either dialog to leave the clipboard unchanged. The plugin only copies SQL; it never executes it. Review generated UPDATE and DELETE statements before running them: identical selected values can match more than one database row.

### Copy as DELETE Clause
Select data in your query results and right-click to copy as a DELETE clause. You will be prompted to enter a table name for the DELETE statement. All columns are used in the WHERE clause to identify the rows to delete.

## Features

### Notifications
The plugin shows a notification when SQL is successfully copied to the clipboard, providing immediate feedback that the operation was successful.

### Error Handling
The plugin includes comprehensive error handling to provide user-friendly error messages when something goes wrong, such as:
- No data grid selected
- Failed to process SQL data
- Failed to copy to clipboard
- Other unexpected errors

## Development

Use **JDK 11** for Gradle and the platform-backed tests. The project builds against IDEA Ultimate 2022.2 to keep the oldest supported API and Java bytecode baseline; the plugin's compatibility range remains **222–262.\***. In IntelliJ, set **Settings → Build Tools → Gradle → Gradle JVM** to JDK 11; do not use the current IDE's bundled Java 25 to run this Gradle 7.6.6 wrapper.

```bash
# macOS, after installing a JDK 11
export JAVA_HOME="$(/usr/libexec/java_home -v 11)"

./gradlew runIde             # Separate IDEA 2026.2.3 development sandbox
./gradlew test               # SQL output, NULL, escaping, empty selection, cancellation
./gradlew buildPlugin        # build/distributions/Copy as SQL-1.0.8.zip
./gradlew runPluginVerifier  # Binary compatibility against the configured IDE matrix
```

`runIde` downloads IDEA 2026.2.3 and its matching JetBrains Runtime (JBR 25) on first use. Gradle, compilation, tests, and searchable-options generation stay on the 2022.2 baseline. The 2026 sandbox keeps its configuration and caches in `build/idea-sandbox-2026/`, separate from the baseline sandbox and your installed IDE. Internally, `runIde` delegates to `runCurrentIde`, a plain `JavaExec` built from the IDE's `product-info.json`: the Gradle IntelliJ 1.x launcher cannot parse 2026 JVM arguments and also ties tests to the original `runIde` SDK. `--args` and `--debug-jvm` on `runIde` are forwarded. The launcher selects the macOS, Windows, or Linux entry for the host architecture; only macOS aarch64 has been exercised.

The verifier targets IDEA Ultimate 2022.2, IDEA 2026.2.3, and DataGrip 2026.2.6. Its first run downloads IDE distributions and dependencies and can use several GB. Reports are written to `build/reports/pluginVerifier/`; test reports are in `build/reports/tests/test/`.
All three targets passed Plugin Verifier 1.410. On 2022.2, the missing **optional** Grid Core dependency is expected: those classes still belong to Database Tools in that version.

To try a packaged build in another IDE, use **Plugins → Install Plugin from Disk…** and select the generated ZIP. In the development sandbox, verify the five menu actions on selected query results, including apostrophes, SQL NULL, and cancellation of both UPDATE dialogs.

### Disposable PostgreSQL playground

Requires Docker with Docker Compose. From the repository root:

```bash
docker compose up -d --wait
```

Connect from DataGrip or IntelliJ's **Database** tool window using a **PostgreSQL** data source:

| Setting | Value |
| --- | --- |
| Host | `127.0.0.1` |
| Port | `55432` |
| Database | `copy_as_sql` |
| User | `copy_as_sql` |
| Password | `copy_as_sql` |
| JDBC URL | `jdbc:postgresql://127.0.0.1:55432/copy_as_sql` |
| Schema to introspect | `playground` |

The credentials are public test credentials, not production secrets. The port is bound to IPv4 loopback only. If `55432` is already in use, change the host port in `compose.yaml` and the connection settings.

`docker/postgres/init.sql` creates these fixtures without extensions:

| Table | Cases |
| --- | --- |
| `playground.scalar_types` | Integers and limits, exact decimals, floating point including Infinity/NaN, money, booleans, char/varchar/text, UUID, binary, JSON/JSONB |
| `playground.temporal_types` | Date, time, time with offset, timestamps with/without time zone, microseconds, intervals, infinity |
| `playground.postgres_types` | Enum, arrays including NULL elements and empty arrays, matrix, IPv4/IPv6, CIDR, MAC, bits, ranges/multirange, geometry, XML, full-text search |
| `playground."Odd Table"` | Quoted identifiers: `"Id"`, `"select"`, `"with space"`, `"a""quote"` |
| `playground.insert_target` | Empty copy of `scalar_types` for trying generated INSERT statements |

Rows distinguish SQL `NULL`, the text `'NULL'`, empty strings, JSON `null`, apostrophes, double quotes, backslashes, newlines, tabs and Unicode. These are test inputs, not a guarantee that every plugin action supports every PostgreSQL type: some types lack an equality operator, and SQL may require casts or quoted identifiers.

Open a query console and run:

```sql
SELECT * FROM playground.scalar_types ORDER BY id;
SELECT * FROM playground.temporal_types ORDER BY id;
SELECT * FROM playground.postgres_types ORDER BY id;
SELECT * FROM playground."Odd Table" ORDER BY "Id";
```

Select cells in the result grid, then try the plugin's context-menu actions. Start with `id`, `text_value`, `boolean_value`, `numeric_value`; include rows 2–5 to check escaping and NULL handling.

For INSERT, select all columns of `scalar_types` and enter `playground.insert_target` as the destination. Run the generated SQL inside `BEGIN; … ROLLBACK;` to keep the destination empty. For UPDATE/DELETE, use the same transaction wrapper and include `id` in the selection.

The database stores its data in **tmpfs**, not a persistent Docker volume. Stopping/restarting/recreating the container discards all edits; initialization runs again on the next start. To reset explicitly, including after changing the fixture SQL:

```bash
docker compose down
docker compose up -d --wait
```

To inspect or remove it:

```bash
docker compose logs postgres
docker compose exec postgres psql -U copy_as_sql -d copy_as_sql
docker compose down
```

## Publishing

1. Run `./gradlew test buildPlugin runPluginVerifier`, then manually check the ZIP in the target IDE.
2. Confirm that the version in `build.gradle` is not already published; update it and the change notes before a release.
3. Create a [Marketplace personal access token](https://plugins.jetbrains.com/author/me/tokens). Keep it out of the repository and shell history:

```bash
read -s ORG_GRADLE_PROJECT_intellijPublishToken
export ORG_GRADLE_PROJECT_intellijPublishToken
./gradlew publishPlugin
unset ORG_GRADLE_PROJECT_intellijPublishToken
```

`publishPlugin` uploads to the **default Marketplace channel**. It publishes a real release; building the ZIP does not publish anything. You can also upload the ZIP manually as a new version from the plugin's Marketplace page. A plugin's first publication must be uploaded manually.

See the [JetBrains publishing guide](https://plugins.jetbrains.com/docs/intellij/publishing-plugin.html). The historical `update.md` and `implementation_status.md` are planning notes, not a record of published releases; custom SQL templates and a settings page are not implemented.

## Version History
The 1.0.6–1.0.8 entries were drafted in the recovered work-in-progress; they are not confirmation that those versions were published.

- Unreleased (working version 1.0.8) - Support IDEs through 2026.2. Match all selected old values in UPDATE and prompt for the new SQL value of the last column. Fix SQL NULL predicates and cancelled dialogs. Format selected values using native database type information.
- 1.0.8 - Add notifications on successful copy operations. Improve error handling with user-friendly error messages.
- 1.0.7 - Add support for UPDATE and DELETE clauses.
- 1.0.6 - Add support for FLOAT, DOUBLE, BOOLEAN, DATE, TIME. Make table name configurable for INSERT clause. Add comprehensive documentation.
- 1.0.5 - Fix Compatibility with 2022.2
- 1.0.4 - Remove deprecated API usages
- 1.0.3 - Add support for DECIMAL, SMALLINT, TIMESTAMP, CHARACTER. Other types are computed as VARCHAR.
- 1.0.0 - Initial release with support for INTEGER and VARCHAR.
