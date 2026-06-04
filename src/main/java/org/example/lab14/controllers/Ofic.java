package org.example.lab14.controllers;

import javafx.animation.*;
import javafx.collections.*; //автоматическое реагирование на любые изменения данных
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos; // Позиционирование элементов
import javafx.scene.control.*; // Стандартные элементы управления
import javafx.scene.control.cell.PropertyValueFactory; // Привязка данных к таблице
import javafx.scene.image.*;
import javafx.scene.layout.*; // Базовые контейнеры
import javafx.util.Duration; // Работа со временем
import org.example.lab14.Main;
import org.example.lab14.db.DatabaseHandler;
import org.example.lab14.models.*;
import org.example.lab14.models.MenuItem;
import java.time.LocalDateTime; // Получение текущей даты и времени
import java.time.format.DateTimeFormatter;
import java.util.Locale; //язык

public class Ofic {

    @FXML private AnchorPane rootPane; // Главная панель окна
    @FXML private ComboBox<String> tableComboBox; // Выпадающий список для выбора столика
    @FXML private Label dateTimeLabel, totalLabel, waiterNameLabel, receiptNumberLabel; //Дата/Время, Итоговая сумма, Имя официанта, Номер чека
    @FXML private TilePane hotTilePane, saladTilePane, drinkTilePane, dessertTilePane;//панели
    @FXML private TableView<CartItem> orderTableView; // Таблица корзины
    @FXML private TableColumn<CartItem, String> dishColumn;
    @FXML private TableColumn<CartItem, Integer> qtyColumn;
    @FXML private TableColumn<CartItem, Double> priceColumn;

    private int currentReceiptNumber = 1; // Переменная для хранения текущего номера чека
    private final ObservableList<CartItem> currentOrder = FXCollections.observableArrayList(); //список для хранения содержимого текущего чека

    @FXML
    public void initialize() {
        // Главный метод инициализации работает как оглавление.
        setupBackground();
        setupUserInfo();
        setupTableColumns();
        setupReceiptAndTables();//связь данных чека с экраном
        startClock();
        loadAllMenus();
        updateTotal(); // Обновляем итоговую сумму чека
    }

    private void setupBackground() {
        //ЗАГРУЗКА И НАСТРОЙКА ФОНА
        try {
            ImageView bgView = new ImageView(new Image(getClass().getResourceAsStream("/org/example/lab14/fon.jpg")));
            // Растягиваем по размеру окна
            bgView.fitWidthProperty().bind(rootPane.widthProperty());
            bgView.fitHeightProperty().bind(rootPane.heightProperty());
            bgView.setOpacity(0.4);
            rootPane.getChildren().add(0, bgView);
        } catch (Exception e) {
            System.out.println("Фон не загружен: " + e.getMessage());
        }
    }

    private void setupUserInfo() {
        if (Main.currentUser != null && waiterNameLabel != null) {//вход и метка
            waiterNameLabel.setText("Официант " + Main.currentUser.getFio());
        }
    }

    private void setupTableColumns() {
        // Настраиваем колонки корзины
        if (dishColumn != null) {
            dishColumn.setCellValueFactory(new PropertyValueFactory<>("dishName"));
            qtyColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
            priceColumn.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));

