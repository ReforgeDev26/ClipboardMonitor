package clipboard.ui;

import clipboard.core.*;
import clipboard.model.ClipboardEntry;
import clipboard.model.ClipboardType;
import clipboard.utils.ContentDetector;
import clipboard.utils.HotkeyManager;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MainWindow extends JFrame {
    private final HistoryManager historyManager;
    private final EncryptionManager encryptionManager;
    private final HotkeyManager hotkeyManager;
    private final ContentDetector contentDetector;

    private JTable historyTable;
    private HistoryTableModel tableModel;
    private JTextField searchField;
    private JLabel statusLabel;
    private JCheckBoxMenuItem encryptionMenuItem;

    public MainWindow(HistoryManager historyManager,
                      EncryptionManager encryptionManager,
                      HotkeyManager hotkeyManager) {
        this.historyManager = historyManager;
        this.encryptionManager = encryptionManager;
        this.hotkeyManager = hotkeyManager;
        this.contentDetector = new ContentDetector();

        initializeWindow();
        createMenu();
        createComponents();
        setupListeners();
        updateStatus("Готов");
    }

    private void initializeWindow() {
        setTitle("Clipboard Manager");
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
    }

    private void createMenu() {
        JMenuBar menuBar = new JMenuBar();

        // Файл меню
        JMenu fileMenu = new JMenu("Файл");

        JMenuItem exportItem = new JMenuItem("Экспорт в TXT");
        exportItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_DOWN_MASK));
        exportItem.addActionListener(e -> exportToTxt());

        JMenuItem importItem = new JMenuItem("Импорт из TXT");
        importItem.addActionListener(e -> importFromTxt());

        fileMenu.add(exportItem);
        fileMenu.add(importItem);
        fileMenu.addSeparator();

        JMenuItem clearItem = new JMenuItem("Очистить историю");
        clearItem.addActionListener(e -> clearHistory());
        fileMenu.add(clearItem);
        fileMenu.addSeparator();

        JMenuItem exitItem = new JMenuItem("Выход");
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, KeyEvent.ALT_DOWN_MASK));
        exitItem.addActionListener(e -> exit());
        fileMenu.add(exitItem);

        // Настройки меню
        JMenu settingsMenu = new JMenu("Настройки");

        encryptionMenuItem = new JCheckBoxMenuItem("Шифрование");
        encryptionMenuItem.setSelected(encryptionManager.isEnabled());
        encryptionMenuItem.addActionListener(e -> toggleEncryption());

        JMenuItem maxHistoryItem = new JMenuItem("Максимум записей");
        maxHistoryItem.addActionListener(e -> showMaxHistoryDialog());

        settingsMenu.add(encryptionMenuItem);
        settingsMenu.add(maxHistoryItem);

        // Вид меню
        JMenu viewMenu = new JMenu("Вид");

        JCheckBoxMenuItem alwaysOnTop = new JCheckBoxMenuItem("Поверх всех окон");
        alwaysOnTop.addActionListener(e -> setAlwaysOnTop(alwaysOnTop.isSelected()));

        viewMenu.add(alwaysOnTop);

        menuBar.add(fileMenu);
        menuBar.add(settingsMenu);
        menuBar.add(viewMenu);

        setJMenuBar(menuBar);
    }

    private void createComponents() {
        setLayout(new BorderLayout());

        JPanel searchPanel = createSearchPanel();
        add(searchPanel, BorderLayout.NORTH);

        tableModel = new HistoryTableModel(historyManager, encryptionManager);
        historyTable = new JTable(tableModel);
        setupTable();

        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(scrollPane, BorderLayout.CENTER);

        statusLabel = new JLabel(" ");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
        add(statusLabel, BorderLayout.SOUTH);

        setupPopupMenu();
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        searchField = new JTextField();
        searchField.setToolTipText("Поиск по содержимому (Ctrl+F)");

        JButton clearButton = new JButton("✕");
        clearButton.setPreferredSize(new Dimension(30, 25));
        clearButton.setToolTipText("Очистить поиск");
        clearButton.addActionListener(e -> {
            searchField.setText("");
            filterHistory();
        });

        JPanel searchWrapper = new JPanel(new BorderLayout(5, 0));
        searchWrapper.add(searchField, BorderLayout.CENTER);
        searchWrapper.add(clearButton, BorderLayout.EAST);

        panel.add(new JLabel("🔍 "), BorderLayout.WEST);
        panel.add(searchWrapper, BorderLayout.CENTER);

        return panel;
    }

    private void setupTable() {
        historyTable.setRowHeight(30);
        historyTable.setShowGrid(true);
        historyTable.setGridColor(new Color(230, 230, 230));
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyTable.setAutoCreateRowSorter(true);

        historyTable.getColumnModel().getColumn(0).setPreferredWidth(140);
        historyTable.getColumnModel().getColumn(1).setPreferredWidth(450);
        historyTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        historyTable.getColumnModel().getColumn(3).setPreferredWidth(50);

        historyTable.getColumnModel().getColumn(0).setCellRenderer(new CellRenderers.DateRenderer());
        historyTable.getColumnModel().getColumn(1).setCellRenderer(new CellRenderers.ContentRenderer(encryptionManager));
        historyTable.getColumnModel().getColumn(2).setCellRenderer(new CellRenderers.TypeRenderer());

        historyTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    pasteSelected();
                }
            }
        });
    }

    private void setupPopupMenu() {
        JPopupMenu popup = new JPopupMenu();

        JMenuItem copyItem = new JMenuItem("Копировать");
        copyItem.addActionListener(e -> copySelected());

        JMenuItem pasteItem = new JMenuItem("Вставить");
        pasteItem.addActionListener(e -> pasteSelected());

        popup.add(copyItem);
        popup.add(pasteItem);
        popup.addSeparator();

        JMenuItem favoriteItem = new JMenuItem("В избранное");
        favoriteItem.addActionListener(e -> toggleFavorite());

        JMenuItem deleteItem = new JMenuItem("Удалить");
        deleteItem.addActionListener(e -> deleteSelected());

        popup.add(favoriteItem);
        popup.add(deleteItem);
        popup.addSeparator();

        JMenuItem previewItem = new JMenuItem("Предпросмотр");
        previewItem.addActionListener(e -> showPreview());

        popup.add(previewItem);

        historyTable.setComponentPopupMenu(popup);
    }

    private void setupListeners() {
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { filterHistory(); }
            public void removeUpdate(DocumentEvent e) { filterHistory(); }
            public void insertUpdate(DocumentEvent e) { filterHistory(); }
        });

        hotkeyManager.registerWindow(this);

        historyManager.addListener(() -> {
            SwingUtilities.invokeLater(() -> {
                filterHistory();
                updateStatus("Обновлено");
            });
        });
    }

    private void filterHistory() {
        String query = searchField.getText();
        List<ClipboardEntry> filtered = historyManager.search(query);
        tableModel.updateData(filtered);
        updateStatus("Найдено записей: " + filtered.size());
    }

    // ==================== ЭКСПОРТ/ИМПОРТ ====================

    private void exportToTxt() {
        JFileChooser chooser = new JFileChooser();
        String fileName = "clipboard_history_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".txt";
        chooser.setSelectedFile(new File(fileName));

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();

            try (PrintWriter writer = new PrintWriter(file, "UTF-8")) {
                List<ClipboardEntry> entries = historyManager.getHistory();

                writer.println("==========================================");
                writer.println("        ИСТОРИЯ БУФЕРА ОБМЕНА");
                writer.println("==========================================");
                writer.printf("Дата экспорта: %s%n",
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")));
                writer.printf("Всего записей: %d%n", entries.size());
                writer.println("==========================================");
                writer.println();

                for (int i = 0; i < entries.size(); i++) {
                    ClipboardEntry entry = entries.get(i);
                    String content = encryptionManager.decrypt(entry.getContent());

                    writer.printf("ЗАПИСЬ #%d%n", i + 1);
                    writer.printf("Дата: %s%n", entry.getFormattedDate());
                    writer.printf("Тип: %s %s%n",
                            entry.getType().getIcon(),
                            entry.getType().getDisplayName());
                    writer.printf("Избранное: %s%n", entry.isFavorite() ? "Да ★" : "Нет ☆");
                    writer.printf("Использований: %d%n", entry.getUseCount());
                    writer.printf("Содержимое:%n%s%n", content);
                    writer.println("------------------------------------------");
                    writer.println();
                }

                writer.flush();

                JOptionPane.showMessageDialog(this,
                        String.format("История успешно экспортирована!\nФайл: %s\nЗаписей: %d",
                                file.getName(), entries.size()),
                        "Экспорт завершен",
                        JOptionPane.INFORMATION_MESSAGE);

                updateStatus("Экспортировано: " + entries.size() + " записей");

                try {
                    Desktop.getDesktop().open(file.getParentFile());
                } catch (Exception ex) {
                    // Игнорируем
                }

            } catch (IOException ex) {
                showError("Ошибка при экспорте: " + ex.getMessage());
            }
        }
    }

    private void importFromTxt() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Импорт из TXT");
        chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".txt");
            }

            @Override
            public String getDescription() {
                return "Текстовые файлы (*.txt)";
            }
        });

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Импорт добавит записи из файла в текущую историю.\nПродолжить?",
                    "Подтверждение импорта",
                    JOptionPane.YES_NO_OPTION);

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                StringBuilder content = new StringBuilder();
                boolean readingContent = false;
                int importedCount = 0;

                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("Содержимое:")) {
                        content = new StringBuilder();
                        readingContent = true;
                    } else if (line.startsWith("------------------------------------------")) {
                        if (readingContent && content.length() > 0) {
                            ClipboardType type = contentDetector.detectType(content.toString().trim());
                            ClipboardEntry entry = new ClipboardEntry(
                                    content.toString().trim(),
                                    LocalDateTime.now().minusSeconds(importedCount),
                                    type
                            );
                            historyManager.addEntry(entry);
                            importedCount++;
                            readingContent = false;
                        }
                    } else if (readingContent) {
                        content.append(line).append("\n");
                    }
                }

                JOptionPane.showMessageDialog(this,
                        String.format("Импортировано записей: %d", importedCount),
                        "Импорт завершен",
                        JOptionPane.INFORMATION_MESSAGE);

                updateStatus("Импортировано: " + importedCount + " записей");
                filterHistory();

            } catch (IOException ex) {
                showError("Ошибка при импорте: " + ex.getMessage());
            }
        }
    }

    // ==================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ====================

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Ошибка", JOptionPane.ERROR_MESSAGE);
        updateStatus("Ошибка: " + message);
    }

    // ==================== МЕТОДЫ ДЛЯ HOTKEYMANAGER ====================

    public JTextField getSearchField() {
        return searchField;
    }

    public void refreshTable() {
        filterHistory();
    }

    public void updateStatus(String message) {
        statusLabel.setText(" " + message);
    }

    public void blinkStatus() {
        Color original = statusLabel.getBackground();
        statusLabel.setBackground(new Color(200, 230, 255));

        Timer timer = new Timer(100, null);
        timer.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                statusLabel.setBackground(original);
                timer.stop();
            }
        });
        timer.setRepeats(false);
        timer.start();
    }

    public void deleteSelectedEntry() {
        int row = historyTable.getSelectedRow();
        if (row >= 0) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Удалить выбранную запись?", "Подтверждение",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                int modelRow = historyTable.convertRowIndexToModel(row);
                ClipboardEntry entry = tableModel.getEntryAt(modelRow);
                historyManager.removeEntry(entry);
                updateStatus("Запись удалена");
            }
        } else {
            updateStatus("Ничего не выбрано");
        }
    }

    public void showOnlyFavorites() {
        searchField.setText("");
        List<ClipboardEntry> favorites = historyManager.getFavorites();
        tableModel.updateData(favorites);
        updateStatus("Избранное: " + favorites.size() + " записей");
    }

    public void clearSearchField() {
        searchField.setText("");
        filterHistory();
        updateStatus("Поиск очищен");
    }

    // ==================== МЕТОДЫ ДЛЯ РАБОТЫ С ЗАПИСЯМИ ====================

    private void copySelected() {
        int row = historyTable.getSelectedRow();
        if (row >= 0) {
            int modelRow = historyTable.convertRowIndexToModel(row);
            ClipboardEntry entry = tableModel.getEntryAt(modelRow);
            String content = encryptionManager.decrypt(entry.getContent());

            Toolkit.getDefaultToolkit()
                    .getSystemClipboard()
                    .setContents(new StringSelection(content), null);

            updateStatus("Скопировано");
        }
    }

    private void pasteSelected() {
        copySelected();

        try {
            Robot robot = new Robot();
            robot.keyPress(KeyEvent.VK_CONTROL);
            robot.keyPress(KeyEvent.VK_V);
            robot.keyRelease(KeyEvent.VK_V);
            robot.keyRelease(KeyEvent.VK_CONTROL);
        } catch (AWTException e) {
            updateStatus("Ошибка вставки");
        }
    }

    private void toggleFavorite() {
        int row = historyTable.getSelectedRow();
        if (row >= 0) {
            int modelRow = historyTable.convertRowIndexToModel(row);
            ClipboardEntry entry = tableModel.getEntryAt(modelRow);
            entry.setFavorite(!entry.isFavorite());
            tableModel.fireTableRowsUpdated(row, row);
            updateStatus(entry.isFavorite() ? "Добавлено в избранное" : "Удалено из избранного");
        }
    }

    private void deleteSelected() {
        int row = historyTable.getSelectedRow();
        if (row >= 0) {
            int modelRow = historyTable.convertRowIndexToModel(row);
            ClipboardEntry entry = tableModel.getEntryAt(modelRow);
            historyManager.removeEntry(entry);
            updateStatus("Запись удалена");
        }
    }

    private void showPreview() {
        int row = historyTable.getSelectedRow();
        if (row >= 0) {
            int modelRow = historyTable.convertRowIndexToModel(row);
            ClipboardEntry entry = tableModel.getEntryAt(modelRow);
            String content = encryptionManager.decrypt(entry.getContent());

            JTextArea textArea = new JTextArea(content);
            textArea.setEditable(false);
            textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(500, 400));

            JOptionPane.showMessageDialog(this, scrollPane,
                    "Предпросмотр - " + entry.getFormattedDate(),
                    JOptionPane.PLAIN_MESSAGE);
        }
    }

    private void clearHistory() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Очистить всю историю?", "Подтверждение",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            historyManager.clearHistory();
            updateStatus("История очищена");
        }
    }

    private void showMaxHistoryDialog() {
        String input = JOptionPane.showInputDialog(this,
                "Максимальное количество записей:",
                historyManager.getMaxSize());

        try {
            int max = Integer.parseInt(input);
            historyManager.setMaxSize(max);
            updateStatus("Максимум записей: " + max);
        } catch (NumberFormatException e) {
            // Игнорируем
        }
    }

    private void toggleEncryption() {
        if (encryptionMenuItem.isSelected()) {
            encryptionManager.enable();
        } else {
            encryptionManager.disable();
        }

        filterHistory();
        updateStatus("Шифрование " +
                (encryptionManager.isEnabled() ? "включено" : "отключено"));
    }

    private void exit() {
        dispose();
    }
}