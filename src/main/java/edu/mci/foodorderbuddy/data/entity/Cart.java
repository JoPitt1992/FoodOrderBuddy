package edu.mci.foodorderbuddy.data.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Entity
@Table(name = "cart")
public class Cart {
    @Column(name = "cart_id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cartId;

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

    @Column(name = "cart_delivered")
    private Boolean cartDelivered;

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
    public List<Menu> getCartList() {return cartList; }
    public OrderHistory getOrderHistory() {return orderhistory; }
    public Double getCartPrice() {return cartPrice; }
    public Boolean getCartPayed() {return cartPayed; }
    public Date getCartPaydate() {return cartPaydate; }
    public Boolean getCartDelivered() {return cartDelivered; }
    public Person getOwner() {return owner; }
    public String getPaymentReference() {return paymentReference; }
    public String getPaymentMethod() {return paymentMethod; }

    public void setCartList(List<Menu> cartList) {this.cartList = cartList; }
    public void setOrderHistory(OrderHistory orderhistory) {this.orderhistory = orderhistory; }
    public void setCartPrice(Double cartPrice) {this.cartPrice = cartPrice; }
    public void setCartPayed(Boolean cartPayed) {this.cartPayed = cartPayed; }
    public void setCartPaydate(Date cartPaydate) {this.cartPaydate = cartPaydate; }
    public void setCartDelivered(Boolean cartDelivered) {this.cartDelivered = cartDelivered; }
    public void setOwner(Person owner) {this.owner = owner; }
    public void setPaymentReference(String paymentReference) {this.paymentReference = paymentReference; }
    public void setPaymentMethod(String paymentMethod) {this.paymentMethod = paymentMethod; }
}