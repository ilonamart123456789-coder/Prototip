package org.example.lab14.controllers;

import javafx.application.Platform; // Класс для отложенного выполнения кода
import javafx.collections.FXCollections; // обновляемые списки
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Group; // Контейнер для группировки надписей у графика
import javafx.scene.Node;
import javafx.scene.chart.BarChart; // Класс для создания графика
import javafx.scene.chart.NumberAxis; // Класс для создания числовой оси
import javafx.scene.chart.XYChart; // Базовый класс для работы с осями и данными графика
import javafx.scene.control.*; // все стандартные элементы управления
import javafx.scene.control.cell.PropertyValueFactory; // связь колонки таблицы с переменной из модели
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import org.example.lab14.Main;
import org.example.lab14.db.DatabaseHandler;
import org.example.lab14.models.*;
import org.example.lab14.models.MenuItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Admin {

    @FXML private AnchorPane rootPane;
    @FXML private Label tabTitle; // заголовок текущей вкладки
    @FXML private Button btnMenu, btnEmployees, btnOrders, btnCharts;
    @FXML private VBox ordersPane, menuPane, chartsPane, employeesPane; // страницы-вкладки

    // ЭЛЕМЕНТЫ ВКЛАДКИ "ЗАКАЗЫ"
    @FXML private TableView<Order> ordersTable; // Верхняя таблица
    @FXML private TableColumn<Order, Integer> colOrderId, colCheck, colTable;
    @FXML private TableColumn<Order, String> colDate, colTime, colWaiter;
    @FXML private TableColumn<Order, Double> colTotal; // сумма чека
    @FXML private TableView<AdminOrderItem> orderItemsTable; // Нижняя таблица
    @FXML private TableColumn<AdminOrderItem, String> colItemName;
    @FXML private TableColumn<AdminOrderItem, Double> colItemPrice, colItemTotal;
    @FXML private TableColumn<AdminOrderItem, Integer> colItemQty; // количество порций

    // ЭЛЕМЕНТЫ ВКЛАДКИ "МЕНЮ"
    @FXML private TableView<MenuItem> menuTable;
    @FXML private TableColumn<MenuItem, Integer> colMenuId, colMenuProdan;
    @FXML private TableColumn<MenuItem, String> colMenuName, colMenuCategory, colMenuSostav;
    @FXML private TableColumn<MenuItem, Double> colMenuPrice;
    @FXML private TextField fldMenuName, fldMenuPrice, fldMenuSostav; // Поля ввода для добавления нового блюда
    @FXML private ComboBox<String> cmbMenuCategory; // Выпадающий список для выбора категории нового блюда

    // ЭЛЕМЕНТЫ ВКЛАДКИ "СОТРУДНИКИ"
    @FXML private TableView<Employee> empTable;
    @FXML private TableColumn<Employee, Integer> colEmpId;
    @FXML private TableColumn<Employee, String> colEmpFio, colEmpData, colEmpTelefon;
    @FXML private TextField fldEmpFio, fldEmpData, fldEmpTelefon;

    // ЭЛЕМЕНТЫ ВКЛАДКИ "ГРАФИКИ"
    @FXML private BarChart<String, Number> barChart;
    private final List<Text> chartLabels = new ArrayList<>();

    //стили для боковых кнопок
    private final String IDLE = "-fx-background-color: transparent; -fx-border-color: #3B3B3B; -fx-border-width: 0 0 1 0;";
    private final String ACTIVE = "-fx-background-color: rgba(211, 211, 211, 0.7); -fx-border-color: #3B3B3B; -fx-border-width: 0 0 1 0;";

    @FXML
    public void initialize() {
        try {
            ImageView bgView = new ImageView(new Image(getClass().getResourceAsStream("/org/example/lab14/fon.jpg")));
            bgView.fitWidthProperty().bind(rootPane.widthProperty());
            bgView.fitHeightProperty().bind(rootPane.heightProperty());
            bgView.setOpacity(0.4);
            rootPane.getChildren().add(0, bgView);
        } catch (Exception e) { System.out.println("Фон не загружен: " + e.getMessage()); }

        setupTableColumns(); //настройка связей колонок с моделями данных

        if (barChart != null) {
            barChart.setTitle(null);
            barChart.setLegendVisible(true); //цветные квадратики с именами внизу
            barChart.setAnimated(false); // Выключаем анимацию появления столбцов
            if (barChart.getYAxis() instanceof NumberAxis yAxis) {
                yAxis.setLabel("Выручка (руб)");
            }
        }

        // Выделение строки в верхней таблице заказов
        ordersTable.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            // загружаем детали из БД в нижнюю таблицу по ID чека
            if (val != null) orderItemsTable.setItems(FXCollections.observableArrayList(DatabaseHandler.getOrderItemsByOrderId(val.getId())));
        });

        // при клике на сотрудника его данные копируются в текстовые поля внизу
        empTable.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            if (val != null) { // Если выбрали кого-то
                fldEmpFio.setText(val.getFio());
                fldEmpData.setText(val.getData());
                fldEmpTelefon.setText(val.getTelefon());
            }
        });

        // при клике на блюдо его данные копируются в поля ввода
        menuTable.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            if (val != null) { // Если кликнули на блюдо
                fldMenuName.setText(val.getName());
                cmbMenuCategory.setValue(val.getCategory());
                fldMenuSostav.setText(val.getSostav());
                fldMenuPrice.setText(String.valueOf(val.getPrice()));
            }
        });

        // категории
        if (cmbMenuCategory != null) cmbMenuCategory.setItems(FXCollections.observableArrayList("горячее", "салаты", "напитки", "десерты"));
        showMenu(null);
    }

    private void setupTableColumns() { // откуда брать данные колонкам
        colOrderId.setCellValueFactory(new PropertyValueFactory<>("id")); colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("time")); colCheck.setCellValueFactory(new PropertyValueFactory<>("checkNum"));
        colTable.setCellValueFactory(new PropertyValueFactory<>("tableNum")); colWaiter.setCellValueFactory(new PropertyValueFactory<>("waiter"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));

        colItemName.setCellValueFactory(new PropertyValueFactory<>("name")); colItemPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colItemQty.setCellValueFactory(new PropertyValueFactory<>("quantity")); colItemTotal.setCellValueFactory(new PropertyValueFactory<>("total"));

        colMenuId.setCellValueFactory(new PropertyValueFactory<>("id")); colMenuName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colMenuCategory.setCellValueFactory(new PropertyValueFactory<>("category")); colMenuSostav.setCellValueFactory(new PropertyValueFactory<>("sostav"));
        colMenuPrice.setCellValueFactory(new PropertyValueFactory<>("price")); colMenuProdan.setCellValueFactory(new PropertyValueFactory<>("prodan"));

        colEmpId.setCellValueFactory(new PropertyValueFactory<>("id")); colEmpFio.setCellValueFactory(new PropertyValueFactory<>("fio"));
        colEmpData.setCellValueFactory(new PropertyValueFactory<>("data")); colEmpTelefon.setCellValueFactory(new PropertyValueFactory<>("telefon"));
    }

    @FXML void showMenu(ActionEvent event) { selectTab(btnMenu, "Меню"); loadData(menuTable, DatabaseHandler.getAllMenu()); }
    @FXML void showEmployees(ActionEvent event) { selectTab(btnEmployees, "Сотрудники"); loadData(empTable, DatabaseHandler.getAllEmployees()); }
    @FXML void showOrders(ActionEvent event) { selectTab(btnOrders, "Заказы"); loadData(ordersTable, DatabaseHandler.getAllOrders()); }
    @FXML void showCharts(ActionEvent event) { selectTab(btnCharts, "Статистика продаж"); updateChart(); }

    // метод для загрузки любого списка данных в любую таблицу
    private <T> void loadData(TableView<T> table, List<T> data) {
        if (table != null) table.setItems(FXCollections.observableArrayList(data));
    }

    private void selectTab(Button button, String title) { // Метод визуального переключения вкладок
        btnMenu.setStyle(IDLE); btnEmployees.setStyle(IDLE); btnOrders.setStyle(IDLE); btnCharts.setStyle(IDLE); // Сбрасываем стили всех кнопок в серый
        if (button != null) button.setStyle(ACTIVE); // Подсвечиваем нажатую кнопку
        if (tabTitle != null) tabTitle.setText(title); // Меняем главный текстовый заголовок сверху

        ordersPane.setVisible(button == btnOrders); // Показываем только если нажата кнопка
        menuPane.setVisible(button == btnMenu);
        chartsPane.setVisible(button == btnCharts);
        employeesPane.setVisible(button == btnEmployees);

        // очищаем нижнюю таблицу
        if (button != btnOrders && orderItemsTable != null) orderItemsTable.getItems().clear();
    }

    private void updateChart() { // Метод перестроения всего графика
        if (barChart == null) return;
        barChart.getData().clear();

        //убираем старые надписи с экрана
        chartLabels.removeIf(l -> l.getParent() instanceof Group parent && parent.getChildren().remove(l));
        chartLabels.clear();

        //Запрашиваем статистику из БД
        var allStats = DatabaseHandler.getAllWaitersStatsByMonth();
        // Ищем самую большую выручку
        int maxSum = allStats.values().stream()
                .flatMap(months -> months.values().stream())
                .mapToInt(arr -> (int) arr[1])
                .max().orElse(0);

        //Настраиваем ось Y
        if (barChart.getYAxis() instanceof NumberAxis yAxis) {
            yAxis.setAutoRanging(false); //расчёт шкалы
            yAxis.setLowerBound(0);// Старт от нуля
            yAxis.setUpperBound(Math.ceil(maxSum * 1.35 / 5000) * 5000);
            yAxis.setTickUnit(5000);
        }

        String[] colors = {"#4CAF50", "#2196F3", "#F44336","#FFFF00"};
        int colorIndex = 0; // Счетчик для выдачи цветов официантам по очереди

        // Проходим по каждому официанту из словаря статистики
        for (var entry : allStats.entrySet()) {
            var series = new XYChart.Series<String, Number>();//группа столбиков для официанта
            series.setName(entry.getKey()); // Имя официанта
            String myColor = colors[colorIndex % colors.length];//цвет

            for (String month : new String[]{"04", "05"}) {
                double[] stats = entry.getValue().getOrDefault(month, new double[]{0, 0});
                int count = (int) stats[0];
                int sum   = (int) stats[1];
                var data = new XYChart.Data<String, Number>(month.equals("04") ? "Апрель" : "Май", sum);
                if (count > 0) {
                    addLabelToBar(data, sum, count, myColor);
                }
                series.getData().add(data);
            }
            barChart.getData().add(series);
            colorIndex++;
        }

        Platform.runLater(() -> {
            barChart.applyCss(); barChart.layout();
            int i = 0;
            // Ищем все маленькие квадратики-символы в Легенде по их CSS-классу
            for (Node legendSymbol : barChart.lookupAll(".chart-legend-item-symbol")) {
                if (i < colors.length) legendSymbol.setStyle("-fx-background-color: " + colors[i++] + ";");
            }
        });
    }

    // текст графиков
    private void addLabelToBar(XYChart.Data<String, Number> data, int sum, int count, String hexColor) {
        Text dataText = new Text(String.format("%d руб\n(%d шт.)", sum, count));
        dataText.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-fill: #1A1A1A;");
        dataText.setTextAlignment(TextAlignment.CENTER);
        chartLabels.add(dataText);

        data.nodeProperty().addListener((obs, old, node) -> {
            if (node == null) return;
            node.setStyle("-fx-bar-fill: " + hexColor + ";"); // Красим столбец
            node.parentProperty().addListener((o, oldP, parent) -> {

                if (parent instanceof Group) {
                    Group group = (Group) parent;
                    if (!group.getChildren().contains(dataText)) group.getChildren().add(dataText);
                }
            });

            node.boundsInParentProperty().addListener((o, oldB, b) ->
                    dataText.relocate(
                            Math.round(b.getMinX() + b.getWidth() / 2 - dataText.prefWidth(-1) / 2),
                            Math.round(b.getMinY() - 35)
                    )
            );
        });
    }

    @FXML void handleAddEmp(ActionEvent event) { // Кнопка "Добавить" сотрудника
        if (fldEmpFio.getText().trim().isEmpty()) { showAlert("Ошибка", "Введите ФИО", Alert.AlertType.WARNING); return; }
        if (DatabaseHandler.addEmployee(fldEmpFio.getText().trim(), fldEmpData.getText().trim(), fldEmpTelefon.getText().trim())) {
            clearEmpFields(); showEmployees(null); // Очищаем поля и обновляем таблицу
        } else showAlert("Ошибка БД", "Не удалось добавить", Alert.AlertType.ERROR);
    }

    @FXML void handleUpdateEmp(ActionEvent event) { // Кнопка "Изменить" сотрудника
        Employee sel = empTable.getSelectionModel().getSelectedItem(); // Смотрим, кто выделен в таблице
        if (sel == null) { showAlert("Внимание", "Выберите сотрудника", Alert.AlertType.WARNING); return; }
        if (DatabaseHandler.updateEmployee(sel.getId(), fldEmpFio.getText().trim(), fldEmpData.getText().trim(), fldEmpTelefon.getText().trim())) {
            clearEmpFields(); showEmployees(null);
        } else showAlert("Ошибка БД", "Не удалось обновить", Alert.AlertType.ERROR);
    }

    @FXML void handleDeleteEmp(ActionEvent event) { // Кнопка "Удалить" сотрудника
        Employee sel = empTable.getSelectionModel().getSelectedItem();
        if (sel != null && DatabaseHandler.deleteEmployee(sel.getId())) {
            clearEmpFields(); showEmployees(null);
        } else showAlert("Внимание", "Выберите сотрудника для удаления", Alert.AlertType.WARNING);
    }

    private void clearEmpFields() { // Метод для быстрой очистки всех текстовых полей ввода сотрудников
        fldEmpFio.clear(); fldEmpData.clear(); fldEmpTelefon.clear();
        empTable.getSelectionModel().clearSelection(); // Снимаем выделение в таблице
    }

    @FXML void handleAddMenu(ActionEvent event) { // Кнопка "Добавить" блюдо
        try {
            double price = Double.parseDouble(fldMenuPrice.getText().replace(",", "."));
            if (DatabaseHandler.addMenuItem(fldMenuName.getText().trim(), cmbMenuCategory.getValue(), fldMenuSostav.getText().trim(), price)) { // Отправляем в БД
                fldMenuName.clear(); cmbMenuCategory.getSelectionModel().clearSelection(); fldMenuSostav.clear(); fldMenuPrice.clear(); showMenu(null); // Чистим поля, обновляем таблицу
            } else showAlert("Ошибка", "Ошибка БД", Alert.AlertType.ERROR);
        } catch (Exception e) { showAlert("Ошибка", "Проверьте введенные данные. Цена должна быть числом.", Alert.AlertType.WARNING); }
    }

    @FXML void handleDeleteMenu(ActionEvent event) { // Кнопка "Удалить выбранное" блюдо
        MenuItem sel = menuTable.getSelectionModel().getSelectedItem();
        if (sel != null && DatabaseHandler.deleteMenuItem(sel.getId())) {
            // очистка полей
            fldMenuName.clear(); cmbMenuCategory.getSelectionModel().clearSelection(); fldMenuSostav.clear(); fldMenuPrice.clear();
            showMenu(null);
        } else {
            showAlert("Внимание", "Выберите блюдо для удаления", Alert.AlertType.WARNING);
        }
    }

    private void showAlert(String t, String c, Alert.AlertType type) { // метод для вызова всплывающих диалоговых окон
        Alert a = new Alert(type); a.setTitle(t); a.setHeaderText(null); a.setContentText(c); a.showAndWait();
    }

    @FXML void logout(ActionEvent event) { // Кнопка "Выход"
        Main.currentUser = null; // Стираем данные сессии
        Main.setRoot("/org/example/lab14/Prototip.fxml");
    }
}