//======= Проверка и отслеживание буфера обмена ========

package clipboard.core;

import clipboard.model.ClipboardEntry;
import clipboard.model.ClipboardType;
import clipboard.utils.ContentDetector;

import java.awt.*;
import java.awt.datatransfer.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Timer;
import java.util.TimerTask;

public class ClipboardMonitor implements ClipboardOwner {
    private static final int CHECK_INTERVAL_MS = 1000;

    private final Clipboard systemClipboard;
    private final HistoryManager historyManager;
    private final ContentDetector contentDetector;
    private String lastContent = "";
    private MonitorListener listener;

    public interface MonitorListener {
        void onNewContent(ClipboardEntry entry);
        void onError(String error);
    }

    public ClipboardMonitor(HistoryManager historyManager) {
        this.systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        this.historyManager = historyManager;
        this.contentDetector = new ContentDetector();
        startMonitoring();
    }

    public void setListener(MonitorListener listener) {
        this.listener = listener;
    }
    // Начало мониторинга
    private void startMonitoring() {
        Timer timer = new Timer("ClipboardMonitor", true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkClipboard();
            }
        }, 0, CHECK_INTERVAL_MS);
    }
    // Проверка буфера
    private void checkClipboard() {
        try {
            Transferable contents = systemClipboard.getContents(this);

            if (contents != null && contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                processTextContent(contents);
            }

        } catch (Exception e) {
            if (listener != null) {
                listener.onError("Ошибка доступа к буферу: " + e.getMessage());
            }
        }
    }
    // Извлечение текста
    private void processTextContent(Transferable contents)
            throws UnsupportedFlavorException, IOException {

        String currentContent = (String) contents.getTransferData(DataFlavor.stringFlavor);

        if (!currentContent.equals(lastContent) && !currentContent.trim().isEmpty()) {
            lastContent = currentContent;

            ClipboardType type = contentDetector.detectType(currentContent);
            ClipboardEntry entry = new ClipboardEntry(
                    currentContent,
                    LocalDateTime.now(),
                    type
            );

            historyManager.addEntry(entry);

            if (listener != null) {
                listener.onNewContent(entry);
            }
        }
    }
    // Копирование текста в буфер
    public void copyToClipboard(String text) {
        StringSelection selection = new StringSelection(text);
        systemClipboard.setContents(selection, this);
    }

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        // не используется для базового функционала
    }
}