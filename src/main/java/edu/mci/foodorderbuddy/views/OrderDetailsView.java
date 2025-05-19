package edu.mci.foodorderbuddy.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import edu.mci.foodorderbuddy.data.entity.Cart;
import edu.mci.foodorderbuddy.data.entity.CartItem;
import edu.mci.foodorderbuddy.data.entity.OrderStatus;
import edu.mci.foodorderbuddy.security.SecurityService;
import edu.mci.foodorderbuddy.service.OrderHistoryService;
import edu.mci.foodorderbuddy.service.OrderStatusBroadcaster;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;

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

    private UI ui;
    private String currentUsername;
    private boolean listenerRegistered = false;

    private Paragraph totalLabel;
    private Cart cart;
    private Span statusLabel;



    public OrderDetailsView(OrderHistoryService orderHistoryService, SecurityService securityService) {
        this.orderHistoryService = orderHistoryService;
        this.securityService = securityService;

        setSizeFull();
        setAlignItems(Alignment.CENTER);

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

        if (isUserInRole("ROLE_ADMIN")) {
            itemsGrid.addColumn(new ComponentRenderer<>(item -> {
                        HorizontalLayout layout = new HorizontalLayout();
                        layout.setAlignItems(Alignment.CENTER);
                        layout.setSpacing(true);

                        IntegerField quantityField = new IntegerField();
                        quantityField.setValue(item.getQuantity());
                        quantityField.setMin(1);
                        quantityField.setStep(1);
                        quantityField.setWidth("60px");
                        quantityField.getStyle().set("flex-shrink", "0");

                        quantityField.addValueChangeListener(event -> {
                            if (event.getValue() != null) {
                                item.setQuantity(event.getValue());
                                itemsGrid.getDataProvider().refreshItem(item);

                                double newTotal = cart.getCartItems().stream()
                                        .mapToDouble(ci -> ci.getMenu().getMenuPrice() * ci.getQuantity())
                                        .sum();

                                cart.setCartPrice(newTotal);
                                totalLabel.setText("Gesamtbetrag: " + currencyFormat.format(newTotal));

                            }
                        });

                        layout.add(quantityField);
                        return layout;
                    }))
                    .setHeader("Anzahl")
                    .setFlexGrow(1);
        } else {
            itemsGrid.addColumn(CartItem::getQuantity)
                    .setHeader("Anzahl")
                    .setFlexGrow(1);
        }

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
            cart = optionalCart.get();

            // Sicherheitsprüfung: Der Benutzer und der Admin darf die Bestellungen ansehen
            if (cart.getOwner() != null &&
                    (cart.getOwner().getPersonUserName().equals(user.getUsername())
                            || user.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")))) {
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
        this.cart = cart;

        H2 title = new H2("Bestelldetails");

        HorizontalLayout orderInfo = new HorizontalLayout();
        orderInfo.setWidthFull();

        VerticalLayout leftInfo = new VerticalLayout();
        leftInfo.setPadding(false);
        leftInfo.setSpacing(false);
        leftInfo.add(new Paragraph("Bestellnummer: " + cart.getPaymentReference()));
        leftInfo.add(new Paragraph("Bestelldatum: " + dateFormat.format(cart.getCartPaydate())));
        if (cart.getOwner() != null) {
            leftInfo.add(new Paragraph("Kunde: " + cart.getOwner().getPersonFirstName() + " " + cart.getOwner().getPersonLastName()));
        }

        VerticalLayout rightInfo = new VerticalLayout();
        rightInfo.setPadding(false);
        rightInfo.setSpacing(false);
        statusLabel = new Span(cart.getCartOrderStatus().getDisplayName());
        styleStatusLabel(statusLabel, cart.getCartOrderStatus());
        rightInfo.add(statusLabel);
        rightInfo.add(new Paragraph("Zahlungsmethode: " + cart.getPaymentMethod()));

        orderInfo.add(leftInfo, rightInfo);

        // Grid mit den bestellten Menüs
        H3 itemsTitle = new H3("Bestellte Artikel");

        // Gesamtpreis
        HorizontalLayout totalLayout = new HorizontalLayout();
        totalLayout.setWidthFull();
        totalLayout.setJustifyContentMode(JustifyContentMode.END);

        totalLabel = new Paragraph("Gesamtbetrag: " + currencyFormat.format(cart.getCartPrice()));
        totalLabel.getStyle().set("font-weight", "bold");
        totalLabel.getStyle().set("font-size", "1.2em");
        totalLayout.add(totalLabel);

        configureGrid();
        itemsGrid.setItems(cart.getCartItems());

        // Buttons
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setWidthFull();
        buttonLayout.setJustifyContentMode(JustifyContentMode.CENTER);

        if (isUserInRole("ROLE_ADMIN")) {
            Button saveButton = new Button("Speichern");
            saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

            saveButton.addClickListener(e -> {
                orderHistoryService.save(cart); // <– EINZIGER Speicherort
                Notification.show("Änderungen gespeichert", 3000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                getUI().ifPresent(ui -> ui.navigate("orderhistory"));
            });

            Button deleteButton = new Button("Löschen");
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

            deleteButton.addClickListener(e -> {
                orderHistoryService.delete(cart);
                Notification.show("Bestellung gelöscht", 3000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                getUI().ifPresent(ui -> ui.navigate("orderhistory"));
            });

            buttonLayout.add(saveButton);
            buttonLayout.add(deleteButton);
        }

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

    private boolean isUserInRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(role));
    }

    private final Consumer<OrderStatusBroadcaster.OrderStatusMessage> orderStatusListener = message -> {
        if (!message.targetUsername.equals(currentUsername)) return;

        if (cart != null && cart.getCartId().equals(message.cartId)) {
            cart.setCartOrderStatus(message.newStatus);
            ui.access(() -> {
                // Statusanzeige aktualisieren
                if (statusLabel != null) {
                    statusLabel.setText(message.newStatus.getDisplayName());
                    styleStatusLabel(statusLabel, message.newStatus);
                }

                // Notification + Sound
                Notification.show("Der Status Ihrer Bestellung wurde geändert auf: " + message.newStatus.getDisplayName(),
                        4000, Notification.Position.MIDDLE);
                ui.getPage().executeJs(
                        "const audio = new Audio('/sounds/notify.wav'); audio.play().catch(err => console.warn('Audio not played:', err));"
                );
            });
        }
    };

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        ui = attachEvent.getUI();
        UserDetails user = securityService.getAuthenticatedUser();
        if (user != null) {
            currentUsername = user.getUsername();
            if (!listenerRegistered) {
                OrderStatusBroadcaster.register(orderStatusListener);
                listenerRegistered = true;
            }
        }
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        OrderStatusBroadcaster.unregister(orderStatusListener);
        super.onDetach(detachEvent);
    }

    private void styleStatusLabel(Span label, OrderStatus status) {
        label.getStyle().set("font-weight", "bold");

        switch (status) {
            case ZUGESTELLT -> label.getStyle().set("color", "var(--lumo-success-color)");
            case IN_ZUSTELLUNG -> label.getStyle().set("color", "orange");
            default -> label.getStyle().set("color", "var(--lumo-primary-color)");
        }
    }
}