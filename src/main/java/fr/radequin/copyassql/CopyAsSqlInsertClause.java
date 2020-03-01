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



public class CopyAsSqlInsertClause extends CopyAsSqlAction {



    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

        DataGrid grid = DataGridUtil.getDataGrid(e.getDataContext());
        if (grid != null) {
            Map<Integer, List<SQLExctrator.Cell>> map1 = SQLExctrator.extractSQLDataByRow(grid);

            String result = "INSERT INTO TableName (" + String.join(", ", SQLExctrator.extractSQLColumnName(grid)) + ") \nVALUES" ;
           List<String> map2 = map1.values()
                    .stream()
                    .map(this::collect)
                    .collect(Collectors.toList());

            result += String.join(",\n", map2);

            CopyPasteManager.getInstance().setContents(new StringSelection(result));

        }

    }

    String collect(List<SQLExctrator.Cell> cells){
        return " (" +
                cells.stream()
                        .map(a -> SQLType.apply(a.getValue(), a.getType()))
                        .collect(Collectors.joining(", ")) +
                ")";
    }



}
