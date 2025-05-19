package edu.mci.foodorderbuddy.service;

import edu.mci.foodorderbuddy.data.entity.Cart;
import edu.mci.foodorderbuddy.data.entity.OrderHistory;
import edu.mci.foodorderbuddy.data.entity.Person;
import edu.mci.foodorderbuddy.data.repository.CartRepository;
import edu.mci.foodorderbuddy.data.repository.OrderHistoryRepository;
import edu.mci.foodorderbuddy.data.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class OrderHistoryService {

    private final OrderHistoryRepository orderHistoryRepository;
    private final PersonRepository personRepository;
    private final CartRepository cartRepository;

    @Autowired
    public OrderHistoryService(OrderHistoryRepository orderHistoryRepository,
                               PersonRepository personRepository,
                               CartRepository cartRepository) {
        this.orderHistoryRepository = orderHistoryRepository;
        this.personRepository = personRepository;
        this.cartRepository = cartRepository;
    }

    /**
     * Gibt die Bestellhistorie eines Benutzers zurück oder erstellt eine neue
     */
    public OrderHistory getOrCreateOrderHistoryForUser(String username) {
        Optional<Person> personOpt = personRepository.findByPersonUserName(username);

        if (personOpt.isPresent()) {
            Person person = personOpt.get();

            // OrderHistory suchen
            Optional<OrderHistory> orderHistoryOpt = orderHistoryRepository.findByPerson(person);

            if (orderHistoryOpt.isPresent()) {
                return orderHistoryOpt.get();
            } else {
                // Neue OrderHistory erstellen
                OrderHistory newHistory = new OrderHistory();
                newHistory.setPerson(person);
                newHistory.setCarts(new ArrayList<>());

                return orderHistoryRepository.save(newHistory);
            }
        }

        return null;
    }

    /**
     * Fügt einen bezahlten Warenkorb zur Bestellhistorie hinzu
     */
    public void addCartToOrderHistory(Cart cart) {
        if (cart == null || cart.getOwner() == null || !Boolean.TRUE.equals(cart.getCartPayed())) {
            return;
        }

        OrderHistory orderHistory = getOrCreateOrderHistoryForUser(cart.getOwner().getPersonUserName());

        if (orderHistory != null) {
            // Prüfen, ob der Warenkorb bereits in der Historie ist
            boolean cartAlreadyInHistory = orderHistory.getCarts().stream()
                    .anyMatch(existingCart -> existingCart.getCartId().equals(cart.getCartId()));

            if (!cartAlreadyInHistory) {
                orderHistory.addCart(cart);
                orderHistoryRepository.save(orderHistory);
            }
        }
    }

    /**
     * Gibt alle bezahlten Warenkörbe eines Benutzers zurück
     */
    public List<Cart> getPaidCartsForUser(String username) {
        OrderHistory history = getOrCreateOrderHistoryForUser(username);

        if (history != null && history.getCarts() != null) {
            List<Cart> paidCarts = history.getCarts().stream()
                    .filter(cart -> Boolean.TRUE.equals(cart.getCartPayed()))
                    .sorted(Comparator.comparing(Cart::getCartPaydate).reversed()) // Neueste zuerst
                    .toList();

            return paidCarts;
        }

        return Collections.emptyList();
    }

    /**
     * Findet einen spezifischen Warenkorb nach ID
     */
    public Optional<Cart> getCartById(Long cartId) {
        return cartRepository.findById(cartId);
    }

    /**
     * Markiert einen Warenkorb als geliefert
     */
    public void markCartAsDelivered(Long cartId) {
        Optional<Cart> cartOpt = cartRepository.findById(cartId);

        if (cartOpt.isPresent()) {
            Cart cart = cartOpt.get();
            cart.setCartDelivered(true);
            cartRepository.save(cart);
        }
    }

    /**
     * Filtert Bestellungen nach Suchbegriff
     */
    public List<Cart> searchOrdersByTerm(String username, String searchTerm) {
        if (searchTerm == null || searchTerm.isEmpty()) {
            return getPaidCartsForUser(username);
        }

        String lowerTerm = searchTerm.toLowerCase();

        return getPaidCartsForUser(username).stream()
                .filter(cart ->
                        (cart.getPaymentReference() != null &&
                                cart.getPaymentReference().toLowerCase().contains(lowerTerm)) ||
                                (cart.getPaymentMethod() != null &&
                                        cart.getPaymentMethod().toLowerCase().contains(lowerTerm)) ||
                                cart.getCartItems().stream().anyMatch(item ->
                                        item.getMenu().getMenuTitle().toLowerCase().contains(lowerTerm) ||
                                                item.getMenu().getMenuIngredients().toLowerCase().contains(lowerTerm))
                )
                .toList();
    }
}