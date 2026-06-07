package org.example.lab14.controllers;

import javafx.application.Platform; // класс для отложенного выполнения кода
import javafx.collections.FXCollections; // обновляемые списки
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Group; // контейнер для группировки надписей у графика
import javafx.scene.Node;// общие свойства графических элементов
import javafx.scene.chart.BarChart; // класс для создания графика
import javafx.scene.chart.NumberAxis; // класс для создания числовой оси
import javafx.scene.chart.XYChart; // класс для работы с осями и данными графика
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

public class Admin {

    @FXML private AnchorPane rootPane;// холст
    @FXML private Label tabTitle; // заголовок текущей вкладки
    @FXML private Button btnMenu, btnEmployees, btnOrders, btnCharts;// меню, сотрудники, заказы, графики
    @FXML private VBox ordersPane, menuPane, chartsPane, employeesPane; // страницы-вкладки

    // ЭЛЕМЕНТЫ ВКЛАДКИ ЗАКАЗЫ
    @FXML private TableView<Order> ordersTable; // верхняя таблица
    @FXML private TableColumn<Order, Integer> colOrderId, colCheck, colTable;
    @FXML private TableColumn<Order, String> colDate, colTime, colWaiter;
    @FXML private TableColumn<Order, Double> colTotal; // сумма чека
    @FXML private TableView<AdminOrderItem> orderItemsTable; // нижняя таблица
    @FXML private TableColumn<AdminOrderItem, String> colItemName;// названия блюд
    @FXML private TableColumn<AdminOrderItem, Double> colItemPrice, colItemTotal;
    @FXML private TableColumn<AdminOrderItem, Integer> colItemQty; // количество порций

    // ЭЛЕМЕНТЫ ВКЛАДКИ МЕНЮ
    @FXML private TableView<MenuItem> menuTable;// таблица меню
    @FXML private TableColumn<MenuItem, Integer> colMenuId, colMenuProdan;
    @FXML private TableColumn<MenuItem, String> colMenuName, colMenuCategory, colMenuSostav;
    @FXML private TableColumn<MenuItem, Double> colMenuPrice;
    @FXML private TextField fldMenuName, fldMenuPrice, fldMenuSostav; // роля ввода для добавления нового блюда
    @FXML private ComboBox<String> cmbMenuCategory; // выпадающий список для выбора категории нового блюда

    // ЭЛЕМЕНТЫ ВКЛАДКИ СОТРУДНИКИ
    @FXML private TableView<Employee> empTable;// таблица сотрудники
    @FXML private TableColumn<Employee, Integer> colEmpId;
    @FXML private TableColumn<Employee, String> colEmpFio, colEmpData, colEmpTelefon;
    @FXML private TextField fldEmpFio, fldEmpData, fldEmpTelefon;

    // ЭЛЕМЕНТЫ ВКЛАДКИ ГРАФИКИ
    @FXML private BarChart<String, Number> barChart;
    private final List<Text> chartLabels = new ArrayList<>();// хранение текста над графиком

    // стили для боковых кнопок
    private final String IDLE = "-fx-background-color: transparent; -fx-border-color: #3B3B3B; -fx-border-width: 0 0 1 0;";
    private final String ACTIVE = "-fx-background-color: rgba(211, 211, 211, 0.7); -fx-border-color: #3B3B3B; -fx-border-width: 0 0 1 0;";

