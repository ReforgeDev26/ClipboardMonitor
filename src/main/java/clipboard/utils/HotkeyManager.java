//====== Добавление горячих клавиш ======
package clipboard.utils;

import clipboard.ui.MainWindow;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class HotkeyManager {

    public void registerWindow(MainWindow window) {
        JRootPane rootPane = window.getRootPane();

        // Ctrl+F - фокус на поле поиска
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("ctrl F"), "focusSearch");
        rootPane.getActionMap().put("focusSearch", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                focusSearch(window);
            }
        });

        // F5 - обновление таблицы
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("F5"), "refresh");
        rootPane.getActionMap().put("refresh", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refresh(window);
            }
        });

        // Ctrl+N - новое окно (опционально)
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("ctrl N"), "newWindow");
        rootPane.getActionMap().put("newWindow", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Можно реализовать открытие нового окна
            }
        });

        // Ctrl+D - удалить выбранную запись
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("ctrl D"), "deleteEntry");
        rootPane.getActionMap().put("deleteEntry", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteSelected(window);
            }
        });

        // Ctrl+Shift+F - показать только избранное
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("ctrl shift F"), "showFavorites");
        rootPane.getActionMap().put("showFavorites", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showFavorites(window);
            }
        });

        // Escape - очистить поиск
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("ESCAPE"), "clearSearch");
        rootPane.getActionMap().put("clearSearch", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearSearch(window);
            }
        });
    }

    /**
     * Устанавливает фокус на поле поиска и выделяет весь текст
     */
    private void focusSearch(MainWindow window) {
        SwingUtilities.invokeLater(() -> {
            JTextField searchField = window.getSearchField();
            if (searchField != null) {
                searchField.requestFocusInWindow();
                searchField.selectAll();
            }
        });
    }

    /**
     * Обновляет таблицу с историей
     */
    private void refresh(MainWindow window) {
        SwingUtilities.invokeLater(() -> {
            window.refreshTable();
            // Показываем уведомление в статусной строке
            window.updateStatus("Таблица обновлена");

            // Мигаем строкой статуса для визуального подтверждения
            Timer timer = new Timer(100, null);
            timer.addActionListener(new AbstractAction() {
                int count = 0;
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (count < 3) {
                        window.blinkStatus();
                        count++;
                    } else {
                        timer.stop();
                    }
                }
            });
            timer.start();
        });
    }

    /**
     * Удаляет выбранную запись
     */
    private void deleteSelected(MainWindow window) {
        SwingUtilities.invokeLater(() -> {
            window.deleteSelectedEntry();
        });
    }

    /**
     * Показывает только избранные записи
     */
    private void showFavorites(MainWindow window) {
        SwingUtilities.invokeLater(() -> {
            window.showOnlyFavorites();
        });
    }

    /**
     * Очищает поле поиска
     */
    private void clearSearch(MainWindow window) {
        SwingUtilities.invokeLater(() -> {
            window.clearSearchField();
        });
    }
}