package fr.radequin.copyassql;

import com.intellij.database.datagrid.DataGrid;
import com.intellij.database.datagrid.DataGridUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.ui.Messages;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class SqlCancellationTest {

    @Test
    void cancellingInsertLeavesClipboardUntouched() {
        assertCancellation(new CopyAsSqlInsertClause());
    }

    @Test
    void cancellingDeleteLeavesClipboardUntouched() {
        assertCancellation(new CopyAsSqlDeleteClause());
    }

    @Test
    void cancellingUpdateTableLeavesClipboardUntouched() {
        assertUpdateCancellation(null, 0);
    }

    @Test
    void cancellingUpdateValueLeavesClipboardUntouched() {
        assertUpdateCancellation("people", 1);
    }

    private void assertUpdateCancellation(String tableName, int valuePromptCount) {
        AnActionEvent event = mock(AnActionEvent.class);
        DataContext context = mock(DataContext.class);
        DataGrid grid = mock(DataGrid.class);
        when(event.getDataContext()).thenReturn(context);

        try (MockedStatic<DataGridUtil> grids = mockStatic(DataGridUtil.class);
             MockedStatic<SQLExtractor> extraction = mockStatic(SQLExtractor.class);
             MockedStatic<Messages> dialogs = mockStatic(Messages.class);
             MockedStatic<CopyPasteManager> clipboard = mockStatic(CopyPasteManager.class)) {
            grids.when(() -> DataGridUtil.getDataGrid(context)).thenReturn(grid);
            extraction.when(() -> SQLExtractor.extractSQLDataByRow(grid))
                    .thenReturn(Map.of(0, List.of(new SQLExtractor.Cell("'Alice'", "name", 0))));
            dialogs.when(() -> Messages.showInputDialog(
                    anyString(), anyString(), isNull(), eq("TableName"), isNull())).thenReturn(tableName);
            dialogs.when(() -> Messages.showInputDialog(anyString(), anyString(), isNull())).thenReturn(null);

            new CopyAsSqlUpdateClause().actionPerformed(event);

            dialogs.verify(() -> Messages.showInputDialog(
                    anyString(), anyString(), isNull(), eq("TableName"), isNull()));
            dialogs.verify(() -> Messages.showInputDialog(
                    anyString(), anyString(), isNull()), times(valuePromptCount));
            clipboard.verifyNoInteractions();
        }
    }

    private void assertCancellation(CopyAsSqlAction action) {
        AnActionEvent event = mock(AnActionEvent.class);
        DataContext context = mock(DataContext.class);
        DataGrid grid = mock(DataGrid.class);
        when(event.getDataContext()).thenReturn(context);

        try (MockedStatic<DataGridUtil> grids = mockStatic(DataGridUtil.class);
             MockedStatic<Messages> dialogs = mockStatic(Messages.class);
             MockedStatic<CopyPasteManager> clipboard = mockStatic(CopyPasteManager.class)) {
            grids.when(() -> DataGridUtil.getDataGrid(context)).thenReturn(grid);
            dialogs.when(() -> Messages.showInputDialog(
                    anyString(), anyString(), isNull(), eq("TableName"), isNull())).thenReturn(null);

            action.actionPerformed(event);

            dialogs.verify(() -> Messages.showInputDialog(
                    anyString(), anyString(), isNull(), eq("TableName"), isNull()));
            clipboard.verifyNoInteractions();
            verifyNoInteractions(grid);
        }
    }
}
