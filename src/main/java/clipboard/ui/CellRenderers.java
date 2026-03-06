package clipboard.ui;

import clipboard.core.EncryptionManager;
import clipboard.model.ClipboardEntry;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class CellRenderers {

    public static class DateRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {

            Component c = super.getTableCellRendererComponent(table, value,
                    isSelected, hasFocus, row, column);

            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 248, 248));
            }

            setHorizontalAlignment(JLabel.CENTER);
            return c;
        }
    }

    public static class ContentRenderer extends DefaultTableCellRenderer {
        private final EncryptionManager encryptionManager;

        public ContentRenderer(EncryptionManager encryptionManager) {
            this.encryptionManager = encryptionManager;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {

            Component c = super.getTableCellRendererComponent(table, value,
                    isSelected, hasFocus, row, column);

            if (!isSelected) {
                HistoryTableModel model = (HistoryTableModel) table.getModel();
                ClipboardEntry entry = model.getEntryAt(row);

                if (entry.isFavorite()) {
                    c.setBackground(new Color(255, 255, 200));
                } else {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 248, 248));
                }
            }

            return c;
        }
    }

    public static class TypeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {

            Component c = super.getTableCellRendererComponent(table, value,
                    isSelected, hasFocus, row, column);

            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 248, 248));
            }

            HistoryTableModel model = (HistoryTableModel) table.getModel();
            ClipboardEntry entry = model.getEntryAt(row);
            setText(entry.getType().getIcon() + " " + value);

            return c;
        }
    }
}