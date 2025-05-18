package edu.mci.foodorderbuddy.views;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import edu.mci.foodorderbuddy.data.entity.Cart;
import edu.mci.foodorderbuddy.data.entity.Menu;
import edu.mci.foodorderbuddy.security.SecurityService;
import edu.mci.foodorderbuddy.service.CartService;
import edu.mci.foodorderbuddy.service.MenuService;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RolesAllowed({"ROLE_USER", "ROLE_ADMIN"})
@Route(value = "menu", layout = MainLayout.class)
@PageTitle("Speisekarte | Food Order Buddy")
public class MenuView extends VerticalLayout {
    private final Grid<Menu> dailyMenuGrid = new Grid<>(Menu.class, false);
    private final Grid<Menu> regularMenuGrid = new Grid<>(Menu.class, false);
    private final TextField filterText = new TextField();
    private final MenuForm form;
    private final MenuService menuService;
    private final CartService cartService;
    private final SecurityService securityService;

    private final VerticalLayout contentLayout = new VerticalLayout(); // Enthält Listen
    private final HorizontalLayout mainLayout = new HorizontalLayout(); // Enthält Listen + Form

    public MenuView(MenuService menuService, CartService cartService, SecurityService securityService) {
        this.menuService = menuService;
        this.cartService = cartService;
        this.securityService = securityService;

        setSizeFull();
        setPadding(true);
        getStyle().set("padding", "16px");
        setSpacing(false);

        configureGrid(dailyMenuGrid);
        configureGrid(regularMenuGrid);

        form = new MenuForm();
        configureForm();

        contentLayout.addClassName("list-section");
        contentLayout.setWidthFull();
        contentLayout.setSpacing(false);
        contentLayout.setPadding(false);
        contentLayout.add(getToolbar());
        contentLayout.add(createSection("Tagesmenü:", dailyMenuGrid));
        contentLayout.add(createSection("Speisekarte:", regularMenuGrid));

        form.setVisible(false);
        mainLayout.setSizeFull();
        mainLayout.add(contentLayout, form);
        mainLayout.setFlexGrow(1, contentLayout);
        mainLayout.setFlexGrow(0, form);

        add(mainLayout);

        updateLists();
        closeEditor();
    }

    private void configureForm() {
        form.setWidth("25%");
        form.addClassName("menu-form");
        form.addListener(MenuForm.SaveEvent.class, this::saveMenu);
        form.addListener(MenuForm.DeleteEvent.class, this::deleteMenu);
        form.addListener(MenuForm.CloseEvent.class, e -> closeEditor());
        form.addListener(MenuForm.AddToCartEvent.class, this::addToCart);
    }

    private void configureGrid(Grid<Menu> grid) {
        grid.setWidthFull();
        grid.addClassName("menu-grid");
        grid.setHeight("300px");

        grid.addColumn(Menu::getMenuId)
                .setHeader("Nr.")
                .setSortable(true)
                .setAutoWidth(true);

        grid.addColumn(Menu::getMenuTitle)
                .setHeader("Menübezeichnung")
                .setAutoWidth(true);

        grid.addColumn(Menu::getMenuIngredients)
                .setHeader("Zutaten")
                .setAutoWidth(true);

        grid.addColumn(new ComponentRenderer<>(menu -> {
                    if (menu != null && menu.getMenuPrice() != null) {
                        return new Text(String.format("%.2f €", menu.getMenuPrice()));
                    }
                    return new Text("-");
                }))
                .setHeader("Preis")
                .setTextAlign(ColumnTextAlign.CENTER)
                .setAutoWidth(true)
                .setSortable(true)
                .setComparator(Comparator.comparing(Menu::getMenuPrice, Comparator.nullsLast(Comparator.naturalOrder())));

        grid.asSingleSelect().addValueChangeListener(event -> editMenu(event.getValue()));
    }

    private HorizontalLayout getToolbar() {
        filterText.setPlaceholder("Suche in der Speisekarte... ");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        filterText.addValueChangeListener(e -> updateLists());
        filterText.setWidth("500px");

        HorizontalLayout toolbar = new HorizontalLayout(filterText);
        toolbar.setWidthFull();
        toolbar.setAlignItems(Alignment.CENTER);

        if (isUserInRole("ROLE_ADMIN")) {
            Button addMenubutton = new Button("Menü hinzufügen");
            addMenubutton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            addMenubutton.addClickListener(click -> addMenu());

            Div spacer = new Div();
            toolbar.add(spacer);
            toolbar.setFlexGrow(1, spacer);
            toolbar.add(addMenubutton);
        }

        return toolbar;
    }

    private VerticalLayout createSection(String title, Grid<Menu> grid) {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(false);

        H3 heading = new H3(title);
        heading.getStyle().set("margin-top", "12px");
        heading.getStyle().set("margin-bottom", "12px");

        section.add(heading, grid);
        return section;
    }


    private void editMenu(Menu menu) {
        if (menu == null) {
            closeEditor();
        } else {
            form.setMenu(menu);
            form.setVisible(true);
        }
    }

    private void saveMenu(MenuForm.SaveEvent event) {
        menuService.saveMenu(event.getMenu());
        updateLists();
        closeEditor();
    }

    private void deleteMenu(MenuForm.DeleteEvent event) {
        menuService.deleteMenu(event.getMenu());
        updateLists();
        closeEditor();
    }

    private void addToCart(MenuForm.AddToCartEvent event) {
        Menu menu = event.getMenu();
        UserDetails user = securityService.getAuthenticatedUser();

        if (user != null) {
            Cart cart = cartService.getOrCreateCart(user.getUsername());
            if (cart != null) {
                cartService.addMenuToCart(cart, menu);
                Notification notification = new Notification("Menü zum Warenkorb hinzugefügt", 3000, Notification.Position.MIDDLE);
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                notification.open();
            }
        }
        closeEditor();
    }

    private void closeEditor() {
        form.setMenu(null);
        form.setVisible(false);
        dailyMenuGrid.asSingleSelect().clear();
        regularMenuGrid.asSingleSelect().clear();
    }

    private void addMenu() {
        closeEditor();
        editMenu(new Menu());
    }

    private void updateLists() {
        String filter = filterText.getValue();
        List<Menu> allMenus = menuService.findAllMenus(filter);

        List<Menu> dailyMenus = allMenus.stream()
                .filter(Menu::isMenuDaily)
                .collect(Collectors.toList());

        List<Menu> regularMenus = allMenus.stream()
                .filter(menu -> !menu.isMenuDaily())
                .collect(Collectors.toList());

        dailyMenuGrid.setItems(dailyMenus);
        regularMenuGrid.setItems(regularMenus);
    }

    private boolean isUserInRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(role));
    }
}
