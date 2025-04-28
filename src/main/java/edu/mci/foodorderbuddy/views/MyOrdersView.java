package edu.mci.foodorderbuddy.views;

import edu.mci.foodorderbuddy.service.MenuService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@PermitAll
@Route(value = "orders", layout = MainLayout.class)
@PageTitle("My Orders | Food Order Buddy")
public class MyOrdersView extends VerticalLayout {
    private final MenuService service;

    public MyOrdersView(MenuService service) {
        this.service = service;
        addClassName("dashboard-view");
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        add(getContactStats());
    }

    private Component getContactStats() {
        Span stats = new Span(service.countMenu() + " Menüs stehen zur Auswahl");
        stats.addClassNames("text-xl", "mt-m");
        return stats;
    }
}