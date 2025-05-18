package edu.mci.foodorderbuddy.views;

import edu.mci.foodorderbuddy.security.SecurityService;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HighlightConditions;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.component.button.Button;

public class MainLayout extends AppLayout {
    private final SecurityService securityService;

    public MainLayout(SecurityService securityService) {
        this.securityService = securityService;
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        H1 logo = new H1("Food-Order-Buddy");
        logo.addClassNames("text-l", "m-m");

        Button logout = new Button("Log out", e -> securityService.logout());
        HorizontalLayout header = new HorizontalLayout(new DrawerToggle(), logo, logout);

        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(logo);
        header.setWidth("100%");
        header.addClassNames("py-0", "px-m");

        addToNavbar(header);
    }

    private void createDrawer() {
        RouterLink menuLink = new RouterLink("Speisekarte", MenuView.class);
        menuLink.setHighlightCondition(HighlightConditions.sameLocation());

        RouterLink cartLink = new RouterLink("Warenkorb", CartView.class);
        cartLink.setHighlightCondition(HighlightConditions.sameLocation());

        RouterLink orderHistoryLink = new RouterLink("Bestellhistorie", OrderHistoryView.class);
        orderHistoryLink.setHighlightCondition(HighlightConditions.sameLocation());

        RouterLink personDataLink = new RouterLink("Benutzerdaten", PersonDataView.class);
        personDataLink.setHighlightCondition(HighlightConditions.sameLocation());

        RouterLink dashboardLink = new RouterLink("Dashboard", DashboardView.class);
        dashboardLink.setHighlightCondition(HighlightConditions.sameLocation());

        RouterLink registryLink = new RouterLink("Registrieren", DashboardView.class);
        dashboardLink.setHighlightCondition(HighlightConditions.sameLocation());

        addToDrawer(new VerticalLayout(
                menuLink,
                cartLink,
                orderHistoryLink,
                personDataLink,
                dashboardLink,
                registryLink
        ));
    }
}