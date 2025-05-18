package edu.mci.foodorderbuddy.views;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
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

@RolesAllowed({"ROLE_USER", "ROLE_ADMIN"})
@Route(value = "menu", layout = MainLayout.class)
@PageTitle("Speisekarte | Food Order Buddy")
public class MenuView extends VerticalLayout {
    Grid<Menu> menuGrid = new Grid<>(Menu.class);
    TextField filterText = new TextField();
    MenuForm form;
    private final MenuService menuService;
    private final CartService cartService;
    private final SecurityService securityService;

    public MenuView(MenuService menuService, CartService cartService, SecurityService securityService) {
        this.menuService = menuService;
        this.cartService = cartService;
        this.securityService = securityService;

        addClassName("list-view");
        setSizeFull();
        configureGrids();
        form = new MenuForm();
        configureForm();

        add(getToolbar(), getContent());
        updateList();
        closeEditor();
    }

    private void configureGrids() {
        configureMenuGrid();
    }

    private void configureForm() {
        form.setWidth("50em");
        form.addListener(MenuForm.SaveEvent.class, this::saveMenu);
        form.addListener(MenuForm.DeleteEvent.class, this::deleteMenu);
        form.addListener(MenuForm.CloseEvent.class, e -> closeEditor());
        form.addListener(MenuForm.AddToCartEvent.class, this::addToCart);
    }

    private void configureMenuGrid() {
        menuGrid.addClassNames("menu-grid");
        menuGrid.setSizeFull();

        menuGrid.setColumns("menuId", "menuTitle", "menuIngredients", "menuPrice");
        menuGrid.getColumnByKey("menuId").setHeader("Nr.");
        menuGrid.getColumnByKey("menuTitle").setHeader("Menübezeichnung");
        menuGrid.getColumnByKey("menuIngredients").setHeader("Zutaten");
        menuGrid.getColumnByKey("menuPrice")
                .setHeader("Preis")
                .setTextAlign(ColumnTextAlign.CENTER)
                .setRenderer(new ComponentRenderer<>(menu -> {
                    if (menu != null && menu.getMenuPrice() != null) {
                        return new Text(String.format("%.2f %s", menu.getMenuPrice(), " €"));
                    }
                    return new Text("-");
                }));


        menuGrid.getColumns().forEach(col -> col.setAutoWidth(true));
        menuGrid.asSingleSelect().addValueChangeListener(event -> editMenu(event.getValue()));
    }

    private HorizontalLayout getContent() {
        HorizontalLayout content = new HorizontalLayout(menuGrid, form);
        content.setFlexGrow(2, menuGrid);
        content.setFlexGrow(1, form);
        content.addClassNames("content");
        content.setSizeFull();
        return content;
    }

    private HorizontalLayout getToolbar() {
        filterText.setPlaceholder("Suche in der Speisekarte... ");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        filterText.addValueChangeListener(e -> updateList());
        filterText.setWidth("500px");

        HorizontalLayout toolbar = new HorizontalLayout(filterText);
        toolbar.addClassName("toolbar");
        toolbar.setWidthFull();
        toolbar.setAlignItems(Alignment.CENTER);

        if (isUserInRole("ROLE_ADMIN")) {
            Button addMenubutton = new Button("Menü hinzufügen");
            addMenubutton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            addMenubutton.addClickListener(click -> addMenu());

            // Spacer hinzufügen, um den Button rechts auszurichten
            Div spacer = new Div();
            toolbar.add(spacer);
            toolbar.setFlexGrow(1, spacer);
            toolbar.add(addMenubutton);
        }

        return toolbar;
    }

    public void editMenu(Menu menu) {
        if (menu == null) {
            closeEditor();
        } else {
            form.setMenu(menu);
            form.setVisible(true);
            addClassName("editing");
        }
    }

    private void saveMenu(MenuForm.SaveEvent event) {
        menuService.saveMenu(event.getMenu());
        updateList();
        closeEditor();
    }

    private void deleteMenu(MenuForm.DeleteEvent event) {
        menuService.deleteMenu(event.getMenu());
        updateList();
        closeEditor();
    }

    private void addToCart(MenuForm.AddToCartEvent event) {
        Menu menu = event.getMenu();
        UserDetails user = securityService.getAuthenticatedUser();

        if (user != null) {
            Cart cart = cartService.getOrCreateCart(user.getUsername());

            if (cart != null) {
                cartService.addMenuToCart(cart, menu);

                Notification notification = new Notification(
                        "Menü zum Warenkorb hinzugefügt",
                        3000,
                        Notification.Position.MIDDLE);
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                notification.open();
            }
        }

        closeEditor();
    }

    private void closeEditor() {
        form.setMenu(null);
        form.setVisible(false);
        removeClassName("editing");
    }

    private void addMenu() {
        menuGrid.asSingleSelect().clear();
        editMenu(new Menu());
    }

    private void updateList() {
        menuGrid.setItems(menuService.findAllMenus(filterText.getValue()));
    }

    private boolean isUserInRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(role));
    }
}