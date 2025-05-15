package edu.mci.foodorderbuddy.views;

import edu.mci.foodorderbuddy.service.MenuService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@PermitAll
@Route(value = "orderhistory", layout = MainLayout.class)
@PageTitle("Bestellhistorie | Food Order Buddy")
public class OrderHistoryView extends VerticalLayout {
    private final MenuService service;

    public OrderHistoryView(MenuService service) {
        this.service = service;
        addClassName("dashboard-view");
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        add(exampleMethod());
    }

    private Component exampleMethod() {
        Span message = new Span("Orderhistory-Site under construction");
        message.addClassNames("text-xl", "mt-m");
        return message;
    }
}