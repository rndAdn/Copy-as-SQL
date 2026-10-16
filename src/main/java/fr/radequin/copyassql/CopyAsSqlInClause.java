package fr.radequin.copyassql;

import com.intellij.database.datagrid.*;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ide.CopyPasteManager;
import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.StringSelection;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Action to copy selected data as an SQL IN clause.
 * This action formats the selected data as values in an IN clause,
 * with each column's values in a separate IN clause.
 */
public class CopyAsSqlInClause extends CopyAsSqlAction {

    /**
     * Performs the action of copying the selected data as an SQL IN clause.
     *
     * @param e The action event
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        try {
            DataGrid grid = DataGridUtil.getDataGrid(e.getDataContext());
            if (grid != null) {
                try {
                    Map<String, List<SQLExtractor.Cell>> groupByColumn = SQLExtractor.extractSQLDataByColumn(grid);

                    List<String> listOfInClauses = groupByColumn.entrySet()
                            .stream()
                            .map(entry -> collect(entry.getKey(), entry.getValue()))
                            .collect(Collectors.toList());
                    String result = String.join("\n", listOfInClauses);

                    try {
                        CopyPasteManager.getInstance().setContents(new StringSelection(result));
                        showCopySuccessNotification(e, "SQL IN Clause");
                    } catch (Exception ex) {
                        showErrorNotification(e, "Failed to copy to clipboard: " + ex.getMessage());
                    }
                } catch (Exception ex) {
                    showErrorNotification(e, "Failed to process SQL data: " + ex.getMessage());
                }
            } else {
                showErrorNotification(e, "No data grid selected. Please select data in a query result.");
            }
        } catch (Exception ex) {
            showErrorNotification(e, "An unexpected error occurred: " + ex.getMessage());
        }
    }

    /**
     * Formats a column's values as an IN clause.
     * SQL nulls are matched separately from the column's non-null SQL literals.
     *
     * @param columnName The name of the column
     * @param cells The list of cells containing the values
     * @return A string representing the IN clause
     */
    String collect(String columnName, List<SQLExtractor.Cell> cells){
        StringJoiner values = new StringJoiner(", ");
        boolean hasNull = false;
        for (SQLExtractor.Cell cell : cells) {
            if ("NULL".equals(cell.getValue())) {
                hasNull = true;
            } else {
                values.add(cell.getValue());
            }
        }
        if (values.length() == 0) {
            return columnName + " IS NULL";
        }
        String inClause = columnName + IN + "(" + values + ")";
        return hasNull ? "(" + inClause + OR + columnName + " IS NULL)" : inClause;
    }

}
