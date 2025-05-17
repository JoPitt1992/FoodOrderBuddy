package edu.mci.foodorderbuddy.data.entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "order_history")

public class OrderHistory {
    @Column(name = "orderhistory_id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderhistoryId;

    @OneToOne
    @JoinColumn(name = "person_id", nullable = false, unique = true)
    private Person person;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "orderhistory_id")
    private List<Cart> carts;

    public OrderHistory() {}

    public Long getOrderhistoryId() {return orderhistoryId;}
    public Person getPerson() {return person;}
    public List<Cart> getCarts() {return carts;}

    public void setPerson(Person person) {this.person = person; }
    public void setCarts(List<Cart> carts) {this.carts = carts;}
    public void addCart(Cart cart) {carts.add(cart); cart.setOrderHistory(this); }
}
