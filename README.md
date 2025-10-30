# Лабораторная работа №4 — Десктопное приложение «Система управления тарифами аэропорта»

Данная лабораторная работа представляет собой десктопное Java-приложение, разработанное на основе консольного проекта из лабораторной работы №3.  
Цель — портировать консольное приложение в графический интерфейс с соблюдением всех требований: многокомпонентность, валидация, работа с файлами, сортировка и т.д.

> 💡 **Технологии**: Java 23 + Swing (встроен в JDK, не требует внешних зависимостей)  
> ✅ **Платформа**: Кроссплатформенное (Windows, macOS, Linux)  
> 📦 **Исполняемый файл**: Собирается в `.jar`, запускается командой `java -jar AirportManager.jar`

---

## 📌 Основной функционал

- ✅ **Два окна**: главное окно и диалог добавления/редактирования тарифа  
- ✅ **Перегрузка конструктора** второго окна (для создания нового тарифа и редактирования существующего)  
- ✅ **Добавление, изменение и удаление** тарифов вручную  
- ✅ **Сохранение и загрузка** данных в/из текстового файла (формат: `направление|базовая_цена|скидка`)  
- ✅ **Сортировка** по итоговой цене (по возрастанию/убыванию)  
- ✅ **Валидация ввода**: проверка названия направления, неотрицательных цен, корректности чисел  
- ✅ **Поиск тарифа с максимальной стоимостью**  
- ✅ Все классы данных (`Tariff`, `Airport`, `DiscountStrategy` и др.) вынесены в отдельные файлы  
- ✅ Используется `JTable` (аналог `DataGridView`) и `JFileChooser` (аналог `OpenFileDialog`)  
- ✅ Приложение компилируется в **единый исполняемый `.jar`-файл**

---


---

## 🖼️ Скриншоты интерфейса

*(Рекомендуется добавить 1–2 скриншота после сборки: главное окно и диалог добавления)*

---

## 📐 UML-диаграмма классов

![UML Diagram](https://www.plantuml.com/plantuml/png/XP1DIyCm48Nl-HNI42rLq4xGm5Ld9LmW8dLp5cNq9Lp5cNq9Lp5cNq9Lp5cNq9Lp5cNq9Lp5cNq9Lp5cNq9Lp5cNq9Lp5cNq9Lp5cNq9Lp5cNq9Lp5cNq9Lp5cNq9Lp5cNq9Lp5cNq9Lp5cNq9Lp5cNq9Lp5cNq9Lp5cNq9Lp5cNq9Lp5cNq9Lp5cNq9Lp5cNq9Lp5cNq9Lp5cNq9Lp5cNq9Lp5cNq9Lp5cNq9Lp5cNq9Lp5cNq9Lp5cNq9Lp5cNq9Lp5cNq9Lp5cNq9Lp5cNq9Lp5cNq9Lp5cNq9Lp5cNq9Lp5cNq9Lp5cNq9Lp5cNq9Lp5cNq9Lp5cNq9Lp5cNq9Lp5cNq9Lp5cNq9Lp5cNq9Lp5cNq9Lp5cNq9Lp5cNq9Lp5cNq9Lp5cNq9Lp5cNq9Lp5cNq9Lp5cNq9Lp5cNq9Lp5cNq9Lp5cNq9Lp5cNq9Lp5cNq9Lp5cNq9Lp5cNq9Lp5cNq9Lp5cNq9Lp5cNq9Lp5cNq9Lp5cNq9Lp5cNq9Lp5cNq9Lp5cNq9Lp5cNq9Lp5cNq9Lp5cN......)

> ⚠️ *Ссылка выше — заглушка. Чтобы сгенерировать реальную диаграмму:*
> 1. Скопируй [код PlantUML из отчёта](#uml-диаграмма-ниже)
> 2. Вставь на [https://www.plantuml.com/plantuml](https://www.plantuml.com/plantuml)
> 3. Сохрани изображение и замени ссылку

### Код PlantUML (для генерации диаграммы)

```plantuml
@startuml
package "model" {
  class Tariff {
    - destination: String
    - basePrice: double
    - discountStrategy: DiscountStrategy
    + Tariff(destination: String, basePrice: double, discountStrategy: DiscountStrategy)
    + getDestination(): String
    + getBasePrice(): double
    + getPrice(): double
    + toString(): String
  }

  class Airport {
    - tariffs: List<Tariff>
    + addTariff(tariff: Tariff): void
    + removeTariff(tariff: Tariff): boolean
    + getTariffs(): List<Tariff>
    + findMaxPriceTariff(): Tariff
    + saveToFile(filename: String): void
    + loadFromFile(filename: String): void
  }

  interface DiscountStrategy {
    + applyDiscount(basePrice: double): double
  }

  class NoDiscount {
    + NoDiscount()
    + applyDiscount(basePrice: double): double
  }

  class FixedDiscount {
    - discountAmount: double
    + FixedDiscount(discountAmount: double)
    + applyDiscount(basePrice: double): double
  }

  class InvalidTariffException {
    + InvalidTariffException(message: String)
  }
}

package "view" {
  class MainWindow
  class TariffEditDialog
  class TariffTableModel
}

class Main

Tariff --> "1" DiscountStrategy
Airport --> "0..*" Tariff
FixedDiscount ..|> DiscountStrategy
NoDiscount ..|> DiscountStrategy
InvalidTariffException --|> Exception

MainWindow --> "1" Airport
MainWindow --> TariffEditDialog : creates
TariffEditDialog --> Tariff : returns
Main --> MainWindow : creates
@enduml
