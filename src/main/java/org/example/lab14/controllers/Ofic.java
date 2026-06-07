package org.example.lab14.controllers;

import javafx.animation.*;
import javafx.collections.*; // автоматическое реагирование на любые изменения данных
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*; // стандартные элементы управления
import javafx.scene.control.cell.PropertyValueFactory; // привязка данных к таблице
import javafx.scene.image.*;
import javafx.scene.layout.*; // базовые контейнеры
import javafx.util.Duration; // работа со временем
import org.example.lab14.Main;
import org.example.lab14.db.DatabaseHandler;
import org.example.lab14.models.*;
import org.example.lab14.models.MenuItem;
import java.time.LocalDateTime; // получение текущей даты и времени
import java.time.format.DateTimeFormatter;
import java.util.Locale; // язык

public class Ofic {

    @FXML private AnchorPane rootPane; // Главная панель окна
    @FXML private ComboBox<String> tableComboBox; // выпадающий список для выбора столика
    @FXML private Label dateTimeLabel, totalLabel, waiterNameLabel, receiptNumberLabel; // дата и время, итоговая сумма, имя официанта, номер чека
    @FXML private TilePane hotTilePane, saladTilePane, drinkTilePane, dessertTilePane;// панели
    @FXML private TableView<CartItem> orderTableView; // таблица корзины
    @FXML private TableColumn<CartItem, String> dishColumn;// блюдо
    @FXML private TableColumn<CartItem, Integer> qtyColumn;// количество
    @FXML private TableColumn<CartItem, Double> priceColumn;// цена

    private int currentReceiptNumber = 1; // переменная для хранения текущего номера чека
    private final ObservableList<CartItem> currentOrder = FXCollections.observableArrayList(); // список для хранения содержимого текущего чека

    @FXML
    public void initialize() { // срабатывает при открытии окна
        setupBackground(); // делает динамичный фон
        setupUserInfo(); // фио вошедшего официанта и его роль
        setupTableColumns(); // привязывает колонки таблиц к полям классов-моделей
        setupReceiptAndTables(); // привязывает текущий заказ к интерфейсу и настраивает обработку кликов по строкам таблиц
        startClock();
        loadAllMenus(); // загружает актуальный список блюд из БД и раскладывает их по вкладкам
        updateTotal(); // обновляет итоговую сумму чека на экране
    }

    // ЗАГРУЗКА И НАСТРОЙКА ФОНА
    private void setupBackground() {
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
        if (Main.currentUser != null && waiterNameLabel != null) {// вход и метка
            waiterNameLabel.setText("Официант " + Main.currentUser.getFio());
        }
    }

    // настраиваем колонки корзины
    private void setupTableColumns() {
        if (dishColumn != null) {
            dishColumn.setCellValueFactory(new PropertyValueFactory<>("dishName"));
            qtyColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
            priceColumn.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));

