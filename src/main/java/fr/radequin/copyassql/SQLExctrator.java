package fr.radequin.copyassql;

import com.intellij.database.datagrid.*;
import com.intellij.database.extractors.ObjectFormatter;
import com.intellij.database.run.ui.DataAccessType;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SQLExctrator {


    private static List<Cell> extractSQLData(DataGrid grid){
        SelectionModel<DataConsumer.Row, DataConsumer.Column> model = grid.getSelectionModel();
        ModelIndexSet<DataConsumer.Column> columns = model.getSelectedColumns();
        ModelIndexSet<DataConsumer.Row> rows = model.getSelectedRows();
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

    public static List<String> extractSQLColumnName(DataGrid grid){
        GridModel<DataConsumer.Row, DataConsumer.Column> dataModel = grid.getDataModel(DataAccessType.DATA_WITH_MUTATIONS);
        SelectionModel<DataConsumer.Row, DataConsumer.Column> model = grid.getSelectionModel();
        ModelIndexSet<DataConsumer.Column> columns = model.getSelectedColumns();

        return columns.asList().stream().map(columnIdx -> {
            DataConsumer.Column column = ObjectUtils.notNull(dataModel.getColumn(columnIdx));
            return column.name;

        }).collect(Collectors.toList());
    }

    private static List<Cell> getCells(@NotNull ModelIndexSet<DataConsumer.Row> rowsIdxs, @NotNull ModelIndexSet<DataConsumer.Column> columnsIdxs, @NotNull DataGrid grid) {

        GridModel<DataConsumer.Row, DataConsumer.Column> dataModel = grid.getDataModel(DataAccessType.DATA_WITH_MUTATIONS);
        ObjectFormatter formatter = grid.getObjectFormatter();
        List<Cell> result = new ArrayList<>();

        for (ModelIndex<DataConsumer.Column> columnIdx : columnsIdxs.asList()) {
            DataConsumer.Column column = ObjectUtils.notNull(dataModel.getColumn(columnIdx));
            for (ModelIndex<DataConsumer.Row> rowIdx : rowsIdxs.asList()) {
                DataConsumer.Row row = ObjectUtils.notNull(dataModel.getRow(rowIdx));
                Object value = dataModel.getValueAt(rowIdx, columnIdx);
                String stringValue = formatter.getPlainValue(value, column, DataGridUtil.getDbms(grid));
                result.add(new Cell(stringValue, column.name, column.typeName, row.rowNum));
            }
        }
        return result;
    }



    protected static class Cell {
        private final int row;
        private final String value;
        private final String title;
        private final String type;

        Cell(@NotNull String value, @NotNull String title, @NotNull String type, int row) {
            super();
            this.value = value;
            this.title = title;
            this.type = type;
            this.row = row;
        }

        public String getValue() {
            return value;
        }

        public String getTitle() {
            return title;
        }

        public String getType() {
            return type;
        }

        public int getRow() {
            return row;
        }


        @Override
        public String toString() {
            return "Cell{" +
                    "value='" + value + '\'' +
                    ", title='" + title + '\'' +
                    ", type='" + type + '\'' +
                    '}';
        }
    }
}
