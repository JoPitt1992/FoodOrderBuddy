package edu.mci.foodorderbuddy.data.entity;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "cart_cookie")
public class CartCookie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cookie_id")
    private Long cookieId;

    @Column(name = "cookie_value", nullable = false, unique = true)
    private String cookieValue;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "expires_at", nullable = false)
    private Date expiresAt;

    @OneToOne
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    public CartCookie() {}

    public CartCookie(String cookieValue, Date expiresAt, Cart cart) {
        this.cookieValue = cookieValue;
        this.expiresAt = expiresAt;
        this.cart = cart;
    }

    public Long getCookieId() { return cookieId; }
    public String getCookieValue() { return cookieValue; }
    public Date getExpiresAt() { return expiresAt; }
    public Cart getCart() { return cart; }

    public void setCookieValue(String cookieValue) { this.cookieValue = cookieValue; }
    public void setExpiresAt(Date expiresAt) { this.expiresAt = expiresAt; }
    public void setCart(Cart cart) { this.cart = cart; }
}