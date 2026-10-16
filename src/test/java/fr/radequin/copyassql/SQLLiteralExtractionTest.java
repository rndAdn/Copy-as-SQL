package fr.radequin.copyassql;

import com.intellij.database.Dbms;
import com.intellij.database.datagrid.DataConsumer;
import com.intellij.database.datagrid.DataGrid;
import com.intellij.database.datagrid.DataGridUtil;
import com.intellij.database.datagrid.GridColumn;
import com.intellij.database.datagrid.GridModel;
import com.intellij.database.datagrid.GridRow;
import com.intellij.database.datagrid.ModelIndex;
import com.intellij.database.datagrid.ModelIndexSet;
import com.intellij.database.datagrid.SelectionModel;
import com.intellij.database.extractors.DbObjectFormatter;
import com.intellij.database.run.ReservedCellValue;
import com.intellij.database.run.ui.DataAccessType;
import com.intellij.testFramework.fixtures.IdeaTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;

import java.sql.Types;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class SQLLiteralExtractionTest {
    private static IdeaTestFixture fixture;

    @BeforeAll
    static void startPlatform() throws Exception {
        fixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture();
        fixture.setUp();
    }

    @AfterAll
    static void stopPlatform() throws Exception {
        fixture.tearDown();
    }

    static Stream<Arguments> stringLiterals() {
        return Stream.of(
                Arguments.of(Dbms.POSTGRES, Types.VARCHAR, "VARCHAR", "O'Reilly", "'O''Reilly'"),
                Arguments.of(Dbms.POSTGRES, Types.VARCHAR, "VARCHAR", "a\\b", "'a\\b'"),
                Arguments.of(Dbms.MYSQL, Types.VARCHAR, "VARCHAR", "a'\\b", "'a''\\\\b'"),
                Arguments.of(Dbms.MARIA, Types.VARCHAR, "VARCHAR", "a\\b", "'a\\\\b'"),
                Arguments.of(Dbms.CLICKHOUSE, Types.VARCHAR, "String", "a\\b", "'a\\\\b'"),
                Arguments.of(Dbms.MSSQL, Types.NVARCHAR, "NVARCHAR", "é'", "N'é'''"),
                Arguments.of(Dbms.MYSQL, Types.BIT, "BIT", "101", "b'101'"),
                Arguments.of(Dbms.POSTGRES, Types.VARCHAR, "VARCHAR", "", "''")
        );
    }

    @ParameterizedTest
    @MethodSource("stringLiterals")
    void quotesSqlTextWithoutChangingItsValue(Dbms dbms, int type, String typeName, String value, String expected) {
        DataConsumer.Column column = mock(DataConsumer.Column.class);
        when(column.getType()).thenReturn(type);
        when(column.getTypeName()).thenReturn(typeName);
        when(column.getSize()).thenReturn(3);

        assertEquals(expected, SQLExtractor.stringLiteral(dbms, column, value));
    }
    static Stream<Arguments> sqlValues() {
        return Stream.of(
                Arguments.of("O'Reilly", Types.VARCHAR, "'O''Reilly'"),
                Arguments.of("NULL", Types.VARCHAR, "'NULL'"),
                Arguments.of(null, Types.VARCHAR, "NULL"),
                Arguments.of(ReservedCellValue.NULL, Types.VARCHAR, "NULL"),
                Arguments.of(42, Types.INTEGER, "42")
        );
    }

    @ParameterizedTest
    @MethodSource("sqlValues")
    @SuppressWarnings("unchecked")
    void extractionProducesNativeSqlLiterals(Object value, int type, String expected) {
        DataGrid grid = mock(DataGrid.class);
        GridModel<GridRow, GridColumn> model = mock(GridModel.class);
        SelectionModel<GridRow, GridColumn> selection = mock(SelectionModel.class);
        ModelIndexSet<GridColumn> columns = mock(ModelIndexSet.class);
        ModelIndexSet<GridRow> rows = mock(ModelIndexSet.class);
        ModelIndex<GridColumn> columnIndex = mock(ModelIndex.class);
        ModelIndex<GridRow> rowIndex = mock(ModelIndex.class);
        GridColumn column = mock(GridColumn.class);
        GridRow row = mock(GridRow.class);

        when(grid.getSelectionModel()).thenReturn(selection);
        when(selection.getSelectedColumns()).thenReturn(columns);
        when(selection.getSelectedRows()).thenReturn(rows);
        when(columns.asList()).thenReturn(List.of(columnIndex));
        when(rows.asList()).thenReturn(List.of(rowIndex));
        when(grid.getDataModel(DataAccessType.DATA_WITH_MUTATIONS)).thenReturn(model);
        when(grid.getObjectFormatter()).thenReturn(new DbObjectFormatter(Dbms.POSTGRES));
        when(model.getColumn(columnIndex)).thenReturn(column);
        when(model.getRow(rowIndex)).thenReturn(row);
        when(model.getValueAt(rowIndex, columnIndex)).thenReturn(value);
        when(column.getName()).thenReturn("value");
        when(column.getType()).thenReturn(type);
        when(row.getRowNum()).thenReturn(1);
        if (type == Types.VARCHAR) {
            when(column.getTypeName()).thenReturn("VARCHAR");
        }

        try (MockedStatic<DataGridUtil> grids = mockStatic(DataGridUtil.class)) {
            grids.when(() -> DataGridUtil.getDbms(grid)).thenReturn(Dbms.POSTGRES);

            List<SQLExtractor.Cell> cells = SQLExtractor.extractSQLDataByRow(grid).get(1);

            assertEquals(1, cells.size());
            assertEquals(expected, cells.get(0).getValue());
            assertEquals(" (" + ("NULL".equals(expected) ? "value IS NULL" : "value = " + expected) + ")",
                    new CopyAsSqlWhereClause().collect(cells));
            assertEquals(" (" + expected + ")", new CopyAsSqlInsertClause().collect(cells));
        }
    }
}
