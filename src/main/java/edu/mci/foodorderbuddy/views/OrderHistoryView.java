package edu.mci.foodorderbuddy.views;

import com.vaadin.flow.component.button.Button;
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
import edu.mci.foodorderbuddy.security.SecurityService;
import edu.mci.foodorderbuddy.service.OrderHistoryService;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.core.userdetails.UserDetails;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

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

        orderGrid.addColumn(new ComponentRenderer<>(cart -> {
                    if (Boolean.TRUE.equals(cart.getCartDelivered())) {
                        Span delivered = new Span("Zugestellt");
                        delivered.getStyle().set("color", "var(--lumo-success-color)");
                        delivered.getStyle().set("font-weight", "bold");
                        return delivered;
                    } else {
                        Span pending = new Span("In Bearbeitung");
                        pending.getStyle().set("color", "var(--lumo-primary-color)");
                        return pending;
                    }
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

        if (user != null) {
            String searchTerm = filterText.getValue();
            List<Cart> orders = orderHistoryService.searchOrdersByTerm(user.getUsername(), searchTerm);

            if (orders.isEmpty()) {
                orderGrid.setItems();
                if (!searchTerm.isEmpty()) {
                    Notification.show("Keine Bestellungen gefunden für: " + searchTerm);
                }
            } else {
                orderGrid.setItems(orders);
            }
        }
    }
}