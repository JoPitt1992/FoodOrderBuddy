package edu.mci.foodorderbuddy.views;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import edu.mci.foodorderbuddy.data.entity.Cart;
import edu.mci.foodorderbuddy.data.entity.Menu;
import edu.mci.foodorderbuddy.data.entity.Person;
import edu.mci.foodorderbuddy.security.SecurityService;
import edu.mci.foodorderbuddy.service.CartService;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.core.userdetails.UserDetails;

import java.text.NumberFormat;
import java.util.Locale;

@RolesAllowed({"ROLE_USER", "ROLE_ADMIN"})
@PageTitle("Warenkorb | Food Order Buddy")
@Route(value = "cart", layout = MainLayout.class)
public class CartView extends VerticalLayout {

    private Grid<Menu> menuGrid = new Grid<>(Menu.class);
    private final CartService cartService;
    private final SecurityService securityService;

    private Cart currentCart;
    private Span totalPriceLabel;

    public CartView(CartService cartService, SecurityService securityService) {
        this.cartService = cartService;
        this.securityService = securityService;

        addClassName("cart-view");
        setSizeFull();

        configureGrid();

        H3 title = new H3("Mein Warenkorb");

        // Gesamtpreis-Anzeige
        HorizontalLayout totalLayout = new HorizontalLayout();
        totalLayout.setWidthFull();
        totalLayout.setJustifyContentMode(JustifyContentMode.END);

        Span totalLabel = new Span("Gesamtbetrag: ");
        totalLabel.getStyle().set("font-weight", "bold");

        totalPriceLabel = new Span("0,00 €");
        totalPriceLabel.getStyle().set("font-weight", "bold");

        totalLayout.add(totalLabel, totalPriceLabel);

        Button checkoutButton = createCheckoutButton();

        VerticalLayout contentLayout = new VerticalLayout(
                title, menuGrid, totalLayout, checkoutButton
        );
        contentLayout.setSizeFull();
        contentLayout.setHorizontalComponentAlignment(Alignment.END, checkoutButton);

        add(contentLayout);

        loadCart();
    }

    private void configureGrid() {
        menuGrid.addClassNames("cart-grid");
        menuGrid.setSizeFull();

        menuGrid.setColumns(); // Alle Standard-Spalten entfernen

        menuGrid.addColumn(Menu::getMenuTitle)
                .setHeader("Menü")
                .setFlexGrow(2);

        menuGrid.addColumn(Menu::getMenuIngredients)
                .setHeader("Zutaten")
                .setFlexGrow(3);

        menuGrid.addColumn(new ComponentRenderer<>(menu -> {
                    if (menu != null && menu.getMenuPrice() != null) {
                        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.GERMANY);
                        return new Text(formatter.format(menu.getMenuPrice()));
                    }
                    return new Text("-");
                }))
                .setHeader("Preis")
                .setTextAlign(ColumnTextAlign.END)
                .setFlexGrow(1);

        // Entfernen-Button
        menuGrid.addColumn(new ComponentRenderer<>(menu -> {
                    Button removeButton = new Button(new Icon(VaadinIcon.TRASH));
                    removeButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
                    removeButton.addClickListener(e -> removeMenuFromCart(menu));
                    return removeButton;
                }))
                .setHeader("Entfernen")
                .setTextAlign(ColumnTextAlign.CENTER)
                .setWidth("100px")
                .setFlexGrow(0);

        menuGrid.getColumns().forEach(col -> col.setAutoWidth(true));
    }

    private Button createCheckoutButton() {
        Button checkoutButton = new Button("Zur Kasse");
        checkoutButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        checkoutButton.setIcon(new Icon(VaadinIcon.CART));

        checkoutButton.addClickListener(e -> {
            if (currentCart != null && !currentCart.getCartList().isEmpty()) {
                // Weiterleitung zur Bezahlseite
                getUI().ifPresent(ui -> ui.navigate("payment"));
            } else {
                Notification.show("Ihr Warenkorb ist leer",
                                3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        return checkoutButton;
    }

    private void loadCart() {
        UserDetails user = securityService.getAuthenticatedUser();
        if (user != null) {
            currentCart = cartService.getOrCreateCart(user.getUsername());
            updateCartDisplay();
        }
    }

    private void updateCartDisplay() {
        if (currentCart != null && currentCart.getCartList() != null) {
            menuGrid.setItems(currentCart.getCartList());

            // Gesamtpreis berechnen und anzeigen
            double total = cartService.calculateTotalPrice(currentCart);
            NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.GERMANY);
            totalPriceLabel.setText(formatter.format(total));
        } else {
            menuGrid.setItems();
            totalPriceLabel.setText("0,00 €");
        }
    }

    private void removeMenuFromCart(Menu menu) {
        if (currentCart != null) {
            cartService.removeMenuFromCart(currentCart, menu);
            updateCartDisplay();

            Notification.show("Menü aus dem Warenkorb entfernt",
                    2000, Notification.Position.MIDDLE);
        }
    }
}