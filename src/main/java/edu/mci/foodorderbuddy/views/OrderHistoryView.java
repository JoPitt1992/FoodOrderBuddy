package edu.mci.foodorderbuddy.views;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import edu.mci.foodorderbuddy.data.entity.Cart;
import edu.mci.foodorderbuddy.service.OrderHistoryService;
import edu.mci.foodorderbuddy.service.PersonService;
import jakarta.annotation.security.RolesAllowed;

import java.util.List;

@RolesAllowed({"ROLE_USER", "ROLE_ADMIN"})
@PageTitle("Bestellhistorie | Food Order Buddy")
@Route(value = "orderhistory", layout = MainLayout.class)
public class OrderHistoryView extends VerticalLayout {

    private final OrderHistoryService orderHistoryService;
    private final PersonService personService;

    private Grid<Cart> cartGrid = new Grid<>(Cart.class);
    private TextField filterText = new TextField();

    public OrderHistoryView(OrderHistoryService orderHistoryService, PersonService personService) {
        this.orderHistoryService = orderHistoryService;
        this.personService = personService;

        addClassName("list-view");
        setSizeFull();

        configureGrid();

        add(getToolbar(), cartGrid);

        //updateList();
    }

    private void configureGrid() {
        cartGrid.setSizeFull();
        cartGrid.setColumns("cartId", "cartPrice", "cartPaydate", "cartDelivered");

        cartGrid.getColumnByKey("cartId").setHeader("Nr.");
        cartGrid.getColumnByKey("cartPrice").setHeader("Gesamtpreis");
        cartGrid.getColumnByKey("cartPaydate").setHeader("Bezahldatum");
        cartGrid.getColumnByKey("cartDelivered").setHeader("Lieferstatus");

        cartGrid.getColumns().forEach(col -> col.setAutoWidth(true));
    }

    private HorizontalLayout getToolbar() {
        filterText.setPlaceholder("Suche in der Bestellhistorie...");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        //filterText.addValueChangeListener(e -> updateList());
        filterText.setWidth("500px");

        HorizontalLayout toolbar = new HorizontalLayout(filterText);
        toolbar.addClassName("toolbar");
        toolbar.setWidthFull();
        toolbar.setAlignItems(Alignment.CENTER);

        return toolbar;
    }
/*
    private void updateList() {
        String filter = filterText.getValue();
        List<Cart> carts;

        if (filter == null || filter.isEmpty()) {
            carts = orderHistoryService.findAllCarts();
        } else {
            // Filter: z.B. nach cartId (als String) oder nach cartDelivered (z.B. true/false)
            carts = orderHistoryService.findAllCarts().stream()
                    .filter(cart -> {
                        boolean matchesId = String.valueOf(cart.getCartId()).contains(filter);
                        boolean matchesDelivered = cart.getCartDelivered() != null &&
                                cart.getCartDelivered().toString().toLowerCase().contains(filter.toLowerCase());
                        return matchesId || matchesDelivered;
                    })
                    .toList();
        }

        cartGrid.setItems(carts);
    }*/
}
