// src/airportapp/view/TariffEditDialog.java
package airportapp.view;

import airportapp.model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TariffEditDialog extends JDialog {
    private Tariff result = null;

    private final JTextField destinationField = new JTextField(20);
    private final JTextField basePriceField = new JTextField(10);
    private final JCheckBox discountCheckBox = new JCheckBox("Со скидкой");
    private final JTextField discountField = new JTextField(10);

    // Конструктор 1: создание нового тарифа
    public TariffEditDialog(Frame owner, Tariff existing) {
        super(owner, existing == null ? "Добавить тариф" : "Изменить тариф", true);
        initialize(existing);
    }

    private void initialize(Tariff existing) {
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Направление:"), gbc);
        gbc.gridx = 1;
        formPanel.add(destinationField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Базовая цена:"), gbc);
        gbc.gridx = 1;
        formPanel.add(basePriceField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(discountCheckBox, gbc);
        gbc.gridx = 1;
        formPanel.add(discountField, gbc);
        discountField.setEnabled(false);

        discountCheckBox.addActionListener(e -> discountField.setEnabled(discountCheckBox.isSelected()));

        if (existing != null) {
            destinationField.setText(existing.getDestination());
            basePriceField.setText(String.valueOf(existing.getBasePrice()));
            double discount = existing.getBasePrice() - existing.getPrice();
            if (discount > 0) {
                discountCheckBox.setSelected(true);
                discountField.setText(String.valueOf(discount));
                discountField.setEnabled(true);
            }
        }

        JPanel buttonPanel = new JPanel();
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Отмена");

        okButton.addActionListener(this::validateAndClose);
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(getOwner());
    }

    private void validateAndClose(ActionEvent e) {
        try {
            String dest = validateDestination(destinationField.getText());
            double base = validatePrice(basePriceField.getText(), "базовая стоимость");

            DiscountStrategy strategy = new NoDiscount();
            if (discountCheckBox.isSelected()) {
                double disc = validatePrice(discountField.getText(), "скидка");
                strategy = new FixedDiscount(disc);
            }

            result = new Tariff(dest, base, strategy);
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Ошибка ввода", JOptionPane.ERROR_MESSAGE);
        }
    }

    public Tariff showDialog() {
        setVisible(true);
        return result;
    }

    // --- Валидация (аналогично консольной версии) ---
    private String validateDestination(String input) throws InvalidTariffException {
        input = input.trim();
        if (input.isEmpty()) throw new InvalidTariffException("Направление не может быть пустым.");
        if (input.matches("^[0-9\\p{Punct}]+$")) throw new InvalidTariffException("Должно содержать хотя бы одну букву.");
        if (input.length() < 2) throw new InvalidTariffException("Слишком короткое (минимум 2 символа).");
        if (input.length() > 50) throw new InvalidTariffException("Слишком длинное (максимум 50 символов).");
        if (!input.matches("^[\\p{L}\\s\\-'’]+$")) throw new InvalidTariffException("Только буквы, пробелы, дефисы, апострофы.");
        return input;
    }

    private double validatePrice(String input, String field) throws InvalidTariffException {
        if (input == null || input.trim().isEmpty()) {
            throw new InvalidTariffException("Поле '" + field + "' не может быть пустым.");
        }
        try {
            double v = Double.parseDouble(input.trim());
            if (Double.isNaN(v) || Double.isInfinite(v)) throw new InvalidTariffException("Некорректное число.");
            if (Math.abs(v) > 1e9) throw new InvalidTariffException("Слишком большое число.");
            if (v < 0) throw new InvalidTariffException(field + " не может быть отрицательной.");
            return v;
        } catch (NumberFormatException ex) {
            throw new InvalidTariffException("Некорректный формат числа.");
        }
    }
}