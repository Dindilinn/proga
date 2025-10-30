// src/airportapp/view/MainWindow.java
package airportapp.view;

import airportapp.model.*;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MainWindow extends JFrame {
    private final Airport airport = new Airport();
    private TariffTableModel tableModel;
    private JTable table;

    public MainWindow() {
        setTitle("Система управления тарифами аэропорта");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmExit();
            }
        });

        tableModel = new TariffTableModel(airport.getTariffs());
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(false); // сортировка — вручную

        JScrollPane scrollPane = new JScrollPane(table);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton addButton = new JButton("Добавить");
        JButton editButton = new JButton("Изменить");
        JButton deleteButton = new JButton("Удалить");
        JButton findMaxButton = new JButton("Найти макс.");
        JButton sortButton = new JButton("Сортировка по итог. цене");
        JButton saveButton = new JButton("Сохранить");
        JButton loadButton = new JButton("Загрузить");

        addButton.addActionListener(this::handleAdd);
        editButton.addActionListener(this::handleEdit);
        deleteButton.addActionListener(this::handleDelete);
        findMaxButton.addActionListener(this::handleFindMax);
        sortButton.addActionListener(this::handleSort);
        saveButton.addActionListener(this::handleSave);
        loadButton.addActionListener(this::handleLoad);

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(findMaxButton);
        buttonPanel.add(sortButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(loadButton);

        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setSize(800, 500);
        setLocationRelativeTo(null);
    }

    private void handleAdd(ActionEvent e) {
        TariffEditDialog dialog = new TariffEditDialog(this, null);
        Tariff result = dialog.showDialog();
        if (result != null) {
            airport.addTariff(result);
            tableModel.addTariff(result);
        }
    }

    private void handleEdit(ActionEvent e) {
        int selected = table.getSelectedRow();
        if (selected == -1) {
            showError("Выберите тариф для изменения.");
            return;
        }
        Tariff selectedTariff = tableModel.getTariffAt(selected);
        TariffEditDialog dialog = new TariffEditDialog(this, selectedTariff);
        Tariff result = dialog.showDialog();
        if (result != null) {
            // Удаляем старый, добавляем новый
            airport.getTariffs().remove(selectedTariff);
            tableModel.removeTariff(selected);

            airport.addTariff(result);
            tableModel.addTariff(result);
        }
    }

    private void handleDelete(ActionEvent e) {
        int selected = table.getSelectedRow();
        if (selected == -1) {
            showError("Выберите тариф для удаления.");
            return;
        }
        Tariff toRemove = tableModel.getTariffAt(selected);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Удалить выбранный тариф?",
                "Подтверждение",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm == JOptionPane.YES_OPTION) {
            airport.removeTariff(toRemove); // ← УДАЛЯЕМ ИЗ РЕАЛЬНОГО СПИСКА
            tableModel.removeTariff(selected);
        }
    }

    private void handleFindMax(ActionEvent e) {
        Tariff max = airport.findMaxPriceTariff();
        if (max == null) {
            JOptionPane.showMessageDialog(this, "Нет добавленных тарифов.", "Информация", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, max.toString(), "Максимальный тариф", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private boolean sortAscending = true;
    private void handleSort(ActionEvent e) {
        List<Tariff> list = airport.getMutableTariffs(); // ← теперь настоящий список
        list.sort((t1, t2) -> {
            return sortAscending
                    ? Double.compare(t1.getPrice(), t2.getPrice())
                    : Double.compare(t2.getPrice(), t1.getPrice());
        });
        sortAscending = !sortAscending;
        tableModel.setTariffs(list); // ← обновляем модель таблицы
    }


    private void handleSave(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Текстовые файлы", "txt"));
        chooser.setSelectedFile(new File("tariffs.txt"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                airport.saveToFile(file.getAbsolutePath());
                JOptionPane.showMessageDialog(this, "Данные успешно сохранены!", "Успех", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                showError("Ошибка записи: " + ex.getMessage());
            }
        }
    }

    private void handleLoad(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Текстовые файлы", "txt"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                airport.loadFromFile(file.getAbsolutePath());
                tableModel.setTariffs(airport.getTariffs());
                JOptionPane.showMessageDialog(this, "Данные успешно загружены!", "Успех", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                showError("Ошибка загрузки: " + ex.getMessage());
            }
        }
    }

    private void confirmExit() {
        int result = JOptionPane.showConfirmDialog(
                this,
                "Сохранить изменения перед выходом?",
                "Выход",
                JOptionPane.YES_NO_CANCEL_OPTION
        );
        if (result == JOptionPane.CANCEL_OPTION) return;
        if (result == JOptionPane.YES_OPTION) {
            handleSave(null);
        }
        System.exit(0);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Ошибка", JOptionPane.ERROR_MESSAGE);
    }

    // --- Внутренний класс модели таблицы ---
    private static class TariffTableModel extends AbstractTableModel {
        private final String[] columns = {"Направление", "Базовая цена", "Итоговая цена"};
        private List<Tariff> tariffs = new ArrayList<>();

        public TariffTableModel(List<Tariff> initial) {
            this.tariffs = new ArrayList<>(initial);
        }

        public void setTariffs(List<Tariff> list) {
            this.tariffs = new ArrayList<>(list);
            fireTableDataChanged();
        }

        public void addTariff(Tariff t) {
            tariffs.add(t);
            fireTableRowsInserted(tariffs.size() - 1, tariffs.size() - 1);
        }

        public void removeTariff(int index) {
            tariffs.remove(index);
            fireTableRowsDeleted(index, index);
        }

        public Tariff getTariffAt(int row) {
            return tariffs.get(row);
        }

        @Override
        public int getRowCount() {
            return tariffs.size();
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
        public Object getValueAt(int rowIndex, int columnIndex) {
            Tariff t = tariffs.get(rowIndex);
            switch (columnIndex) {
                case 0: return t.getDestination();
                case 1: return String.format("%.2f", t.getBasePrice());
                case 2: return String.format("%.2f", t.getPrice());
                default: return "";
            }
        }
    }
}