    @FXML
    public void initialize() {
        try {
            ImageView bgView = new ImageView(new Image(getClass().getResourceAsStream("/org/example/lab14/fon.jpg")));
            bgView.fitWidthProperty().bind(rootPane.widthProperty());// приклеивание картинки к краям по ширине
            bgView.fitHeightProperty().bind(rootPane.heightProperty());// приклеивание картинки к краям по высоте
            bgView.setOpacity(0.4);
            rootPane.getChildren().add(0, bgView);
        } catch (Exception e) { System.out.println("Фон не загружен: " + e.getMessage()); }

        setupTableColumns(); // настройка связей колонок с моделями данных

        if (barChart != null) {
            barChart.setTitle(null);// стирание заголовка
            barChart.setLegendVisible(true); // цветные квадраты с именами внизу
            barChart.setAnimated(false); // выключаем анимацию появления столбцов
            if (barChart.getYAxis() instanceof NumberAxis yAxis) {
                yAxis.setLabel("Выручка (руб)");
            }
        }

        // выделение строки в верхней таблице заказов
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
    // ВКЛАДКА ЗАКАЗЫ
    private void setupTableColumns() {//привязываем колонки верхней таблицы к полям нашей модели
        colOrderId.setCellValueFactory(new PropertyValueFactory<>("id")); colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("time")); colCheck.setCellValueFactory(new PropertyValueFactory<>("checkNum"));
        colTable.setCellValueFactory(new PropertyValueFactory<>("tableNum")); colWaiter.setCellValueFactory(new PropertyValueFactory<>("waiter"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));

        //привязываем колонки нижней таблицы к полям нашей модели
        colItemName.setCellValueFactory(new PropertyValueFactory<>("name")); colItemPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colItemQty.setCellValueFactory(new PropertyValueFactory<>("quantity")); colItemTotal.setCellValueFactory(new PropertyValueFactory<>("total"));

        // ВКЛАДКА МЕНЮ
        colMenuId.setCellValueFactory(new PropertyValueFactory<>("id")); colMenuName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colMenuCategory.setCellValueFactory(new PropertyValueFactory<>("category")); colMenuSostav.setCellValueFactory(new PropertyValueFactory<>("sostav"));
        colMenuPrice.setCellValueFactory(new PropertyValueFactory<>("price")); colMenuProdan.setCellValueFactory(new PropertyValueFactory<>("prodan"));

        // ВКЛАДКА СОТРУДНИКИ
        colEmpId.setCellValueFactory(new PropertyValueFactory<>("id")); colEmpFio.setCellValueFactory(new PropertyValueFactory<>("fio"));
        colEmpData.setCellValueFactory(new PropertyValueFactory<>("data")); colEmpTelefon.setCellValueFactory(new PropertyValueFactory<>("telefon"));
    }
    // нажатия на кнопки боковой панели
    @FXML void showMenu(ActionEvent event) { selectTab(btnMenu, "Меню"); loadData(menuTable, DatabaseHandler.getAllMenu()); }
    @FXML void showEmployees(ActionEvent event) { selectTab(btnEmployees, "Сотрудники"); loadData(empTable, DatabaseHandler.getAllEmployees()); }
    @FXML void showOrders(ActionEvent event) { selectTab(btnOrders, "Заказы"); loadData(ordersTable, DatabaseHandler.getAllOrders()); }
    @FXML void showCharts(ActionEvent event) { selectTab(btnCharts, "Статистика продаж"); updateChart(); }

    // загрузка любого списка данных в любую таблицу
    private <T> void loadData(TableView<T> table, List<T> data) {
        if (table != null) table.setItems(FXCollections.observableArrayList(data));
    }

    private void selectTab(Button button, String title) { // визуальное переключение вкладок
        btnMenu.setStyle(IDLE); btnEmployees.setStyle(IDLE); btnOrders.setStyle(IDLE); btnCharts.setStyle(IDLE); // сбрасываем стили всех кнопок в серый
        if (button != null) button.setStyle(ACTIVE); // подсвечиваем нажатую кнопку
        if (tabTitle != null) tabTitle.setText(title); // меняем главный текстовый заголовок сверху

        // показываем только если нажата кнопка
        ordersPane.setVisible(button == btnOrders);
        menuPane.setVisible(button == btnMenu);
        chartsPane.setVisible(button == btnCharts);
        employeesPane.setVisible(button == btnEmployees);

        // очищаем нижнюю таблицу
        if (button != btnOrders && orderItemsTable != null) orderItemsTable.getItems().clear();
    }

