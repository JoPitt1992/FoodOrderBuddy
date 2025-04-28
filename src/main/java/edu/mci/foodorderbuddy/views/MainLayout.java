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
        H1 logo = new H1("Food-Order Buddy");
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
        RouterLink menuLink = new RouterLink("Menu", MenuView.class);
        menuLink.setHighlightCondition(HighlightConditions.sameLocation());

        RouterLink myOrdersLink = new RouterLink("MyOrders", MyOrdersView.class);
        myOrdersLink.setHighlightCondition(HighlightConditions.sameLocation());

        RouterLink myProfileLink = new RouterLink("MyProfile", MyProfileView.class);
        myProfileLink.setHighlightCondition(HighlightConditions.sameLocation());

        addToDrawer(new VerticalLayout(
                menuLink,
                myOrdersLink,
                myProfileLink
        ));
    }
}