package org.example.lab14.models;

//ВСЕ ЧЕКИ
public class Order {
    private int id;
    private String date;
    private String time;
    private double totalPrice;
    private String waiter;
    private int checkNum;
    private int tableNum;

    public Order(int id, String date, String time, double totalPrice, String waiter, int checkNum, int tableNum) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.totalPrice = totalPrice;
        this.waiter = waiter;
        this.checkNum = checkNum;
        this.tableNum = tableNum;
    }

    public int getId() { return id; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public double getTotalPrice() { return totalPrice; }
    public String getWaiter() { return waiter; }
    public int getCheckNum() { return checkNum; }
    public int getTableNum() { return tableNum; }
}