package org.example.lab14.models;

public class Employee {
    private int id;
    private String fio;
    private String data;
    private String telefon;

    public Employee(int id, String fio, String data, String telefon) {
        this.id = id;
        this.fio = fio;
        this.data = data;
        this.telefon = telefon;
    }

    public int getId() { return id; }
    public String getFio() { return fio; }
    public String getData() { return data; }
    public String getTelefon() { return telefon; }
}