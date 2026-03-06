package clipboard.ui;

import clipboard.core.HistoryManager;
import clipboard.model.ClipboardEntry;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class TrayManager {
    private TrayIcon trayIcon;
    private final HistoryManager historyManager;
    private final MainWindow mainWindow;

    public TrayManager(HistoryManager historyManager, MainWindow mainWindow) {
        this.historyManager = historyManager;
        this.mainWindow = mainWindow;
    }

    public void setup() {
        if (!SystemTray.isSupported()) {
            return;
        }

        try {
            SystemTray tray = SystemTray.getSystemTray();

            Image image = createTrayIcon();
            PopupMenu popup = createPopupMenu();

            trayIcon = new TrayIcon(image, "Clipboard Manager", popup);
            trayIcon.setImageAutoSize(true);
            trayIcon.addActionListener(e -> showWindow());

            tray.add(trayIcon);

        } catch (Exception e) {
            System.err.println("Failed to setup tray: " + e.getMessage());
        }
    }

    private Image createTrayIcon() {
        BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();

        g.setColor(new Color(0, 120, 215));
        g.fillRect(0, 0, 16, 16);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 10));
        g.drawString("C", 4, 12);
        g.dispose();

        return image;
    }

    private PopupMenu createPopupMenu() {
        PopupMenu popup = new PopupMenu();

        MenuItem showItem = new MenuItem("Показать окно");
        showItem.addActionListener(e -> showWindow());

        Menu historyMenu = new Menu("Последние записи");
        updateQuickHistoryMenu(historyMenu);

        popup.add(showItem);
        popup.addSeparator();
        popup.add(historyMenu);
        popup.addSeparator();

        MenuItem clearItem = new MenuItem("Очистить историю");
        clearItem.addActionListener(e -> clearHistory());

        popup.add(clearItem);
        popup.addSeparator();

        MenuItem exitItem = new MenuItem("Выход");
        exitItem.addActionListener(e -> exit());

        popup.add(exitItem);

        return popup;
    }

    private void updateQuickHistoryMenu(Menu historyMenu) {
        List<ClipboardEntry> recent = historyManager.getHistory();

        for (int i = 0; i < Math.min(5, recent.size()); i++) {
            ClipboardEntry entry = recent.get(i);
            String preview = entry.getPreview(30);
            MenuItem item = new MenuItem((i + 1) + ". " + preview);
            historyMenu.add(item);
        }

        if (recent.isEmpty()) {
            historyMenu.add(new MenuItem("Нет записей"));
        }
    }

    private void showWindow() {
        mainWindow.setVisible(true);
        mainWindow.toFront();
        mainWindow.requestFocus();
    }

    private void clearHistory() {
        int confirm = JOptionPane.showConfirmDialog(null,
                "Очистить всю историю?", "Подтверждение",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            historyManager.clearHistory();
        }
    }

    private void exit() {
        if (trayIcon != null) {
            SystemTray.getSystemTray().remove(trayIcon);
        }
        mainWindow.dispose();
        System.exit(0);
    }

    public void showNotification(String title, String message) {
        if (trayIcon != null) {
            trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
        }
    }
}