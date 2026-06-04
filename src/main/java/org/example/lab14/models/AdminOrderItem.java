package org.example.lab14.models;

//СОСТАВ ЗАКАЗА
public class AdminOrderItem {
    private String name;
    private double price;
    private int quantity;

    public AdminOrderItem(String name, double price, int quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public double getTotal() { return price * quantity; }
}