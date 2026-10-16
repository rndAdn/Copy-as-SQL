package fr.radequin.copyassql;

import com.intellij.database.datagrid.*;
import com.intellij.database.Dbms;
import com.intellij.database.extractors.ObjectFormatter;
import com.intellij.database.extractors.ObjectFormatterMode;
import com.intellij.database.extractors.SqlInsertValuesExtractor;
import com.intellij.database.run.ui.DataAccessType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class SQLExtractor {


    private static List<Cell> extractSQLData(DataGrid grid) {
        SelectionModel<GridRow, GridColumn> model = grid.getSelectionModel();
        ModelIndexSet<GridColumn> columns = model.getSelectedColumns();
        ModelIndexSet<GridRow> rows = model.getSelectedRows();
        if (columns.asList().isEmpty() || rows.asList().isEmpty()) {
            throw new IllegalArgumentException("No cells selected.");
        }
        return getCells(rows, columns, grid);
    }

    public static Map<String, List<Cell>> extractSQLDataByColumn(DataGrid grid){
        // group by nom colonne
        return extractSQLData(grid).stream().collect(Collectors.groupingBy(Cell::getTitle));
    }

    public static Map<Integer, List<Cell>> extractSQLDataByRow(DataGrid grid){
        // group by row number
        return extractSQLData(grid).stream().collect(Collectors.groupingBy(Cell::getRow));
    }

    public static List<String> extractSQLColumnName(DataGrid grid) {
        GridModel<GridRow, GridColumn> dataModel = grid.getDataModel(DataAccessType.DATA_WITH_MUTATIONS);
        SelectionModel<GridRow, GridColumn> model = grid.getSelectionModel();
        ModelIndexSet<GridColumn> columns = model.getSelectedColumns();

        return columns.asList().stream().map(columnIdx -> {
            GridColumn column = Objects.requireNonNull(dataModel.getColumn(columnIdx));
            return column.getName();

        }).collect(Collectors.toList());
    }

    private static List<Cell> getCells(@NotNull ModelIndexSet<GridRow> rowsIdxs, @NotNull ModelIndexSet<GridColumn> columnsIdxs, @NotNull DataGrid grid) {

        GridModel<GridRow, GridColumn> dataModel = grid.getDataModel(DataAccessType.DATA_WITH_MUTATIONS);
        ObjectFormatter formatter = grid.getObjectFormatter();
        SqlInsertValuesExtractor extractor = new SqlInsertValuesExtractor(formatter, null);
        Dbms dbms = DataGridUtil.getDbms(grid);
        List<Cell> result = new ArrayList<>();

        for (ModelIndex<GridColumn> columnIdx : columnsIdxs.asList()) {
            GridColumn column = Objects.requireNonNull(dataModel.getColumn(columnIdx));
            for (ModelIndex<GridRow> rowIdx : rowsIdxs.asList()) {
                GridRow row = Objects.requireNonNull(dataModel.getRow(rowIdx));
                Object value = dataModel.getValueAt(rowIdx, columnIdx);
                // SQL_SCRIPT leaves strings unquoted; the native extractor supplies dialect-specific literals.
                String stringValue = formatter.objectToString(value, column, () -> ObjectFormatterMode.SQL_SCRIPT);
                String sqlValue = stringValue == null ? "NULL"
                        : formatter.isStringLiteral(column, value, ObjectFormatterMode.SQL_SCRIPT)
                        ? extractor.getStringValueLiteral(dbms, column, stringValue) : stringValue;
                result.add(new Cell(sqlValue, column.getName(), row.getRowNum()));
            }
        }
        return result;
    }



    protected static class Cell {
        private final int row;
        private final String value;
        private final String title;

        Cell(@NotNull String value, @NotNull String title, int row) {
            super();
            this.value = value;
            this.title = title;
            this.row = row;
        }

        public String getValue() {
            return value;
        }

        public String getCondition() {
            return title + ("NULL".equals(value) ? " IS NULL" : " = " + value);
        }

        public String getTitle() {
            return title;
        }


        public int getRow() {
            return row;
        }


        @Override
        public String toString() {
            return "Cell{" +
                    "value='" + value + '\'' +
                    ", title='" + title + '\'' +
                    '}';
        }
    }
}