            // НАСТРОЙКА: Выводим цену в таблице чека строго с двумя знаками после запятой
            priceColumn.setCellFactory(tc -> new TableCell<CartItem, Double>() {
                @Override
                protected void updateItem(Double price, boolean empty) {
                    super.updateItem(price, empty);
                    if (empty || price == null) {
                        setText(null);
                    } else {
                        setText(String.format(Locale.US, "%.2f", price));
                    }
                }
            });
        }
        if (orderTableView != null) orderTableView.setItems(currentOrder); // Привязываем нашу корзину к таблице
    }

    private void setupReceiptAndTables() {
        currentReceiptNumber = DatabaseHandler.getNextReceiptNumber(); // Запрашиваем из БД следующий свободный номер чека
        if (receiptNumberLabel != null) receiptNumberLabel.setText("Чек №" + currentReceiptNumber); // Показываем номер на экране

        if (tableComboBox != null) {
            tableComboBox.setItems(FXCollections.observableArrayList("1", "2", "3", "4", "5")); // Создаем список доступных столиков и засовываем в ComboBox
            tableComboBox.getSelectionModel().selectFirst(); // По умолчанию выбираем первый столик
        }
    }

    private void loadAllMenus() {
        loadMenuToPane(hotTilePane, "горячее");
        loadMenuToPane(saladTilePane, "салаты");
        loadMenuToPane(drinkTilePane, "напитки");
        loadMenuToPane(dessertTilePane, "десерты");
    }

    private void loadMenuToPane(TilePane pane, String category) {
        if (pane == null) return;
        pane.getChildren().clear(); // Очищаем контейнер
        pane.setAlignment(Pos.TOP_CENTER);
        pane.setPrefTileWidth(290.0);

        //отступы между карточками
        pane.setHgap(20.0); //горизонталь
        pane.setVgap(20.0); //вертикаль

        for (MenuItem item : DatabaseHandler.getMenuByCategory(category)) { //для каждого блюда создаем свою кнопку на экране
            Button btn = createDishCard(item); //Создаем внешний вид карточки
            btn.setOnAction(e -> addToCart(item)); // при клике на кнопку добавить блюдо в корзину
            pane.getChildren().add(btn); // добавляем карточку на экран
        }
    }

    private Button createDishCard(MenuItem item) {

        Button btn = new Button();
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("-fx-background-color: transparent; -fx-padding: 0;");

        VBox card = new VBox();
        card.setAlignment(Pos.TOP_CENTER);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #DDDDDD; -fx-border-radius: 10; -fx-border-width: 1;");

        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(); // Создаем маску для обрезки вылезающих углов карточки
        clip.setArcWidth(20); // Задаем радиус скругления по ширине
        clip.setArcHeight(20); // Задаем радиус скругления по высоте
        clip.widthProperty().bind(card.widthProperty());
        clip.heightProperty().bind(card.heightProperty());
        card.setClip(clip); // Применяем готовую маску

        Region imgRegion = new Region(); //пустой холст для фотографии
        imgRegion.prefWidthProperty().bind(card.widthProperty());
        imgRegion.prefHeightProperty().bind(imgRegion.prefWidthProperty());
        imgRegion.setMinHeight(Region.USE_PREF_SIZE);
        imgRegion.setMaxHeight(Region.USE_PREF_SIZE);

        //Поиск картинки
        java.net.URL imgResource = getClass().getResource("/org/example/lab14/" + item.getName() + ".jpg");
        if (imgResource != null) { // Если картинка есть в папке

            // Формируем путь к картинке по названию блюда
            String imgUrl = imgResource.toExternalForm();
            imgRegion.setStyle("-fx-background-image: url('" + imgUrl + "'); -fx-background-size: cover; -fx-background-position: center; -fx-background-repeat: no-repeat;");
        } else {
            imgRegion.setStyle("-fx-background-color: #E0E0E0;");
        }
        // Создаем текстовую метку для названия блюда
        Label nameLbl = new Label(item.getName());
        nameLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-wrap-text: true; -fx-alignment: center; -fx-text-fill: #222222;");

        //цена (форматируем в double с двумя нулями)
        String priceStr = String.format(Locale.US, "%.2f", item.getPrice());
        Label priceLbl = new Label(priceStr + " руб");
        priceLbl.setStyle("-fx-font-size: 16px; -fx-text-fill: #1dc3f5; -fx-font-weight: bold;");

        VBox mainTextBlock = new VBox(6, nameLbl, priceLbl);
        mainTextBlock.setAlignment(Pos.CENTER);
        mainTextBlock.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 12; -fx-border-color: #DDDDDD; -fx-border-width: 1 0 0 0;");

        String sostavText = (item.getSostav() != null && !item.getSostav().isEmpty()) ? item.getSostav() : "Состав не указан";

        Label sostavLbl = new Label(sostavText); // Создаем метку для состава
        sostavLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #888888; -fx-wrap-text: true; -fx-text-alignment: center;");

        sostavLbl.maxWidthProperty().bind(card.widthProperty().subtract(24));

        VBox sostavBlock = new VBox(sostavLbl); // Кладем состав в отдельный нижний контейнер-нарост
        sostavBlock.setAlignment(Pos.CENTER);
        sostavBlock.setStyle("-fx-background-color: white; -fx-padding: 10; -fx-border-color: #DDDDDD; -fx-border-width: 1 0 0 0;");

        card.getChildren().addAll(imgRegion, mainTextBlock, sostavBlock); // Собираем карточку
        btn.setGraphic(card); //карточка стала кнопкой
        return btn;
    }

    private void addToCart(MenuItem menuItem) { //добавление блюда в чек
        for (CartItem cartItem : currentOrder) { // Проверяем: есть ли уже такое блюдо в корзине?
            if (cartItem.getMenuItem().getId() == menuItem.getId()) { // Если ID совпадают
                cartItem.increaseQuantity(); //увеличиваем количество
                if (orderTableView != null) orderTableView.refresh(); //обновляем визуальную таблицу
                updateTotal(); // Пересчитываем итоговую сумму
                return;
            }
        }
        currentOrder.add(new CartItem(menuItem));
        updateTotal(); // Пересчитываем сумму
    }

    private void updateTotal() {
        // Считаем сумму всех позиций в корзине
        double total = currentOrder.stream().mapToDouble(CartItem::getTotalPrice).sum();
        if (totalLabel != null) {
            // Выводим с 2 знаками после запятой
            totalLabel.setText(String.format(Locale.US, "%.2f руб", total));
        }
    }

    @FXML void increaseItem(ActionEvent event) { modifyCartItem(1); }
    @FXML void decreaseItem(ActionEvent event) { modifyCartItem(-1); }
    @FXML void removeItem(ActionEvent event) { modifyCartItem(0); }

    private void modifyCartItem(int action) { //работа с корзиной
        if (orderTableView == null) return;
        CartItem selected = orderTableView.getSelectionModel().getSelectedItem(); // Какая строка выделена
        if (selected == null) return;

        if (action == 1) selected.increaseQuantity(); // Увеличиваем количество
        else if (action == -1) selected.decreaseQuantity(); // Уменьшаем количество
        else currentOrder.remove(selected); // Полностью удаляем позицию из чека

        orderTableView.refresh();
        updateTotal();
    }

    @FXML void processPayment(ActionEvent event) { //кнопка"Оплатить"
        if (currentOrder.isEmpty()) { // Проверка от дурака
            showAlert("Ошибка", "Заказ пуст. Добавьте блюда перед оплатой.", Alert.AlertType.WARNING);
            return;
        }
        // Берем текущее системное время
        LocalDateTime now = LocalDateTime.now();
        String date = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")); // Форматируем дату
        String time = now.format(DateTimeFormatter.ofPattern("HH:mm:ss")); // Форматируем время

        // Читаем сумму обратно как double
        double total = Double.parseDouble(totalLabel.getText().replace(" руб", "").replace(",", "."));
        int tableNum = Integer.parseInt(tableComboBox.getValue());
        String waiterName = (Main.currentUser != null) ? Main.currentUser.getFio() : "Неизвестный"; // Берем ФИО текущего официанта

        // Внимание: если ваш метод DatabaseHandler.createOrder принимает int, измените там тип на double,
        // либо скастуйте total обратно в (int), написав (int) total
        int orderId = DatabaseHandler.createOrder(date, time, total, waiterName, currentReceiptNumber, tableNum); // Делаем запись в БД

        if (orderId != -1) { // Если ID чека получен
            DatabaseHandler.saveOrderItems(orderId, currentOrder); //привязываем их к этому ID чека
            showAlert("Успех", "Заказ успешно оплачен! (Чек №" + currentReceiptNumber + ", Стол №" + tableNum + ")", Alert.AlertType.INFORMATION);
            currentOrder.clear();
            updateTotal();
            currentReceiptNumber++; // Увеличиваем счетчик номеров чека
            if (receiptNumberLabel != null) receiptNumberLabel.setText("Чек №" + currentReceiptNumber); // Показываем новый номер
        } else {
            showAlert("Ошибка БД", "Не удалось сохранить заказ.", Alert.AlertType.ERROR);
        }
    }
    private void showAlert(String title, String content, Alert.AlertType type) { // Стандартный показ окон
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void startClock() { //Логика часов
        if (dateTimeLabel == null) return;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy 'г.', HH:mm", new Locale("ru")); // Шаблон
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> { // Таймер: мгновенное выполнение при старте
            String formattedDate = LocalDateTime.now().format(formatter); // Берем время и форматируем
            dateTimeLabel.setText(formattedDate); // Пишем на экран
        }), new KeyFrame(Duration.seconds(1))); // Повторяем кадр каждую 1 секунду
        clock.setCycleCount(Animation.INDEFINITE); // Крутить бесконечно
        clock.play();
    }

    @FXML void logout(ActionEvent event) { // Кнопка "Выход"
        Main.currentUser = null; // Сбрасываем сессию
        Main.setRoot("/org/example/lab14/Prototip.fxml"); // Возвращаем экран логина
    }
}