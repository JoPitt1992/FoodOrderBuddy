package edu.mci.foodorderbuddy.data.entity;

import jakarta.persistence.*;

import java.util.Date;
import java.util.List;


@Entity
@Table(name = "cart")
public class Cart {
    @Column(name = "cart_id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cartId;

    @ManyToMany
    @JoinTable(
            name = "cart_menu",
            joinColumns = @JoinColumn(name = "cart_id"),
            inverseJoinColumns = @JoinColumn(name = "menu_id")
    )
    private List<Menu> cartList;

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

    @OneToOne(mappedBy = "cart", cascade = CascadeType.ALL)
    private CartCookie cartCookie;

    public Cart(){}

    public Long getCartId() {return cartId; }
    public List<Menu> getCartList() {return cartList; }
    public OrderHistory getOrderhistory() {return orderhistory; }
    public Double getCartPrice() {return cartPrice; }
    public Boolean getCartPayed() {return cartPayed; }
    public Date getCartPaydate() {return cartPaydate; }
    public Boolean getCartDelivered() {return cartDelivered; }
    public CartCookie getCartCookie() {return cartCookie; }

    public void setCartList(List<Menu> cartList) {this.cartList = cartList; }
    public void setOrderhistory(OrderHistory orderhistory) {this.orderhistory = orderhistory; }
    public void setCartPrice(Double cartPrice) {this.cartPrice = cartPrice; }
    public void setCartPayed(Boolean cartPayed) {this.cartPayed = cartPayed; }
    public void setCartPaydate(Date cartPaydate) {this.cartPaydate = cartPaydate; }
    public void setCartDelivered(Boolean cartDelivered) {this.cartDelivered = cartDelivered; }
    public void setCartCookie(CartCookie cartCookie) {this.cartCookie = cartCookie; }
}
