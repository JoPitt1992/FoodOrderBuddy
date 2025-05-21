package edu.mci.foodorderbuddy.entity;

import edu.mci.foodorderbuddy.data.entity.*;
import edu.mci.foodorderbuddy.data.repository.CartRepository;
import edu.mci.foodorderbuddy.data.repository.MenuRepository;
import edu.mci.foodorderbuddy.data.repository.PersonRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Transactional
public class CartEntityTest {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private MenuRepository menuRepository;

    @Test
    public void cart_shouldPersistWithCartItemsAndOwner() {
        Person p = new Person("Max", "Mustermann", "testuser", "max@example.com", "sicheresPasswort123");
        p.setPersonRole("ROLE_USER");
        personRepository.save(p);

        personRepository.save(p);

        // Setup: Menu speichern
        Menu menu = new Menu();
        menu.setMenuTitle("Pizza");
        menu.setMenuIngredients("Tomaten, Käse, Salami");
        menu.setMenuPrice(9.99);
        menu = menuRepository.save(menu);

        // Setup: CartItem
        CartItem item = new CartItem();
        item.setMenu(menu);
        item.setQuantity(2);

        // Setup: Cart
        Cart cart = new Cart();
        cart.setOwner(p);
        cart.setCartPrice(19.98);
        cart.setCartPayed(false);
        cart.setCartOrderStatus(OrderStatus.IN_BEARBEITUNG);
        cart.setCartPaydate(new Date());
        cart.setPaymentMethod("Kreditkarte");
        cart.setPaymentReference("ABC123");
        cart.addCartItem(item);

        // Save
        Cart savedCart = cartRepository.save(cart);

        // Assertions
        assertThat(savedCart.getCartId()).isNotNull();
        assertThat(savedCart.getCartItems()).hasSize(1);
        assertThat(savedCart.getOwner().getPersonUserName()).isEqualTo("testuser");
        assertThat(savedCart.getCartItems().get(0).getMenu().getMenuTitle()).isEqualTo("Pizza");
    }

    @Test
    public void cart_shouldRemoveOrphanCartItemsOnUpdate() {
        // Setup wie oben
        Cart cart = new Cart();
        cart.setCartPrice(0.0);
        CartItem item1 = new CartItem();
        item1.setQuantity(1);
        cart.addCartItem(item1);

        cart = cartRepository.save(cart);
        Long itemId = cart.getCartItems().get(0).getId();

        // Entferne Item
        cart.removeCartItem(item1);
        cart = cartRepository.save(cart);

        // Prüfe, ob es wirklich entfernt wurde
        Cart reloaded = cartRepository.findById(cart.getCartId()).orElseThrow();
        assertThat(reloaded.getCartItems()).isEmpty();
    }
}
