// src/airportapp/model/Airport.java
package airportapp.model;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Airport {
    private final List<Tariff> tariffs = new ArrayList<>();

    public void addTariff(Tariff tariff) {
        tariffs.add(tariff);
    }

    // НОВЫЙ МЕТОД
    public boolean removeTariff(Tariff tariff) {
        return tariffs.remove(tariff);
    }

    // Возвращает копию (для безопасности)
    public List<Tariff> getTariffs() {
        return new ArrayList<>(tariffs);
    }

    // Возвращает внутренний список (только для GUI-слоя!)
    public List<Tariff> getMutableTariffs() {
        return tariffs; // прямой доступ — осторожно!
    }
    public Tariff findMaxPriceTariff() {
        if (tariffs.isEmpty()) return null;
        return tariffs.stream()
                .max((t1, t2) -> Double.compare(t1.getPrice(), t2.getPrice()))
                .orElse(null);
    }

    public void saveToFile(String filename) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filename))) {
            for (Tariff t : tariffs) {
                double discount = t.getBasePrice() - t.getPrice();
                String line = String.format("%s|%f|%f", t.getDestination(), t.getBasePrice(), discount);
                writer.write(line);
                writer.newLine();
            }
        }
    }

    public void loadFromFile(String filename) throws IOException, InvalidTariffException {
        tariffs.clear();
        List<String> lines = Files.readAllLines(Paths.get(filename));
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;
            String[] parts = line.split("\\|");
            if (parts.length != 3) {
                throw new InvalidTariffException("Некорректный формат строки: " + line);
            }
            String destination = parts[0];

            // Заменяем запятую на точку для парсинга
            String basePriceStr = parts[1].replace(',', '.');
            String discountStr = parts[2].replace(',', '.');

            double basePrice = Double.parseDouble(basePriceStr);
            double discount = Double.parseDouble(discountStr);

            DiscountStrategy strategy = (discount > 0) ? new FixedDiscount(discount) : new NoDiscount();
            tariffs.add(new Tariff(destination, basePrice, strategy));
        }
    }
}
