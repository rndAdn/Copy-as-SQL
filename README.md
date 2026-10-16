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

## SQL Values
Values are formatted using the database tools' SQL formatter and the connection's dialect. SQL `NULL` is kept distinct from the text `'NULL'`; predicates use `IS NULL` where needed.

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

## Version History
- Unreleased - Match all selected old values in UPDATE and prompt for the new SQL value of the last column. Fix SQL NULL predicates and cancelled dialogs. Format selected values using native database type information.
- 1.0.8 - Add notifications on successful copy operations. Improve error handling with user-friendly error messages.
- 1.0.7 - Add support for UPDATE and DELETE clauses.
- 1.0.6 - Add support for FLOAT, DOUBLE, BOOLEAN, DATE, TIME. Make table name configurable for INSERT clause. Add comprehensive documentation.
- 1.0.5 - Fix Compatibility with 2022.2
- 1.0.4 - Remove deprecated API usages
- 1.0.3 - Add support for DECIMAL, SMALLINT, TIMESTAMP, CHARACTER. Other types are computed as VARCHAR.
- 1.0.0 - Initial release with support for INTEGER and VARCHAR.
