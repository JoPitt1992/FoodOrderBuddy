package edu.mci.foodorderbuddy.views;

import edu.mci.foodorderbuddy.service.MenuService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@PermitAll
@Route(value = "dashboard", layout = MainLayout.class)
@PageTitle("Admin Dashboard | Food Order Buddy")
public class DashboardView extends VerticalLayout {
    private final MenuService service;

    public DashboardView(MenuService service) {
        this.service = service;
        addClassName("dashboard-view");
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        add(exampleMethod());
    }

    private Component exampleMethod() {
        Span message = new Span("DashboardView-Site under construction --- By bennin");
        message.addClassNames("text-xl", "mt-m");
        return message;
    }
}