    private void updateChart() { // перестроение всего графика
        if (barChart == null) return;
        barChart.getData().clear();

        // убираем старые надписи с экрана
        chartLabels.removeIf(l -> l.getParent() instanceof Group parent && parent.getChildren().remove(l));
        chartLabels.clear();

        // запрашиваем статистику из БД
        var allStats = DatabaseHandler.getAllWaitersStatsByMonth();
        // ищем самую большую выручку
        int maxSum = allStats.values().stream()
                .flatMap(months -> months.values().stream())
                .mapToInt(arr -> (int) arr[1])
                .max().orElse(0);

        // настраиваем ось Y
        if (barChart.getYAxis() instanceof NumberAxis yAxis) {
            yAxis.setAutoRanging(false); //расчёт шкалы
            yAxis.setLowerBound(0);// старт от нуля
            yAxis.setUpperBound(Math.ceil(maxSum * 1.35 / 5000) * 5000);
            yAxis.setTickUnit(5000);
        }

        String[] colors = {"#4CAF50", "#2196F3", "#F44336","#FFFF00"};
        int colorIndex = 0; // счетчик для выдачи цветов официантам по очереди

        // проходимся по каждому официанту из словаря статистики
        for (var entry : allStats.entrySet()) {
            var series = new XYChart.Series<String, Number>();// группа столбиков для официанта
            series.setName(entry.getKey()); // имя официанта
            String myColor = colors[colorIndex % colors.length];// цвет

            for (String month : new String[]{"04", "05"}) { // перебираем Апрель и Май
                double[] stats = entry.getValue().getOrDefault(month, new double[]{0, 0}); // ищем данные за месяц, если их нет — берем нули
                int count = (int) stats[0]; // извлекаем из массива количество продаж
                int sum   = (int) stats[1]; // извлекаем из массива сумму выручки
                var data = new XYChart.Data<String, Number>(month.equals("04") ? "Апрель" : "Май", sum); // создаем столбец: имя месяца на ось X, сумму на ось Y
                if (count > 0) { // Если у официанта были продажи в этом месяце то
                    addLabelToBar(data, sum, count, myColor); //  отрисовываем текст с рублями и штуками над столбцом
                }
                series.getData().add(data); // добавляем готовый столбец в персональную серию данных официанта
            }
            barChart.getData().add(series); // выводим всю серию официанта (столбцы Апреля и Мая) на общий график
            colorIndex++; // сдвигаем счетчик, чтобы следующий официант получил новый цвет
        }

        Platform.runLater(() -> { // откладываем выполнение кода до тех пор, пока графический поток не освободится
            barChart.applyCss(); // применяем CSS-стили к графику
            barChart.layout(); // заставляем график пересчитать координаты и размеры всех своих элементов на экране
            int i = 0; // счетчик-указатель для перебора цветов из массива

            // ищем все элементы легенды, у которых в CSS прописан класс .chart-legend-item-symbol
            for (Node legendSymbol : barChart.lookupAll(".chart-legend-item-symbol")) {
                if (i < colors.length) // Проверяем, что мы не вышли за пределы массива с заготовленными цветами
                    legendSymbol.setStyle("-fx-background-color: " + colors[i++] + ";"); // Красим квадратик легенды в цвет официанта и двигаем счетчик
            }
        });
    }

    // текст графиков
    private void addLabelToBar(XYChart.Data<String, Number> data, int sum, int count, String hexColor) { // создание текстовой метки для конкретного столбца
        Text dataText = new Text(String.format("%d руб\n(%d шт.)", sum, count)); // создаем графический объект текста и форматируем его: Сумма руб (Количество шт.)
        dataText.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-fill: #1A1A1A;");
        dataText.setTextAlignment(TextAlignment.CENTER);
        chartLabels.add(dataText); // регистрируем надпись в списке для её последующего удаления при обновлении графика

        data.nodeProperty().addListener((obs, old, node) -> { // ждем, когда сделается прямоугольник для столбца
            if (node == null) return; // если столбца на экране ещё нет, то ничего не делаем
            node.setStyle("-fx-bar-fill: " + hexColor + ";"); // красим прямоугольник столбца в персональный цвет официанта
            node.parentProperty().addListener((o, oldP, parent) -> { // ждем, когда столбец прикрепится к своему родительскому контейнеру

                if (parent instanceof Group) {
                    Group group = (Group) parent;
                    if (!group.getChildren().contains(dataText)) group.getChildren().add(dataText); // если нашей надписи еще нет в этой группе, добавляем её на экран
                }
            });

            node.boundsInParentProperty().addListener((o, oldB, b) -> // следим за изменением координат и размеров столбца на экране
                    dataText.relocate( // перемещаем текстовую надпись на новые рассчитанные координаты
                            Math.round(b.getMinX() + b.getWidth() / 2 - dataText.prefWidth(-1) / 2), // выравниваем текст строго по центру столбца
                            Math.round(b.getMinY() - 35) // поднимаем текст на 35 пикселей
                    )
            );
        });
    }

