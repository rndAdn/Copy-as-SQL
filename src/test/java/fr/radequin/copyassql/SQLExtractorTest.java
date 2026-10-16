package fr.radequin.copyassql;

import com.intellij.database.datagrid.DataGrid;
import com.intellij.database.datagrid.GridColumn;
import com.intellij.database.datagrid.GridRow;
import com.intellij.database.datagrid.ModelIndex;
import com.intellij.database.datagrid.ModelIndexSet;
import com.intellij.database.datagrid.SelectionModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SQLExtractorTest {

    @Mock
    private DataGrid dataGrid;

    @Mock
    private SelectionModel<GridRow, GridColumn> selectionModel;

    @Mock
    private ModelIndexSet<GridColumn> columns;

    @Mock
    private ModelIndexSet<GridRow> rows;

    @Mock
    private ModelIndex<GridColumn> columnIndex;

    @BeforeEach
    void setup() {
        when(dataGrid.getSelectionModel()).thenReturn(selectionModel);
        when(selectionModel.getSelectedColumns()).thenReturn(columns);
        when(selectionModel.getSelectedRows()).thenReturn(rows);
    }

    @Test
    void rowExtractionRejectsSelectionWithoutColumns() {
        when(columns.asList()).thenReturn(List.of());

        assertThrows(IllegalArgumentException.class, () -> SQLExtractor.extractSQLDataByRow(dataGrid));
        verifyNoInteractions(rows);
    }

    @Test
    void columnExtractionRejectsSelectionWithoutRows() {
        when(columns.asList()).thenReturn(List.of(columnIndex));
        when(rows.asList()).thenReturn(List.of());

        assertThrows(IllegalArgumentException.class, () -> SQLExtractor.extractSQLDataByColumn(dataGrid));
    }
}
