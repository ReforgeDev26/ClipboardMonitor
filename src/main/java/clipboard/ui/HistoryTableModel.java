package clipboard.ui;

import clipboard.core.EncryptionManager;
import clipboard.core.HistoryManager;
import clipboard.model.ClipboardEntry;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class HistoryTableModel extends AbstractTableModel {
    private final String[] columns = {"Дата", "Содержимое", "Тип", "★"};
    private final EncryptionManager encryptionManager;
    private List<ClipboardEntry> data = new ArrayList<>();

    public HistoryTableModel(HistoryManager historyManager, EncryptionManager encryptionManager) {
        this.encryptionManager = encryptionManager;
        this.data = historyManager.getHistory();
    }

    public void updateData(List<ClipboardEntry> newData) {
        this.data = newData;
        fireTableDataChanged();
    }

    public ClipboardEntry getEntryAt(int row) {
        return data.get(row);
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return String.class;
    }

    @Override
    public Object getValueAt(int row, int column) {
        ClipboardEntry entry = data.get(row);

        switch (column) {
            case 0: return entry.getFormattedDate();
            case 1:
                String content = encryptionManager.decrypt(entry.getContent());
                return entry.getPreview(100);
            case 2:
                return entry.getType().getDisplayName();
            case 3:
                return entry.isFavorite() ? "★" : "☆";
            default: return "";
        }
    }
}