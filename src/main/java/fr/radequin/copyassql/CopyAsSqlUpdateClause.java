package fr.radequin.copyassql;

import com.intellij.database.datagrid.DataGrid;
import com.intellij.database.datagrid.DataGridUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.StringSelection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Action to copy selected data as an SQL UPDATE clause.
 * All selected values identify the row; a separately entered SQL value
 * replaces the last selected column.
 */
public class CopyAsSqlUpdateClause extends CopyAsSqlAction {

    /**
     * Performs the action of copying the selected data as an SQL UPDATE clause.
     * Prompts for a table name and a new SQL value for the last selected column.
     *
     * @param e The action event
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        try {
            DataGrid grid = DataGridUtil.getDataGrid(e.getDataContext());
            if (grid != null) {
                try {
                    Map<Integer, List<SQLExtractor.Cell>> rows = SQLExtractor.extractSQLDataByRow(grid);
                    List<SQLExtractor.Cell> firstRow = rows.values().iterator().next();
                    String columnName = firstRow.get(firstRow.size() - 1).getTitle();

                    String tableName = Messages.showInputDialog(
                        "Enter the table name for the UPDATE statement:",
                        "Table Name",
                        null,
                        "TableName",
                        null
                    );

                    if (tableName == null) {
                        return;
                    }
                    if (tableName.trim().isEmpty()) {
                        throw new IllegalArgumentException("Table name cannot be empty.");
                    }
                    String newValue = Messages.showInputDialog(
                        "Enter the new SQL value for " + columnName + " (examples: 'Bob', 42, NULL):",
                        "New SQL Value",
                        null
                    );
                    if (newValue == null) {
                        return;
                    }
                    if (newValue.trim().isEmpty()) {
                        throw new IllegalArgumentException("SQL value cannot be empty. Use '' for empty text or NULL for a null value.");
                    }

                    List<String> updateStatements = rows.values()
                            .stream()
                            .map(cells -> createUpdateStatement(tableName.trim(), cells, newValue.trim()))
                            .collect(Collectors.toList());

                    String result = String.join(";" + NEW_LINE, updateStatements) + ";";

                    try {
                        CopyPasteManager.getInstance().setContents(new StringSelection(result));
                        showCopySuccessNotification(e, "SQL UPDATE Clause");
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
     * Creates an UPDATE using every old value to identify the row.
     *
     * @param tableName The table to update
     * @param cells Selected cells in column order
     * @param newValue A SQL literal or expression entered by the user
     * @return An UPDATE of the last column, constrained by all selected old values
     */
    String createUpdateStatement(String tableName, List<SQLExtractor.Cell> cells, String newValue) {
        if (cells.isEmpty()) {
            throw new IllegalArgumentException("Select at least one column.");
        }
        SQLExtractor.Cell target = cells.get(cells.size() - 1);
        String whereClause = cells.stream()
                .map(SQLExtractor.Cell::getCondition)
                .collect(Collectors.joining(AND));

        return "UPDATE " + tableName + " SET " + target.getTitle() + " = " + newValue + " WHERE " + whereClause;
    }
}
