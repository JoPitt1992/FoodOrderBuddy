package edu.mci.foodorderbuddy.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import edu.mci.foodorderbuddy.data.entity.Cart;
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
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

@RolesAllowed({"ROLE_USER", "ROLE_ADMIN"})
@PageTitle("Bestellhistorie | Food Order Buddy")
@Route(value = "orderhistory", layout = MainLayout.class)
public class OrderHistoryView extends VerticalLayout {

    private final OrderHistoryService orderHistoryService;
    private final SecurityService securityService;

    private final Grid<Cart> orderGrid = new Grid<>(Cart.class);
    private final TextField filterText = new TextField();

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.GERMANY);

    private UI ui;
    private String currentUsername;

    private List<Cart> currentOrders = List.of();

    public OrderHistoryView(OrderHistoryService orderHistoryService, SecurityService securityService) {
        this.orderHistoryService = orderHistoryService;
        this.securityService = securityService;

        addClassName("order-history-view");
        setSizeFull();

        H3 title = new H3("Meine Bestellungen");

        configureGrid();
        configureFilter();

        VerticalLayout contentLayout = new VerticalLayout(title, getToolbar(), orderGrid);
        contentLayout.setSizeFull();
        contentLayout.setPadding(true);
        contentLayout.setSpacing(true);

        add(contentLayout);

        loadOrders();
    }

    private void configureGrid() {
        orderGrid.addClassName("order-grid");
        orderGrid.setSizeFull();
        orderGrid.setHeight("calc(100% - 100px)");

        orderGrid.setColumns(); // Alle Standard-Spalten entfernen

        orderGrid.addColumn(cart -> cart.getPaymentReference() != null ? cart.getPaymentReference() : "")
                .setHeader("Bestellnummer")
                .setSortable(true)
                .setFlexGrow(1);

        orderGrid.addColumn(cart -> cart.getCartPaydate() != null ? dateFormat.format(cart.getCartPaydate()) : "")
                .setHeader("Bestelldatum")
                .setSortable(true)
                .setFlexGrow(1);

        orderGrid.addColumn(cart -> currencyFormat.format(cart.getCartPrice()))
                .setHeader("Gesamtpreis")
                .setTextAlign(ColumnTextAlign.END)
                .setSortable(true)
                .setFlexGrow(1);

        if (isUserInRole("ROLE_ADMIN")) {
            orderGrid.addColumn(new ComponentRenderer<>(cart -> {
                        ComboBox<OrderStatus> statusBox = new ComboBox<>();
                        statusBox.setItems(OrderStatus.values());
                        statusBox.setItemLabelGenerator(OrderStatus::getDisplayName);

                        //Falls kein Status gesetzt
                        statusBox.setValue(cart.getCartOrderStatus() != null
                                ? cart.getCartOrderStatus()
                                : OrderStatus.IN_BEARBEITUNG);

                        statusBox.addValueChangeListener(event -> {
                            OrderStatus selectedStatus = event.getValue();
                            if (selectedStatus == null) return;

                            cart.setCartOrderStatus(selectedStatus);
                            orderHistoryService.save(cart);

                            //Broadcast
                            OrderStatusBroadcaster.broadcast(
                                    new OrderStatusBroadcaster.OrderStatusMessage(
                                            cart.getCartId(),
                                            selectedStatus,
                                            cart.getOwner().getPersonUserName()
                                    )
                            );


                            Notification.show("Status geändert auf: " + selectedStatus.getDisplayName());
                            orderGrid.getDataProvider().refreshItem(cart);
                        });

                return statusBox;
            }))
            .setHeader("Status")
            .setSortable(false)
            .setFlexGrow(1);

            orderGrid.addColumn(new ComponentRenderer<>(cart -> {
                        Button viewButton = new Button("Details");
                        viewButton.setIcon(new Icon(VaadinIcon.SEARCH));
                        viewButton.addClickListener(e ->
                                getUI().ifPresent(ui -> ui.navigate("order-details/" + cart.getCartId()))
                        );
                        return viewButton;
                    }))
                    .setHeader("Aktionen")
                    .setTextAlign(ColumnTextAlign.CENTER)
                    .setWidth("150px")
                    .setFlexGrow(0);

        } else {
            orderGrid.addColumn(new ComponentRenderer<>(cart -> {
                Span statusLabel;

                OrderStatus status = cart.getCartOrderStatus();

                if (status == OrderStatus.ZUGESTELLT) {
                    statusLabel = new Span("Zugestellt");
                    statusLabel.getStyle().set("color", "var(--lumo-success-color)");
                    statusLabel.getStyle().set("font-weight", "bold");
                } else if (status == OrderStatus.IN_ZUSTELLUNG) {
                    statusLabel = new Span("In Zustellung");
                    statusLabel.getStyle().set("color", "orange");
                    statusLabel.getStyle().set("font-weight", "bold");
                } else {
                    statusLabel = new Span("In Bearbeitung");
                    statusLabel.getStyle().set("color", "var(--lumo-primary-color)");
                }

                return statusLabel;
            }))
            .setHeader("Status")
            .setSortable(true)
            .setFlexGrow(1);

            orderGrid.addColumn(new ComponentRenderer<>(cart -> {
                        Button viewButton = new Button("Details");
                        viewButton.setIcon(new Icon(VaadinIcon.SEARCH));
                        viewButton.addClickListener(e ->
                                getUI().ifPresent(ui -> ui.navigate("order-details/" + cart.getCartId()))
                        );
                        return viewButton;
                    }))
                    .setHeader("Aktionen")
                    .setTextAlign(ColumnTextAlign.CENTER)
                    .setWidth("150px")
                    .setFlexGrow(0);
        }

    }

    private void configureFilter() {
        filterText.setPlaceholder("Suche in Bestellungen...");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        filterText.addValueChangeListener(e -> loadOrders());
    }

    private HorizontalLayout getToolbar() {
        filterText.setWidth("50%");

        HorizontalLayout toolbar = new HorizontalLayout(filterText);
        toolbar.setWidthFull();
        toolbar.setAlignItems(Alignment.CENTER);
        toolbar.addClassName("order-history-toolbar");

        return toolbar;
    }

    private void loadOrders() {
        UserDetails user = securityService.getAuthenticatedUser();
        String searchTerm = filterText.getValue();

        if (isUserInRole("ROLE_ADMIN")) {
            List<Cart> orders = orderHistoryService.getAllOrdersByTerm(searchTerm);
            currentOrders = orders;
            orderGrid.setItems(orders);

            if (orders.isEmpty() && !searchTerm.isEmpty()) {
                Notification.show("Keine Bestellungen gefunden für: " + searchTerm);
            }
        } else if (user != null) {
            List<Cart> orders = orderHistoryService.searchOrdersByTerm(user.getUsername(), searchTerm);
            currentOrders = orders;
            orderGrid.setItems(orders);

            if (orders.isEmpty() && !searchTerm.isEmpty()) {
                Notification.show("Keine Bestellungen gefunden für: " + searchTerm);
            }
        }
    }

    private boolean isUserInRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(role));
    }

    /**
     * Für Broadcasting
     */


    private final Consumer<OrderStatusBroadcaster.OrderStatusMessage> orderStatusListener = message -> {
        if (!message.targetUsername.equals(currentUsername)) return;

        Cart updatedCart = currentOrders.stream()
                .filter(cart -> cart.getCartId().equals(message.cartId))
                .findFirst()
                .orElse(null);

        if (updatedCart != null) {
            updatedCart.setCartOrderStatus(message.newStatus);
            ui.access(() -> {
                orderGrid.getDataProvider().refreshItem(updatedCart);
                Notification.show("Status Ihrer Bestellung wurde geändert auf: " + message.newStatus.getDisplayName(),
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
            OrderStatusBroadcaster.register(orderStatusListener);
        }
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        OrderStatusBroadcaster.unregister(orderStatusListener);
        super.onDetach(detachEvent);
    }
}