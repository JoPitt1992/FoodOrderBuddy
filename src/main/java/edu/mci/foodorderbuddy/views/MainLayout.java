package edu.mci.foodorderbuddy.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HighlightConditions;
import com.vaadin.flow.router.RouterLink;
import edu.mci.foodorderbuddy.security.SecurityService;
import org.springframework.security.core.userdetails.UserDetails;

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
        logo.getStyle().set("font-weight", "bold");
        logo.getStyle().set("color", "var(--lumo-primary-color)");

        // User-Info-Bereich
        HorizontalLayout userInfo = createUserInfo();

        Button logout = new Button("Abmelden", e -> securityService.logout());
        logout.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);

        HorizontalLayout header = new HorizontalLayout(new DrawerToggle(), logo, userInfo, logout);

        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(logo);
        header.setWidth("100%");
        header.addClassNames("py-0", "px-m");

        addToNavbar(header);
    }

    private HorizontalLayout createUserInfo() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setAlignItems(FlexComponent.Alignment.CENTER);

        UserDetails user = securityService.getAuthenticatedUser();

        if (user != null) {
            Avatar avatar = new Avatar(user.getUsername());
            avatar.setColorIndex(2); // Ein schöner blauer Ton

            Span name = new Span(user.getUsername());

            layout.add(avatar, name);
        }

        return layout;
    }

    private void createDrawer() {
        // Speisekarte
        RouterLink menuLink = createDrawerLink(
                VaadinIcon.CUTLERY,
                "Speisekarte",
                MenuView.class);

        // Warenkorb
        RouterLink cartLink = createDrawerLink(
                VaadinIcon.CART,
                "Warenkorb",
                CartView.class);

        // Bestellhistorie
        RouterLink orderHistoryLink = createDrawerLink(
                VaadinIcon.LIST,
                "Bestellhistorie",
                OrderHistoryView.class);

        // Benutzerdaten
        RouterLink personDataLink = createDrawerLink(
                VaadinIcon.USER,
                "Benutzerdaten",
                PersonDataView.class);

        // Dashboard (nur für Admins)
        boolean isAdmin = securityService.getAuthenticatedUser() != null &&
                securityService.getAuthenticatedUser().getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        Component dashboardLink = null;
        if (isAdmin) {
            dashboardLink = createDrawerLink(
                    VaadinIcon.DASHBOARD,
                    "Dashboard",
                    DashboardView.class);
        }

        // Layout erstellen
        VerticalLayout drawerLayout = new VerticalLayout();
        drawerLayout.setPadding(false);
        drawerLayout.setSpacing(false);

        // Logo im Drawer
        HorizontalLayout logoLayout = new HorizontalLayout();
        logoLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        logoLayout.setPadding(true);
        logoLayout.setSpacing(true);
        logoLayout.getStyle().set("border-bottom", "1px solid var(--lumo-contrast-10pct)");

        Image logoImage = new Image("icons/icon.png", "Food Order Buddy Logo");
        logoImage.setHeight("36px");

        H1 logoText = new H1("Food Order Buddy");
        logoText.getStyle().set("font-size", "var(--lumo-font-size-l)");
        logoText.getStyle().set("margin", "0");

        logoLayout.add(logoImage, logoText);

        drawerLayout.add(logoLayout);

        // Haupt-Navigationslinks
        Div navSection = new Div();
        navSection.addClassName("drawer-section");
        navSection.getStyle().set("padding", "var(--lumo-space-s) 0");

        navSection.add(menuLink, cartLink, orderHistoryLink, personDataLink);
        if (isAdmin && dashboardLink != null) {
            navSection.add(dashboardLink);
        }

        drawerLayout.add(navSection);

        // Füge alles zum Drawer hinzu
        addToDrawer(drawerLayout);
    }

    private RouterLink createDrawerLink(VaadinIcon icon, String text, Class<? extends Component> viewClass) {
        RouterLink link = new RouterLink(viewClass);
        link.setHighlightCondition(HighlightConditions.sameLocation());

        Icon vaadinIcon = new Icon(icon);
        vaadinIcon.setSize("var(--lumo-icon-size-s)");
        vaadinIcon.getStyle().set("margin-right", "var(--lumo-space-s)");

        Span span = new Span(text);

        link.add(vaadinIcon, span);
        link.addClassName("drawer-link");
        link.getStyle().set("display", "flex");
        link.getStyle().set("align-items", "center");
        link.getStyle().set("padding", "var(--lumo-space-s) var(--lumo-space-m)");
        link.getStyle().set("color", "var(--lumo-secondary-text-color)");
        link.getStyle().set("text-decoration", "none");
        link.getStyle().set("font-size", "var(--lumo-font-size-m)");

        // Hover-Effekt
        link.getStyle().set("transition", "color 0.2s, background-color 0.2s");
        link.getElement().addEventListener("mouseover", event ->
                link.getStyle().set("background-color", "var(--lumo-contrast-5pct)"));
        link.getElement().addEventListener("mouseout", event ->
                link.getStyle().set("background-color", "transparent"));

        return link;
    }
}