package edu.mci.foodorderbuddy.it.elements;

import edu.mci.foodorderbuddy.data.entity.Cart;
import edu.mci.foodorderbuddy.data.entity.CartItem;
import edu.mci.foodorderbuddy.data.entity.Menu;
import edu.mci.foodorderbuddy.data.repository.CartItemRepository;
import edu.mci.foodorderbuddy.data.repository.CartRepository;
import edu.mci.foodorderbuddy.data.repository.PersonRepository;
import edu.mci.foodorderbuddy.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @InjectMocks
    private CartService cartService;

    // ---- Tests für calculateTotalPrice() (öffentliche Methode) ----
    @Test
    public void testCalculateTotalPrice_WithCartItems() {
        // Arrange
        Cart cart = new Cart();
        Menu pizza = createMenu("Pizza", 10.0);
        Menu salad = createMenu("Salad", 5.0);

        cart.setCartItems(List.of(
                new CartItem(cart, pizza, 2), // 20€
                new CartItem(cart, salad, 3)  // 15€
        ));

        // Act
        double total = cartService.calculateTotalPrice(cart);

        // Assert
        assertEquals(35.0, total);
    }

    @Test
    public void testCalculateTotalPrice_EmptyCart() {
        assertEquals(0.0, cartService.calculateTotalPrice(new Cart()));
    }

    // ---- Tests für validatePaymentDetails() (private Methode mit Reflection) ----
    @Test
    public void testValidatePaymentDetails_ValidVisaCard() throws Exception {
        // Arrange
        String validCard = "4111111111111111";
        String validExpiry = "12/25";
        String validCvv = "123";

        // Reflection: Zugriff auf private Methode
        Method method = CartService.class.getDeclaredMethod(
                "validatePaymentDetails",
                String.class, String.class, String.class
        );
        method.setAccessible(true);

        // Act
        boolean isValid = (boolean) method.invoke(
                cartService,
                validCard, validExpiry, validCvv
        );

        // Assert
        assertTrue(isValid);
    }

    @Test
    public void testValidatePaymentDetails_InvalidCardNumber() throws Exception {
        // Arrange
        String invalidCard = "1234";
        String validExpiry = "12/25";
        String validCvv = "123";

        Method method = CartService.class.getDeclaredMethod(
                "validatePaymentDetails",
                String.class, String.class, String.class
        );
        method.setAccessible(true);

        // Act
        boolean isValid = (boolean) method.invoke(
                cartService,
                invalidCard, validExpiry, validCvv
        );

        // Assert
        assertFalse(isValid);
    }

    // ---- Hilfsmethode ----
    private Menu createMenu(String name, double price) {
        Menu menu = new Menu();
        menu.setMenuTitle(name);
        menu.setMenuPrice(price);
        return menu;
    }
}
