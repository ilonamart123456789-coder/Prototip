package org.example.lab14.models;

public class MenuItem {
    private int id;
    private String name;
    private double price;
    private String category;
    private int prodan;
    private String sostav;

    public MenuItem(int id, String name, double price, String category, int prodan, String sostav) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
        this.prodan = prodan;
        this.sostav = sostav;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getProdan() { return prodan; }
    public void setProdan(int prodan) { this.prodan = prodan; }

    public String getSostav() { return sostav; }
    public void setSostav(String sostav) { this.sostav = sostav; }
}