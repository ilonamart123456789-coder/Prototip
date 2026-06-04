package org.example.lab14.db;
import org.example.lab14.models.*;
import java.sql.*;
import java.util.*;

public class DatabaseHandler {

    public static Connection getConnection() throws SQLException {
        return DatabaseConnection.getConnection();
    }

    //АВТОРИЗАЦИЯ И ПОЛЬЗОВАТЕЛИ
    public static User authenticate(String login, String password) {
        String query = "SELECT * FROM Sotrudniki WHERE login = ? AND parol = ?";
        try (Connection conn = getConnection(); PreparedStatement pst = conn.prepareStatement(query)) { // Открываем подключение и передаем sql запрос
            pst.setString(1, login); pst.setString(2, password);
            ResultSet rs = pst.executeQuery(); // отправка и получение запроса
            if (rs.next()) return new User(rs.getInt("id"), rs.getString("fio"), rs.getString("login"), rs.getString("role")); // Если найден, создаем и возвращаем объект User
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public static List<String> getWaitersList() { // получение списка имен всех официантов
        List<String> list = new ArrayList<>();
        try (Connection conn = getConnection(); Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT DISTINCT TRIM(fio) as fio FROM Sotrudniki WHERE LOWER(role) != 'admin' AND LOWER(fio) != 'admin'")) {
            while (rs.next()) list.add(rs.getString("fio"));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    //УПРАВЛЕНИЕ СОТРУДНИКАМИ (ДЛЯ АДМИНА)
    public static List<Employee> getAllEmployees() { // получение полных данных всех сотрудников
        List<Employee> list = new ArrayList<>();
        try (Connection conn = getConnection(); Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT id, fio, data, telefon FROM Sotrudniki WHERE LOWER(role) != 'admin' AND LOWER(fio) != 'admin'")) {
            while (rs.next()) list.add(new Employee(rs.getInt("id"), rs.getString("fio"), rs.getString("data"), rs.getString("telefon")));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public static boolean addEmployee(String fio, String data, String telefon) { //регистрация нового сотрудника
        String query = "INSERT INTO Sotrudniki (fio, data, telefon, login, parol, role) VALUES (?, ?, ?, ?, '123', 'sotrudnik')";
        try (Connection conn = getConnection(); PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, fio); pst.setString(2, data); pst.setString(3, telefon); pst.setString(4, "user" + (int)(Math.random() * 100));
            return pst.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public static boolean updateEmployee(int id, String fio, String data, String telefon) { //изменение данных сотрудника
        String query = "UPDATE Sotrudniki SET fio = ?, data = ?, telefon = ? WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, fio); pst.setString(2, data); pst.setString(3, telefon); pst.setInt(4, id);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public static boolean deleteEmployee(int id) { //удаление сотрудника
        try (Connection conn = getConnection(); PreparedStatement pst = conn.prepareStatement("DELETE FROM Sotrudniki WHERE id = ?")) {
            pst.setInt(1, id); return pst.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    //ГРАФИКИ
    public static Map<String, Map<String, double[]>> getAllWaitersStatsByMonth() {// Возвращает статистику всех официантов по месяцам
        Map<String, Map<String, double[]>> allStats = new LinkedHashMap<>(); //Создает пустую карту для хранения статистики
        for (String w : getWaitersList()) { // Перебираем имена всех официантов
            Map<String, double[]> stats = new LinkedHashMap<>(); // Создает личную карту статистики текущего официанта
            stats.put("04", new double[]{0.0, 0.0}); stats.put("05", new double[]{0.0, 0.0}); //стартовые показатели для апреля и мая
            allStats.put(w, stats);
        }

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT strftime('%m', data) as month, TRIM(fio) as fio, COUNT(id) as count, SUM(total_price) as total FROM Orders GROUP BY month, TRIM(fio)")) {
            while (rs.next()) {
                String month = rs.getString("month"), fio = rs.getString("fio"); // Извлекаем из текущей строки номер месяца и ФИО
                if (allStats.containsKey(fio) && allStats.get(fio).containsKey(month)) {
                    allStats.get(fio).put(month, new double[]{rs.getDouble("count"), rs.getDouble("total")});// выбирает месяц и записывает массив с реальным количеством чеков и суммой из БД
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return allStats;
    }

    //МЕНЮ И ЗАКАЗЫ
    public static List<MenuItem> getMenuByCategory(String category) {
        List<MenuItem> menu = new ArrayList<>();
        try (Connection conn = getConnection(); PreparedStatement pst = conn.prepareStatement("SELECT * FROM Menu WHERE category = ?")) {
            pst.setString(1, category);
            try (ResultSet rs = pst.executeQuery()) { // Выполняем готовый запрос и получаем результат в виде таблицы
                while (rs.next()) menu.add(new MenuItem(rs.getInt("id"), rs.getString("name"), rs.getDouble("price"), rs.getString("category"), rs.getInt("prodan"), rs.getString("sostav")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return menu;
    }

    public static List<MenuItem> getAllMenu() {
        List<MenuItem> menu = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT * FROM Menu")) {
            while (rs.next()) menu.add(new MenuItem(rs.getInt("id"), rs.getString("name"), rs.getDouble("price"), rs.getString("category"), rs.getInt("prodan"), rs.getString("sostav")));
        } catch (SQLException e) { e.printStackTrace(); }
        return menu;
    }

    public static boolean addMenuItem(String name, String category, String sostav, double price) {
        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement("INSERT INTO menu (name, category, sostav, price, prodan) VALUES (?, ?, ?, ?, 0)")) {
            pst.setString(1, name); pst.setString(2, category); pst.setString(3, sostav); pst.setDouble(4, price);
            return pst.executeUpdate() > 0; // Если изменено больше 0 строк, то добавляем
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public static boolean deleteMenuItem(int id) { //полное удаление блюда по его ID
        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement("DELETE FROM Menu WHERE id = ?")) {
            pst.setInt(1, id); return pst.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public static int getNextReceiptNumber() { //вычисление порядкового номера следующего заказа
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT MAX(\"check\") FROM Orders")) {
            if (rs.next()) return rs.getInt(1) + 1;
        } catch (SQLException e) { e.printStackTrace(); }
        return 1;
    }

    public static int createOrder(String date, String time, double total, String fio, int checkNum, int tableNum) { //сохранение главной информации чека по ID
        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement("INSERT INTO Orders (data, acceptance_time, total_price, fio, \"check\", \"table\") VALUES (?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, date); pst.setString(2, time); pst.setDouble(3, total); pst.setString(4, fio); pst.setInt(5, checkNum); pst.setInt(6, tableNum); // Аккуратно расставляем все 6 параметров (дата, время, сумма, кассир, номер чека, столик) по плейсхолдерам
            pst.executeUpdate(); //сохраняем строку в таблицу Orders
            try (ResultSet keys = pst.getGeneratedKeys()) { if (keys.next()) return keys.getInt(1); }
        } catch (SQLException e) { e.printStackTrace(); }
        return -1;
    }

    public static void saveOrderItems(int orderId, List<CartItem> items) { //сохранение позиций заказа и связь блюда с ID чека
        try (Connection conn = getConnection(); // Открываем подключение к базе
             PreparedStatement insertPst = conn.prepareStatement("INSERT INTO Order_Items (order_id, menu_id, quantity) VALUES (?, ?, ?)");
             PreparedStatement updatePst = conn.prepareStatement("UPDATE Menu SET prodan = COALESCE(prodan, 0) + ? WHERE id = ?")) { // Увеличение счетчика продано
            conn.setAutoCommit(false); // отключаем автосохранение
            for (CartItem item : items) { //перебор блюд в корзине
                insertPst.setInt(1, orderId); insertPst.setInt(2, item.getMenuItem().getId()); insertPst.setInt(3, item.getQuantity()); //ID чека, ID блюда и количество купленных порций
                insertPst.addBatch(); //пакет команд

                updatePst.setInt(1, item.getQuantity()); updatePst.setInt(2, item.getMenuItem().getId()); // количество порций и ID блюда
                updatePst.addBatch();
            }
            insertPst.executeBatch(); updatePst.executeBatch(); //полностью обновляем меню
            conn.commit(); //сохраняем
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static List<Order> getAllOrders() { // выгружаем историю чеков
        List<Order> list = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT id, data, acceptance_time, total_price, fio, \"check\", \"table\" FROM Orders ORDER BY id DESC")) {
            while (rs.next()) list.add(new Order(rs.getInt("id"), rs.getString("data"), rs.getString("acceptance_time"), rs.getDouble("total_price"), rs.getString("fio"), rs.getInt("check"), rs.getInt("table")));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public static List<AdminOrderItem> getOrderItemsByOrderId(int orderId) { //что куплено в чеке,видно при нажатии
        List<AdminOrderItem> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement("SELECT m.name, m.price, oi.quantity FROM Order_Items oi JOIN Menu m ON oi.menu_id = m.id WHERE oi.order_id = ?")) {
            pst.setInt(1, orderId); // Подставляем номер нужного нам чека в условие поиска
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) list.add(new AdminOrderItem(rs.getString("name"), rs.getDouble("price"), rs.getInt("quantity")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}