            // выводим цену в таблице чека
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
        if (orderTableView != null) orderTableView.setItems(currentOrder); // привязываем нашу корзину к таблице
    }

    private void setupReceiptAndTables() {
        currentReceiptNumber = DatabaseHandler.getNextReceiptNumber(); // запрашиваем из БД следующий свободный номер чека
        if (receiptNumberLabel != null) receiptNumberLabel.setText("Чек №" + currentReceiptNumber); // показываем номер на экране

        if (tableComboBox != null) {
            tableComboBox.setItems(FXCollections.observableArrayList("1", "2", "3", "4", "5")); // создаем список доступных столиков и засовываем в ComboBox
            tableComboBox.getSelectionModel().selectFirst(); // по умолчанию выбираем первый столик
        }
    }

    private void loadAllMenus() {
        loadMenuToPane(hotTilePane, "горячее");
        loadMenuToPane(saladTilePane, "салаты");
        loadMenuToPane(drinkTilePane, "напитки");
        loadMenuToPane(dessertTilePane, "десерты");
    }

    private void loadMenuToPane(TilePane pane, String category) { // загрузка блюд определенной категории на указанную панель
        if (pane == null) return;
        pane.getChildren().clear(); //  очищаем от старых карточек блюд перед новой загрузкой
        pane.setAlignment(Pos.TOP_CENTER);
        pane.setPrefTileWidth(290.0);

        // отступы между карточками
        pane.setHgap(20.0); // горизонталь
        pane.setVgap(20.0); // вертикаль

        for (MenuItem item : DatabaseHandler.getMenuByCategory(category)) { // для каждого блюда создаем свою кнопку на экране
            Button btn = createDishCard(item); // создаем внешний вид карточки
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

        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(); // создаем маску для обрезки вылезающих углов карточки
        clip.setArcWidth(20); // задаем радиус скругления по ширине
        clip.setArcHeight(20); // задаем радиус скругления по высоте
        clip.widthProperty().bind(card.widthProperty());
        clip.heightProperty().bind(card.heightProperty());
        card.setClip(clip); // применяем готовую маску

        Region imgRegion = new Region(); // пустой холст для фотографии
        imgRegion.prefWidthProperty().bind(card.widthProperty());
        imgRegion.prefHeightProperty().bind(imgRegion.prefWidthProperty());
        imgRegion.setMinHeight(Region.USE_PREF_SIZE);
        imgRegion.setMaxHeight(Region.USE_PREF_SIZE);

        // поиск картинки
        java.net.URL imgResource = getClass().getResource("/org/example/lab14/" + item.getName() + ".jpg");
        if (imgResource != null) { // если картинка есть в папке

            // формируем путь к картинке по названию блюда
            String imgUrl = imgResource.toExternalForm();
            imgRegion.setStyle("-fx-background-image: url('" + imgUrl + "'); -fx-background-size: cover; -fx-background-position: center; -fx-background-repeat: no-repeat;");
        } else {
            imgRegion.setStyle("-fx-background-color: #E0E0E0;");
        }
        // создаем текстовую метку для названия блюда
        Label nameLbl = new Label(item.getName());
        nameLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-wrap-text: true; -fx-alignment: center; -fx-text-fill: #222222;");

        // цена
        String priceStr = String.format(Locale.US, "%.2f", item.getPrice());
        Label priceLbl = new Label(priceStr + " руб");
        priceLbl.setStyle("-fx-font-size: 16px; -fx-text-fill: #1dc3f5; -fx-font-weight: bold;");

        // текстовая информация о блюде
        VBox mainTextBlock = new VBox(6, nameLbl, priceLbl);
        mainTextBlock.setAlignment(Pos.CENTER);
        mainTextBlock.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 12; -fx-border-color: #DDDDDD; -fx-border-width: 1 0 0 0;");

        String sostavText = (item.getSostav() != null && !item.getSostav().isEmpty()) ? item.getSostav() : "Состав не указан";

        Label sostavLbl = new Label(sostavText); // создаем метку для состава
        sostavLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #888888; -fx-wrap-text: true; -fx-text-alignment: center;");

        sostavLbl.maxWidthProperty().bind(card.widthProperty().subtract(24));

        VBox sostavBlock = new VBox(sostavLbl); // кладем состав в отдельный нижний контейнер
        sostavBlock.setAlignment(Pos.CENTER);
        sostavBlock.setStyle("-fx-background-color: white; -fx-padding: 10; -fx-border-color: #DDDDDD; -fx-border-width: 1 0 0 0;");

        card.getChildren().addAll(imgRegion, mainTextBlock, sostavBlock); // собираем карточку
        btn.setGraphic(card); // карточка стала кнопкой
        return btn;
    }

    private void addToCart(MenuItem menuItem) { // добавление блюда в чек
        for (CartItem cartItem : currentOrder) { // есть ли уже такое блюдо в корзине?
            if (cartItem.getMenuItem().getId() == menuItem.getId()) { // если ID совпадают
                cartItem.increaseQuantity(); // то увеличиваем количество
                if (orderTableView != null) orderTableView.refresh(); // обновляем визуальную таблицу
                updateTotal(); // пересчитываем итоговую сумму
                return;
            }
        }
        currentOrder.add(new CartItem(menuItem));
        updateTotal(); // пересчитываем сумму
    }

    private void updateTotal() {
        // считаем сумму всех позиций в корзине
        double total = currentOrder.stream().mapToDouble(CartItem::getTotalPrice).sum();
        if (totalLabel != null) {
            // выводим с 2 знаками после запятой
            totalLabel.setText(String.format(Locale.US, "%.2f руб", total));
        }
    }

    @FXML void increaseItem(ActionEvent event) { modifyCartItem(1); }
    @FXML void decreaseItem(ActionEvent event) { modifyCartItem(-1); }
    @FXML void removeItem(ActionEvent event) { modifyCartItem(0); }

    private void modifyCartItem(int action) { // работа с корзиной
        if (orderTableView == null) return;
        CartItem selected = orderTableView.getSelectionModel().getSelectedItem(); // какая строка выделена
        if (selected == null) return;

        if (action == 1) selected.increaseQuantity(); // увеличиваем количество
        else if (action == -1) selected.decreaseQuantity(); // уменьшаем количество
        else currentOrder.remove(selected); // полностью удаляем позицию из чека

        orderTableView.refresh();
        updateTotal();
    }

    // КНОПКА ОПЛАТИТЬ
    @FXML void processPayment(ActionEvent event) {
        if (currentOrder.isEmpty()) {
            showAlert("Ошибка", "Заказ пуст. Добавьте блюда перед оплатой.", Alert.AlertType.WARNING);
            return;
        }
        // берем текущее системное время
        LocalDateTime now = LocalDateTime.now();
        String date = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")); // форматируем дату
        String time = now.format(DateTimeFormatter.ofPattern("HH:mm:ss")); // форматируем время

        // сумма
        double total = Double.parseDouble(totalLabel.getText().replace(" руб", "").replace(",", "."));
        int tableNum = Integer.parseInt(tableComboBox.getValue());
        String waiterName = (Main.currentUser != null) ? Main.currentUser.getFio() : "Неизвестный"; // берем фио текущего официанта

        int orderId = DatabaseHandler.createOrder(date, time, total, waiterName, currentReceiptNumber, tableNum); // делаем запись в БД

        if (orderId != -1) { // если ID чека получен
            DatabaseHandler.saveOrderItems(orderId, currentOrder); // привязываем их к этому ID чека
            showAlert("Успех", "Заказ успешно оплачен! (Чек №" + currentReceiptNumber + ", Стол №" + tableNum + ")", Alert.AlertType.INFORMATION);
            currentOrder.clear();
            updateTotal();
            currentReceiptNumber++; // увеличиваем счетчик номеров чека
            if (receiptNumberLabel != null) receiptNumberLabel.setText("Чек №" + currentReceiptNumber); // Показываем новый номер
        } else {
            showAlert("Ошибка БД", "Не удалось сохранить заказ.", Alert.AlertType.ERROR);
        }
    }
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title); // текст заголовка
        alert.setHeaderText(null); // отключаем стандартный подзаголовок внутри окна
        alert.setContentText(content); // задаем основной текст сообщения
        alert.showAndWait(); // показываем окно на экране и полностью блокируем интерфейс приложения до тех пор, пока пользователь не нажмет ок
    }

    // ЧАСЫ
    private void startClock() {
        if (dateTimeLabel == null) return;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy 'г.', HH:mm", new Locale("ru")); // Шаблон
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> { // таймер: мгновенное выполнение при старте
            String formattedDate = LocalDateTime.now().format(formatter); // берем время и форматируем
            dateTimeLabel.setText(formattedDate); // пишем на экран
        }), new KeyFrame(Duration.seconds(1))); // повторяем кадр каждую 1 секунду
        clock.setCycleCount(Animation.INDEFINITE); // крутить бесконечно
        clock.play();
    }
    // КНОПКА ВЫХОД
    @FXML void logout(ActionEvent event) {
        Main.currentUser = null; // Сбрасываем сессию
        Main.setRoot("/org/example/lab14/Prototip.fxml"); // возвращаем экран логина
    }
}