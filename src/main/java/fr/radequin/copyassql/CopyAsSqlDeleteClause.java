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
 * Action to copy selected data as an SQL DELETE clause.
 * This action formats the selected data as conditions in a DELETE statement,
 * with each row's conditions combined with AND operators, and rows combined with semicolons.
 */
public class CopyAsSqlDeleteClause extends CopyAsSqlAction {

    /**
     * Performs the action of copying the selected data as an SQL DELETE clause.
     * Prompts the user for a table name and formats the data as a DELETE statement.
     *
     * @param e The action event
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        try {
            DataGrid grid = DataGridUtil.getDataGrid(e.getDataContext());
            if (grid != null) {
                try {
                    // Prompt the user for a table name
                    final String inputTableName = Messages.showInputDialog(
                        "Enter the table name for the DELETE statement:",
                        "Table Name",
                        null,
                        "TableName",
                        null
                    );

                    if (inputTableName == null) {
                        return;
                    }
                    final String tableName = inputTableName.trim().isEmpty()
                        ? "TableName" 
                        : inputTableName;

                    Map<Integer, List<SQLExtractor.Cell>> map1 = SQLExtractor.extractSQLDataByRow(grid);

                    List<String> deleteStatements = map1.values()
                            .stream()
                            .map(cells -> createDeleteStatement(tableName, cells))
                            .collect(Collectors.toList());

                    String result = String.join(";" + NEW_LINE, deleteStatements) + ";";

                    try {
                        CopyPasteManager.getInstance().setContents(new StringSelection(result));
                        showCopySuccessNotification(e, "SQL DELETE Clause");
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
     * Creates a DELETE statement for a single row of data.
     *
     * @param tableName The name of the table to delete from
     * @param cells The list of cells containing the values
     * @return A string representing the DELETE statement
     */
    String createDeleteStatement(String tableName, List<SQLExtractor.Cell> cells) {
        // Create the WHERE clause with all columns
        String whereClause = cells.stream()
                .map(SQLExtractor.Cell::getCondition)
                .collect(Collectors.joining(AND));

        return "DELETE FROM " + tableName + " WHERE " + whereClause;
    }
}
