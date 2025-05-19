package edu.mci.foodorderbuddy.data.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "cart_item")
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cart_id")
    private Cart cart;

    @ManyToOne
    @JoinColumn(name = "menu_id")
    private Menu menu;

    @Column(name = "quantity")
    private Integer quantity = 1;

    public CartItem() {}

    // Konstruktor für einfachere Erstellung
    public CartItem(Cart cart, Menu menu, Integer quantity) {
        this.cart = cart;
        this.menu = menu;
        this.quantity = quantity;
    }

    // Getter und Setter
    public Long getId() { return id; }

    public Cart getCart() { return cart; }
    public void setCart(Cart cart) { this.cart = cart; }

    public Menu getMenu() { return menu; }
    public void setMenu(Menu menu) { this.menu = menu; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}