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



public class CopyAsSqlInsertClause extends CopyAsSqlAction {



    /**
     * Performs the action of copying the selected data as an SQL INSERT clause.
     * Prompts the user for a table name and formats the data as an INSERT statement.
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
                    String tableName = Messages.showInputDialog(
                        "Enter the table name for the INSERT statement:",
                        "Table Name",
                        null,
                        "TableName",
                        null
                    );

                    if (tableName == null) {
                        return;
                    }
                    if (tableName.trim().isEmpty()) {
                        tableName = "TableName";
                    }

                    Map<Integer, List<SQLExtractor.Cell>> map1 = SQLExtractor.extractSQLDataByRow(grid);

                    String result = "INSERT INTO " + tableName + " (" + String.join(", ", SQLExtractor.extractSQLColumnName(grid)) + ") \nVALUES" ;
                    List<String> map2 = map1.values()
                            .stream()
                            .map(this::collect)
                            .collect(Collectors.toList());

                    result += String.join(",\n", map2);

                    try {
                        CopyPasteManager.getInstance().setContents(new StringSelection(result));
                        showCopySuccessNotification(e, "SQL INSERT Clause");
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
     * Formats a list of cells as a VALUES tuple for an INSERT statement.
     *
     * @param cells The list of cells to format
     * @return A string representing the VALUES tuple
     */
    String collect(List<SQLExtractor.Cell> cells){
        return " (" +
                cells.stream()
                        .map(SQLExtractor.Cell::getValue)
                        .collect(Collectors.joining(", ")) +
                ")";
    }



}
