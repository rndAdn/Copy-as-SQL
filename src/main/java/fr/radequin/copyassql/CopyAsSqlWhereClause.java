package fr.radequin.copyassql;

import com.intellij.database.datagrid.DataGrid;
import com.intellij.database.datagrid.DataGridUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ide.CopyPasteManager;
import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.StringSelection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Action to copy selected data as an SQL WHERE clause.
 * This action formats the selected data as conditions in a WHERE clause,
 * with each row's conditions combined with AND operators, and rows combined with OR operators.
 */
public class CopyAsSqlWhereClause extends CopyAsSqlAction {

    /**
     * Performs the action of copying the selected data as an SQL WHERE clause.
     *
     * @param e The action event
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        try {
            DataGrid grid = DataGridUtil.getDataGrid(e.getDataContext());
            if (grid != null) {
                try {
                    Map<Integer, List<SQLExtractor.Cell>> map1 = SQLExtractor.extractSQLDataByRow(grid);

                    List<String> map2 = map1.values()
                            .stream()
                            .map(this::collect)
                            .collect(Collectors.toList());

                    String result = WHERE + String.join(OR + NEW_LINE, map2);

                    try {
                        CopyPasteManager.getInstance().setContents(new StringSelection(result));
                        showCopySuccessNotification(e, "SQL WHERE Clause");
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
     * Formats a list of cells as a condition for a WHERE clause.
     * Null values use IS NULL; each row's conditions are combined with AND operators.
     *
     * @param cells The list of cells to format
     * @return A string representing the condition
     */
    String collect(List<SQLExtractor.Cell> cells){
        return " (" +
                cells.stream()
                        .map(SQLExtractor.Cell::getCondition)
                        .collect(Collectors.joining(AND)) +
                ")";
    }



}
