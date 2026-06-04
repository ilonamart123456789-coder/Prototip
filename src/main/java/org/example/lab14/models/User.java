package org.example.lab14.models;

//ПОЛЬЗОВАТЕЛЬ
public class User {
    private int id;
    private String fio;
    private String login;
    private String role;

    public User(int id, String fio, String login, String role) {
        this.id = id;
        this.fio = fio;
        this.login = login;
        this.role = role;
    }

    public int getId() { return id; }
    public String getFio() { return fio; }
    public String getRole() { return role; }
}