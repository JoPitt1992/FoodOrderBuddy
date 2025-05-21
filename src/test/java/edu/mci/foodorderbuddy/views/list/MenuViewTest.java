package edu.mci.foodorderbuddy.views.list;

import com.vaadin.flow.component.button.Button;
import com.vaadin.testbench.unit.UIUnitTest;
import edu.mci.foodorderbuddy.security.SecurityService;
import edu.mci.foodorderbuddy.service.CartService;
import edu.mci.foodorderbuddy.service.MenuService;
import edu.mci.foodorderbuddy.views.MenuView;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@SpringBootTest
public class MenuViewTest extends UIUnitTest {

    @Test
    public void menuView_shouldHideAddButtonForRegularUser() {
        // Arrange: Nutzer mit USER-Rolle in den SecurityContext setzen
        UserDetails userDetails = User.withUsername("testuser")
                .password("password")
                .roles("USER")  // keine ADMIN-Rolle!
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );

        // Service-Mocks
        MenuService menuService = mock(MenuService.class);
        CartService cartService = mock(CartService.class);
        SecurityService securityService = mock(SecurityService.class);
        when(securityService.getAuthenticatedUser()).thenReturn(userDetails);

        // Act: MenuView wird instanziiert
        MenuView view = new MenuView(menuService, cartService, securityService);

        // Assert: Der Button sollte nicht vorhanden sein
        boolean containsAddButton = view.getToolbarForTesting().getChildren()
                .anyMatch(component -> component instanceof Button && ((Button) component).getText().contains("Menü hinzufügen"));

        assertFalse(containsAddButton, "User should not be able to see the add button");
    }

    @Test
    public void menuView_shouldShowAddButtonForAdminUser() {
        // Arrange: Nutzer mit ADMIN-Rolle in den SecurityContext setzen
        UserDetails userDetails = User.withUsername("adminuser")
                .password("password")
                .roles("ADMIN")  // ADMIN-Rolle!
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );

        // Service-Mocks
        MenuService menuService = mock(MenuService.class);
        CartService cartService = mock(CartService.class);
        SecurityService securityService = mock(SecurityService.class);
        when(securityService.getAuthenticatedUser()).thenReturn(userDetails);

        // Act: MenuView wird instanziiert
        MenuView view = new MenuView(menuService, cartService, securityService);

        // Assert: Der Button sollte vorhanden sein
        boolean containsAddButton = view.getToolbarForTesting().getChildren()
                .anyMatch(component -> component instanceof Button && ((Button) component).getText().contains("Menü hinzufügen"));

        assertTrue(containsAddButton, "Admin should be able to see the add button");
    }

}
