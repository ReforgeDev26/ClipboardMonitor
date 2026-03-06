//======= Хранение истории буфера ========
package clipboard.core;

import clipboard.model.ClipboardEntry;
import clipboard.model.ClipboardType;

import java.io.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class HistoryManager {
    private static final String HISTORY_FILE = "clipboard_history.dat";
    private static final int DEFAULT_MAX_SIZE = 100;

    private final List<ClipboardEntry> history;
    private final EncryptionManager encryptionManager;
    private int maxSize = DEFAULT_MAX_SIZE;
    private List<HistoryListener> listeners = new ArrayList<>();

    public interface HistoryListener {
        void onHistoryChanged();
    }

    public HistoryManager(EncryptionManager encryptionManager) {
        this.history = new CopyOnWriteArrayList<>();
        this.encryptionManager = encryptionManager;
        loadHistory();
    }

    public void addListener(HistoryListener listener) {
        listeners.add(listener);
    }
    // Добавить запись
    public void addEntry(ClipboardEntry entry) {
        Optional<ClipboardEntry> existing = history.stream()
                .filter(e -> e.getContent().equals(entry.getContent()))
                .findFirst();

        if (existing.isPresent()) {
            history.remove(existing.get());
            history.add(0, existing.get());
            existing.get().incrementUseCount();
        } else {
            history.add(0, entry);
        }

        while (history.size() > maxSize) {
            history.remove(history.size() - 1);
        }

        saveHistory();
        notifyListeners();
    }
    // Получить историю
    public List<ClipboardEntry> getHistory() {
        return Collections.unmodifiableList(history);
    }
    // Поиск
    public List<ClipboardEntry> search(String query) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>(history);
        }

        String lowerQuery = query.toLowerCase().trim();

        return history.stream()
                .filter(entry -> {
                    String content = encryptionManager.decrypt(entry.getContent()).toLowerCase();
                    return content.contains(lowerQuery) ||
                            entry.getType().toString().toLowerCase().contains(lowerQuery) ||
                            (entry.isFavorite() && "избранное".contains(lowerQuery));
                })
                .collect(Collectors.toList());
    }
    // Список избранных записей
    public List<ClipboardEntry> getFavorites() {
        return history.stream()
                .filter(ClipboardEntry::isFavorite)
                .collect(Collectors.toList());
    }
    // Получить тип записи
    public List<ClipboardEntry> getByType(ClipboardType type) {
        return history.stream()
                .filter(entry -> entry.getType() == type)
                .collect(Collectors.toList());
    }
    // Удалить запись
    public boolean removeEntry(ClipboardEntry entry) {
        boolean removed = history.remove(entry);
        if (removed) {
            saveHistory();
            notifyListeners();
        }
        return removed;
    }
    // Очистка истории
    public void clearHistory() {
        history.clear();
        saveHistory();
        notifyListeners();
    }
    // Максимальный размер истории
    public int getMaxSize() {
        return maxSize;
    }
    // Задать максимальный размер истории
    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
        while (history.size() > maxSize) {
            history.remove(history.size() - 1);
        }
        saveHistory();
        notifyListeners();
    }
    // Загрузка истории
    @SuppressWarnings("unchecked")
    private void loadHistory() {
        File file = new File(HISTORY_FILE);
        if (!file.exists()) return;

        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(file))) {

            List<ClipboardEntry> loaded = (List<ClipboardEntry>) ois.readObject();
            history.addAll(loaded);

        } catch (Exception e) {
            System.err.println("Ошибка загрузки истории: " + e.getMessage());
        }
    }
    // Сохранение истории
    private void saveHistory() {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(HISTORY_FILE))) {

            oos.writeObject(new ArrayList<>(history));

        } catch (Exception e) {
            System.err.println("Ошибка сохранения истории: " + e.getMessage());
        }
    }
    // Уведомления об изменении истории
    private void notifyListeners() {
        listeners.forEach(HistoryListener::onHistoryChanged);
    }
}