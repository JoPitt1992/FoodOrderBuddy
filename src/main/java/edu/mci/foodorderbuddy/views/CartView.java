package edu.mci.foodorderbuddy.views;

import edu.mci.foodorderbuddy.service.MenuService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@PermitAll
@Route(value = "cart", layout = MainLayout.class)
@PageTitle("Warenkorb | Food Order Buddy")
public class CartView extends VerticalLayout {
    private final MenuService service;

    public CartView(MenuService service) {
        this.service = service;
        addClassName("dashboard-view");
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        add(exampleMethod());
    }

    private Component exampleMethod() {
        Span message = new Span("CartView-Site under construction");
        message.addClassNames("text-xl", "mt-m");
        return message;
    }
}