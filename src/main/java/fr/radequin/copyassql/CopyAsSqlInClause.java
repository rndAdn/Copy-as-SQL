package fr.radequin.copyassql;

import com.intellij.database.datagrid.*;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ide.CopyPasteManager;
import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.StringSelection;
import java.util.*;
import java.util.stream.Collectors;

public class CopyAsSqlInClause extends CopyAsSqlAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

        DataGrid grid = DataGridUtil.getDataGrid(e.getDataContext());
        if (grid != null) {
            Map<String, List<SQLExctrator.Cell>> groupByColumn = SQLExctrator.extractSQLDataByColumn(grid);

            List< String> listOfInClauses = groupByColumn.entrySet()
                    .stream()
                    .map(entry -> collect(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());
            String result = String.join("\n", listOfInClauses);

            CopyPasteManager.getInstance().setContents(new StringSelection(result));
        }

    }

    String collect(String columnName, List<SQLExctrator.Cell> cells){
        return columnName +
                IN + "(" +
                cells.stream()
                        .map(cell -> SQLType.apply(cell.getValue(), cell.getType()))
                        .collect(Collectors.joining(", ")) +
                ")";
    }

}
