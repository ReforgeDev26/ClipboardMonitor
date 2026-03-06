//====== Основной класс ======
package clipboard;

import clipboard.core.*;
import clipboard.model.ClipboardEntry;
import clipboard.ui.*;
import clipboard.utils.HotkeyManager;

import javax.swing.*;

public class ClipboardManager {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Игнорируем
        }

        SwingUtilities.invokeLater(ClipboardManager::createAndShowGUI);
    }
    // Создание GUI интерфейса
    private static void createAndShowGUI() {
        EncryptionManager encryptionManager = new EncryptionManager();
        HistoryManager historyManager = new HistoryManager(encryptionManager);
        HotkeyManager hotkeyManager = new HotkeyManager();

        ClipboardMonitor monitor = new ClipboardMonitor(historyManager);

        MainWindow mainWindow = new MainWindow(
                historyManager,
                encryptionManager,
                hotkeyManager
        );

        TrayManager trayManager = new TrayManager(historyManager, mainWindow);
        trayManager.setup();

        monitor.setListener(new ClipboardMonitor.MonitorListener() {
            // Появление новой записи
            @Override
            public void onNewContent(ClipboardEntry entry) {
                if (!mainWindow.isVisible()) {
                    trayManager.showNotification(
                            "Новое в буфере",
                            entry.getPreview(50)
                    );
                }
            }
            // Отчет об ошибке
            @Override
            public void onError(String error) {
                System.err.println("Monitor error: " + error);
            }
        });

        mainWindow.setVisible(true);
        // Закрытие и выключение программы
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down Clipboard Manager...");
        }));
    }
}