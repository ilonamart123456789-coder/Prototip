package org.example.lab14.models;

//ХРАНЕНИЕ 1 ПОЗИЦИИ В КОРЗИНЕ
public class CartItem {
    //конкретное блюдо
    private MenuItem menuItem;
    // Количество заказанных порций именно этого блюда в текущем чеке
    private int quantity;

    //ПРИ НАЖАТИИ
    public CartItem(MenuItem menuItem) {
        this.menuItem = menuItem;
        this.quantity = 1;
    }
    public MenuItem getMenuItem() { return menuItem; }
    public int getQuantity() { return quantity; }

    //ИМЯ БЛЮДА
    public String getDishName() {
        return menuItem.getName();
    }

    //ДЛЯ СУММЫ
    public double getTotalPrice() {
        return menuItem.getPrice() * quantity;
    }

    //КОЛИЧЕСТВО ПОРЦИЙ
    public void increaseQuantity() {
        this.quantity++;
    }

    public void decreaseQuantity() {
        if (this.quantity > 1) {
            this.quantity--;
        }
    }
    @Override
    public String toString() {
        return menuItem.getName() + " x" + quantity + " = " + getTotalPrice() + " руб.";
    }
}