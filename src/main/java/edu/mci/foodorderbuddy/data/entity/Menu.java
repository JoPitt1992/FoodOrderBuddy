package edu.mci.foodorderbuddy.data.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;

//test
//Test Benni 2
@Entity
@Table
public class Menu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "menu_name", nullable = false)
    private String menuName;

    @Column(name = "menu_ingredients", nullable = false)
    private String menuIngredients;

    @Column(name = "menu_price")
    @Positive
    private Double menuPrice;

    @Positive
    private Integer version;


    public String getMenuName() {
        return menuName;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    public String getMenuIngredients() {
        return menuIngredients;
    }

    public void setMenuIngredients(String ingredients) {
        this.menuIngredients = ingredients;
    }

    public Double getMenuPrice() {
        return menuPrice;
    }

    public void setMenuPrice(Double menuPrice) {
        this.menuPrice = menuPrice;
    }

}
