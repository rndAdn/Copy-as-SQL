package fr.radequin.copyassql;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CopyAsSqlUpdateClauseTest {
    private final CopyAsSqlUpdateClause action = new CopyAsSqlUpdateClause();

    @Test
    void changesOnlyLastColumnAndMatchesAllOldValues() {
        List<SQLExtractor.Cell> cells = List.of(
                new SQLExtractor.Cell("1", "id", 0),
                new SQLExtractor.Cell("'active'", "status", 0),
                new SQLExtractor.Cell("'Alice'", "name", 0));

        assertEquals("UPDATE people SET name = 'Bob' WHERE id = 1 AND status = 'active' AND name = 'Alice'",
                action.createUpdateStatement("people", cells, "'Bob'"));
    }

    @Test
    void singleSelectedColumnStillHasAnOldValuePredicate() {
        assertEquals("UPDATE people SET name = 'Bob' WHERE name = 'Alice'",
                action.createUpdateStatement("people",
                        List.of(new SQLExtractor.Cell("'Alice'", "name", 0)), "'Bob'"));
    }

    @Test
    void matchesSqlNullWithoutConfusingItWithTextNull() {
        List<SQLExtractor.Cell> cells = List.of(
                new SQLExtractor.Cell("'NULL'", "description", 0),
                new SQLExtractor.Cell("NULL", "name", 0));

        assertEquals("UPDATE people SET name = 'O''Reilly' WHERE description = 'NULL' AND name IS NULL",
                action.createUpdateStatement("people", cells, "'O''Reilly'"));
    }

    @Test
    void acceptsAnExplicitSqlNullAsTheNewValue() {
        assertEquals("UPDATE people SET name = NULL WHERE name = 'Alice'",
                action.createUpdateStatement("people",
                        List.of(new SQLExtractor.Cell("'Alice'", "name", 0)), "NULL"));
    }

    @Test
    void cannotGenerateAnUnconstrainedUpdateFromNoCells() {
        assertThrows(IllegalArgumentException.class,
                () -> action.createUpdateStatement("people", List.of(), "'Bob'"));
    }
}
