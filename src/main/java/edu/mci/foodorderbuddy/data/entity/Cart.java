package edu.mci.foodorderbuddy.data.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@Entity
@Table(name = "cart")
public class Cart {
    @Column(name = "cart_id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cartId;

    // Beziehung zu CartItem - ein Warenkorb hat mehrere CartItems
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<CartItem> cartItems = new ArrayList<>();

    // Behalten wir für Abwärtskompatibilität
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinTable(
            name = "cart_menu",
            joinColumns = @JoinColumn(name = "cart_id"),
            inverseJoinColumns = @JoinColumn(name = "menu_id")
    )
    private List<Menu> cartList = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "orderhistory_id")
    private OrderHistory orderhistory;

    @Column(name = "cart_price", nullable = false)
    private Double cartPrice;

    @Column(name = "cart_payed")
    private Boolean cartPayed;

    @Column(name = "cart_paydate")
    private Date cartPaydate;

    @Enumerated(EnumType.STRING)
    @Column(name = "cart_order_status")
    private OrderStatus cartOrderStatus;

    @ManyToOne
    @JoinColumn(name = "person_id")
    private Person owner;

    // Neue Felder für Zahlungsreferenz und Zahlungsmethode
    @Column(name = "payment_reference")
    private String paymentReference;

    @Column(name = "payment_method")
    private String paymentMethod;

    public Cart(){}

    public Long getCartId() {return cartId; }

    // Neue Getter und Setter für CartItems
    public List<CartItem> getCartItems() {return cartItems; }
    public void setCartItems(List<CartItem> cartItems) {this.cartItems = cartItems; }

    // Hilfsmethoden für die einfache Menüverwaltung
    public void addCartItem(CartItem item) {
        cartItems.add(item);
        item.setCart(this);
    }

    public void removeCartItem(CartItem item) {
        cartItems.remove(item);
        item.setCart(null);
    }

    public CartItem findCartItemByMenu(Menu menu) {
        return cartItems.stream()
                .filter(item -> item.getMenu().getMenuId().equals(menu.getMenuId()))
                .findFirst()
                .orElse(null);
    }

    // Für Rückwärtskompatibilität: Stellt die Menü-Liste ohne Mengenangaben bereit
    public List<Menu> getCartList() {
        if (!cartItems.isEmpty()) {
            return cartItems.stream()
                    .map(CartItem::getMenu)
                    .collect(Collectors.toList());
        }
        return cartList;
    }

    public void setCartList(List<Menu> cartList) {this.cartList = cartList; }
    public OrderHistory getOrderHistory() {return orderhistory; }
    public Double getCartPrice() {return cartPrice; }
    public Boolean getCartPayed() {return cartPayed; }
    public Date getCartPaydate() {return cartPaydate; }
    public OrderStatus getCartOrderStatus() {return cartOrderStatus; }
    public Person getOwner() {return owner; }
    public String getPaymentReference() {return paymentReference; }
    public String getPaymentMethod() {return paymentMethod; }

    public void setOrderHistory(OrderHistory orderhistory) {this.orderhistory = orderhistory; }
    public void setCartPrice(Double cartPrice) {this.cartPrice = cartPrice; }
    public void setCartPayed(Boolean cartPayed) {this.cartPayed = cartPayed; }
    public void setCartPaydate(Date cartPaydate) {this.cartPaydate = cartPaydate; }
    public void setCartOrderStatus(OrderStatus cartOrderStatus) {this.cartOrderStatus = cartOrderStatus;}
    public void setOwner(Person owner) {this.owner = owner; }
    public void setPaymentReference(String paymentReference) {this.paymentReference = paymentReference; }
    public void setPaymentMethod(String paymentMethod) {this.paymentMethod = paymentMethod; }
}