    // КНОПКА ДОБАВИТЬ СОТРУДНИКА
    @FXML void handleAddEmp(ActionEvent event) {
        if (fldEmpFio.getText().trim().isEmpty()) { showAlert("Ошибка", "Введите ФИО", Alert.AlertType.WARNING); return; }
        if (DatabaseHandler.addEmployee(fldEmpFio.getText().trim(), fldEmpData.getText().trim(), fldEmpTelefon.getText().trim())) {
            clearEmpFields(); showEmployees(null); // Очищаем поля и обновляем таблицу
        } else showAlert("Ошибка БД", "Не удалось добавить", Alert.AlertType.ERROR);
    }

    // КНОПКА ИЗМЕНИТЬ СОТРУДНИКА
    @FXML void handleUpdateEmp(ActionEvent event) {
        Employee sel = empTable.getSelectionModel().getSelectedItem(); // Смотрим, кто выделен в таблице
        if (sel == null) { showAlert("Внимание", "Выберите сотрудника", Alert.AlertType.WARNING); return; }
        if (DatabaseHandler.updateEmployee(sel.getId(), fldEmpFio.getText().trim(), fldEmpData.getText().trim(), fldEmpTelefon.getText().trim())) {
            clearEmpFields(); showEmployees(null);
        } else showAlert("Ошибка БД", "Не удалось обновить", Alert.AlertType.ERROR);
    }

    // КНОПКА УДАЛИТЬ СОТРУДНИКА
    @FXML void handleDeleteEmp(ActionEvent event) {
        Employee sel = empTable.getSelectionModel().getSelectedItem();
        if (sel != null && DatabaseHandler.deleteEmployee(sel.getId())) {
            clearEmpFields(); showEmployees(null);
        } else showAlert("Внимание", "Выберите сотрудника для удаления", Alert.AlertType.WARNING);
    }

    private void clearEmpFields() { // быстрая очистка всех текстовых полей ввода сотрудников
        fldEmpFio.clear(); fldEmpData.clear(); fldEmpTelefon.clear();
        empTable.getSelectionModel().clearSelection(); // снимаем выделение в таблице
    }

    // КНОПКА ДОБАВИТЬ БЛЮДО
    @FXML void handleAddMenu(ActionEvent event) {
        try {
            double price = Double.parseDouble(fldMenuPrice.getText().replace(",", "."));
            if (DatabaseHandler.addMenuItem(fldMenuName.getText().trim(), cmbMenuCategory.getValue(), fldMenuSostav.getText().trim(), price)) { // Отправляем в БД
                fldMenuName.clear(); cmbMenuCategory.getSelectionModel().clearSelection(); fldMenuSostav.clear(); fldMenuPrice.clear(); showMenu(null); // Чистим поля, обновляем таблицу
            } else showAlert("Ошибка", "Ошибка БД", Alert.AlertType.ERROR);
        } catch (Exception e) { showAlert("Ошибка", "Проверьте введенные данные. Цена должна быть числом.", Alert.AlertType.WARNING); }
    }

    // КНОПКА УДАЛИТЬ БЛЮДО
    @FXML void handleDeleteMenu(ActionEvent event) {
        MenuItem sel = menuTable.getSelectionModel().getSelectedItem();
        if (sel != null && DatabaseHandler.deleteMenuItem(sel.getId())) {
            // очистка полей
            fldMenuName.clear(); cmbMenuCategory.getSelectionModel().clearSelection(); fldMenuSostav.clear(); fldMenuPrice.clear();
            showMenu(null);
        } else {
            showAlert("Внимание", "Выберите блюдо для удаления", Alert.AlertType.WARNING);
        }
    }

    private void showAlert(String t, String c, Alert.AlertType type) { // для вызовов всплывающих диалоговых окон
        Alert a = new Alert(type); a.setTitle(t); a.setHeaderText(null); a.setContentText(c); a.showAndWait();
    }
    // КНОПКА ВЫХОД
    @FXML void logout(ActionEvent event) {
        Main.currentUser = null; // Стираем данные сессии
        Main.setRoot("/org/example/lab14/Prototip.fxml");
    }
}