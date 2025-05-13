package edu.mci.foodorderbuddy.data.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;


@Entity
@Table(name = "menu")
public class Menu {
    @Column(name = "menu_id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long menuId;

    @Column(name = "menu_title", nullable = false)
    private String menuTitle;

    @Column(name = "menu_ingredients", nullable = false)
    private String menuIngredients;

    @Column(name = "menu_price", nullable = false)
    @Positive
    private Double menuPrice;

    @Column(name = "menu_daily")
    boolean menuDaily;

    public Menu(){}

    public Menu(String title, String ingredients, Double price, boolean daily) {
        this.menuTitle = title;
        this.menuIngredients = ingredients;
        this.menuPrice = price;
        this.menuDaily = daily;
    }

    public Long getMenuId() {return menuId; }
    public String getMenuTitle() {
        return menuTitle;
    }
    public String getMenuIngredients() {
        return menuIngredients;
    }
    public Double getMenuPrice() {
        return menuPrice;
    }
    public boolean isMenuDaily() {return menuDaily; }

    // menuId soll nicht neu gesetzt werden können
    public void setMenuTitle(String menuName) {
        this.menuTitle = menuName;
    }
    public void setMenuIngredients(String ingredients) {
        this.menuIngredients = ingredients;
    }
    public void setMenuPrice(Double menuPrice) {
        this.menuPrice = menuPrice;
    }

    // Methoden für Rolle Admin
    // addMenu()
    // editMenu()
    // deleneMenu()
    public void setMenuDaily(boolean menuDaily) {this.menuDaily = menuDaily;}

}
