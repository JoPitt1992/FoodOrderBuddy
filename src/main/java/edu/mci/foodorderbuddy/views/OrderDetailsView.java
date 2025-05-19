package edu.mci.foodorderbuddy.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import edu.mci.foodorderbuddy.data.entity.Cart;
import edu.mci.foodorderbuddy.data.entity.CartItem;
import edu.mci.foodorderbuddy.data.entity.OrderStatus;
import edu.mci.foodorderbuddy.security.SecurityService;
import edu.mci.foodorderbuddy.service.OrderHistoryService;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.core.userdetails.UserDetails;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Optional;

@RolesAllowed({"ROLE_USER", "ROLE_ADMIN"})
@Route(value = "order-details", layout = MainLayout.class)
@PageTitle("Bestelldetails | Food Order Buddy")
public class OrderDetailsView extends VerticalLayout implements HasUrlParameter<Long> {

    private final OrderHistoryService orderHistoryService;
    private final SecurityService securityService;

    private Grid<CartItem> itemsGrid = new Grid<>(CartItem.class);
    private VerticalLayout orderDetails = new VerticalLayout();

    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.GERMANY);

    public OrderDetailsView(OrderHistoryService orderHistoryService, SecurityService securityService) {
        this.orderHistoryService = orderHistoryService;
        this.securityService = securityService;

        setSizeFull();
        setAlignItems(Alignment.CENTER);

        configureGrid();

        orderDetails.setWidth("800px");
        orderDetails.setPadding(true);
        orderDetails.setSpacing(true);

        add(orderDetails);
    }

    private void configureGrid() {
        itemsGrid.setHeight("300px");
        itemsGrid.setColumns(); // Alle Standard-Spalten entfernen

        itemsGrid.addColumn(item -> item.getMenu().getMenuTitle())
                .setHeader("Menü")
                .setFlexGrow(3);

        itemsGrid.addColumn(CartItem::getQuantity)
                .setHeader("Anzahl")
                .setFlexGrow(1);

        itemsGrid.addColumn(item -> currencyFormat.format(item.getMenu().getMenuPrice()))
                .setHeader("Einzelpreis")
                .setFlexGrow(1);

        itemsGrid.addColumn(item -> {
                    double total = item.getMenu().getMenuPrice() * item.getQuantity();
                    return currencyFormat.format(total);
                })
                .setHeader("Gesamtpreis")
                .setFlexGrow(1);
    }

    @Override
    public void setParameter(BeforeEvent event, Long cartId) {
        orderDetails.removeAll();

        UserDetails user = securityService.getAuthenticatedUser();
        if (user == null) {
            event.forwardTo("login");
            return;
        }

        Optional<Cart> optionalCart = orderHistoryService.getCartById(cartId);

        if (optionalCart.isPresent()) {
            Cart cart = optionalCart.get();

            // Sicherheitsprüfung: Der Benutzer darf nur seine eigenen Bestellungen ansehen
            if (cart.getOwner() != null &&
                    cart.getOwner().getPersonUserName().equals(user.getUsername())) {
                displayOrderDetails(cart);
            } else {
                accessDenied();
                event.forwardTo("orderhistory");
            }
        } else {
            orderNotFound();
        }
    }

    private void displayOrderDetails(Cart cart) {
        H2 title = new H2("Bestelldetails");

        HorizontalLayout orderInfo = new HorizontalLayout();
        orderInfo.setWidthFull();

        VerticalLayout leftInfo = new VerticalLayout();
        leftInfo.setPadding(false);
        leftInfo.setSpacing(false);
        leftInfo.add(new Paragraph("Bestellnummer: " + cart.getPaymentReference()));
        leftInfo.add(new Paragraph("Bestelldatum: " + dateFormat.format(cart.getCartPaydate())));

        VerticalLayout rightInfo = new VerticalLayout();
        rightInfo.setPadding(false);
        rightInfo.setSpacing(false);
        rightInfo.add(new Paragraph("Status: " + cart.getCartOrderStatus()));
        rightInfo.add(new Paragraph("Zahlungsmethode: " + cart.getPaymentMethod()));

        orderInfo.add(leftInfo, rightInfo);

        // Grid mit den bestellten Menüs
        H3 itemsTitle = new H3("Bestellte Artikel");
        itemsGrid.setItems(cart.getCartItems());

        // Gesamtpreis
        HorizontalLayout totalLayout = new HorizontalLayout();
        totalLayout.setWidthFull();
        totalLayout.setJustifyContentMode(JustifyContentMode.END);

        Paragraph totalLabel = new Paragraph("Gesamtbetrag: " + currencyFormat.format(cart.getCartPrice()));
        totalLabel.getStyle().set("font-weight", "bold");
        totalLabel.getStyle().set("font-size", "1.2em");

        totalLayout.add(totalLabel);

        // Buttons
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setWidthFull();
        buttonLayout.setJustifyContentMode(JustifyContentMode.CENTER);

        Button backButton = new Button("Zurück zur Übersicht");
        backButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("orderhistory")));

        // Wenn die Bestellung noch nicht als geliefert markiert ist und der Benutzer ein Admin ist
        if (cart.getCartOrderStatus() != OrderStatus.ZUGESTELLT &&
                securityService.getAuthenticatedUser().getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {

            Button markAsDeliveredButton = new Button("Als geliefert markieren");
            markAsDeliveredButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            markAsDeliveredButton.setIcon(new Icon(VaadinIcon.CHECK));

            markAsDeliveredButton.addClickListener(e -> {
                // Setze den Status auf ZUGESTELLT und speichere ihn
                cart.setCartOrderStatus(OrderStatus.ZUGESTELLT);
                orderHistoryService.save(cart);
                Notification.show("Bestellung als geliefert markiert",
                                3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                getUI().ifPresent(ui -> ui.navigate("order-details/" + cart.getCartId()));
            });

            buttonLayout.add(backButton, markAsDeliveredButton);
        } else {
            buttonLayout.add(backButton);
        }

        orderDetails.add(title, orderInfo, itemsTitle, itemsGrid, totalLayout, buttonLayout);
    }

    private void orderNotFound() {
        Icon warningIcon = new Icon(VaadinIcon.WARNING);
        warningIcon.setColor("var(--lumo-error-color)");
        warningIcon.setSize("3em");

        H2 title = new H2("Bestellung nicht gefunden");

        Paragraph message = new Paragraph("Die angeforderte Bestellung wurde nicht gefunden.");

        Button backButton = new Button("Zurück zur Übersicht",
                e -> getUI().ifPresent(ui -> ui.navigate("orderhistory")));
        backButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        orderDetails.add(warningIcon, title, message, backButton);
        orderDetails.setHorizontalComponentAlignment(Alignment.CENTER, warningIcon, title, message, backButton);
    }

    private void accessDenied() {
        Notification.show("Zugriff verweigert: Sie haben keinen Zugriff auf diese Bestellung.",
                        5000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}