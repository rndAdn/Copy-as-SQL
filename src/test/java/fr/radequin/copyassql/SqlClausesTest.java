package fr.radequin.copyassql;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SqlClausesTest {

    private final List<SQLExtractor.Cell> cells = List.of(
            new SQLExtractor.Cell("'O''Reilly'", "author", 1),
            new SQLExtractor.Cell("NULL", "missing", 1),
            new SQLExtractor.Cell("'NULL'", "text_value", 1),
            new SQLExtractor.Cell("42", "id", 1),
            new SQLExtractor.Cell("DATE '2026-10-16'", "created", 1)
    );

    @Test
    void wherePreservesSqlLiteralsAndUsesIsNull() {
        assertEquals(" (author = 'O''Reilly' AND missing IS NULL AND text_value = 'NULL'"
                        + " AND id = 42 AND created = DATE '2026-10-16')",
                new CopyAsSqlWhereClause().collect(cells));
    }

    @Test
    void insertPreservesSqlLiteralsIncludingSqlNull() {
        assertEquals(" ('O''Reilly', NULL, 'NULL', 42, DATE '2026-10-16')",
                new CopyAsSqlInsertClause().collect(cells));
    }

    @Test
    void deletePreservesSqlLiteralsAndUsesIsNull() {
        assertEquals("DELETE FROM books WHERE author = 'O''Reilly' AND missing IS NULL"
                        + " AND text_value = 'NULL' AND id = 42 AND created = DATE '2026-10-16'",
                new CopyAsSqlDeleteClause().createDeleteStatement("books", cells));
    }

    @Test
    void inWithoutSqlNullKeepsOrdinaryInClause() {
        assertEquals("author IN ('O''Reilly', 'NULL')",
                new CopyAsSqlInClause().collect("author", List.of(
                        new SQLExtractor.Cell("'O''Reilly'", "author", 1),
                        new SQLExtractor.Cell("'NULL'", "author", 2))));
    }

    @Test
    void inWithSqlNullIncludesSeparateNullCondition() {
        assertEquals("(author IN ('O''Reilly', 'NULL') OR author IS NULL)",
                new CopyAsSqlInClause().collect("author", List.of(
                        new SQLExtractor.Cell("NULL", "author", 1),
                        new SQLExtractor.Cell("'O''Reilly'", "author", 2),
                        new SQLExtractor.Cell("'NULL'", "author", 3))));
    }

    @Test
    void inWithOnlySqlNullUsesIsNull() {
        assertEquals("author IS NULL",
                new CopyAsSqlInClause().collect("author", List.of(
                        new SQLExtractor.Cell("NULL", "author", 1),
                        new SQLExtractor.Cell("NULL", "author", 2))));
    }
}
