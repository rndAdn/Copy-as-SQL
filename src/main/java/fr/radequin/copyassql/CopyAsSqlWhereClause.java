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


public class CopyAsSqlWhereClause extends CopyAsSqlAction {



    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        DataGrid grid = DataGridUtil.getDataGrid(e.getDataContext());
        if (grid != null) {
            Map<Integer, List<SQLExctrator.Cell>> map1 = SQLExctrator.extractSQLDataByRow(grid);

           List<String> map2 = map1.values()
                    .stream()
                    .map(this::collect)
                    .collect(Collectors.toList());

            String result = WHERE + String.join(OR + NEW_LINE, map2);

            CopyPasteManager.getInstance().setContents(new StringSelection(result));

        }

    }

    String collect(List<SQLExctrator.Cell> cells){
        return " (" +
                cells.stream()
                        .map(a -> a.getTitle() + EQUALS + SQLType.apply(a.getValue(), a.getType()))
                        .collect(Collectors.joining(AND)) +
                ")";
    }



}
