package org.example.lab14.db;

import java.io.OutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    public static final String DB_URL = "jdbc:sqlite:D:/Dekstop/bdTasteWorld.db";

    public static void main(String[] args) {
        try {
            getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        PrintStream originalErr = System.err;
        Connection conn = null;

        try {
            System.setErr(new PrintStream(new OutputStream() {
                @Override
                public void write(int b) {}
            }));

            Class.forName("org.sqlite.JDBC");

            conn = DriverManager.getConnection(DB_URL);
            System.out.println("Соединение с базой данных успешно выполнено!");

        } catch (ClassNotFoundException e) {
            System.setErr(originalErr);
            System.err.println("Не удалось загрузить драйвер SQLite: " + e.getMessage());
        } catch (SQLException e) {
            System.setErr(originalErr);
            System.err.println("Ошибка при подключении к базе данных: " + e.getMessage());
            throw e;
        } finally {
            System.setErr(originalErr);
        }

        return conn;
    }
}