package edu.mci.foodorderbuddy.views;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import edu.mci.foodorderbuddy.data.entity.Menu;
import edu.mci.foodorderbuddy.service.CartService;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

@RolesAllowed({"ROLE_USER", "ROLE_ADMIN"})
@PageTitle("Warenkorb | Food Order Buddy")
@Route(value = "cart", layout = MainLayout.class)
public class CartView extends VerticalLayout {

    private Grid<Menu> menuGrid = new Grid<>(Menu.class);
    private TextField filterText = new TextField();
    private CartService cartService;

    // Beispiel: aktueller Warenkorb, hier noch hartkodiert
    private Long currentCartId = 1L;

    public CartView(CartService cartService) {
        this.cartService = cartService;

        addClassName("list-view");
        setSizeFull();

        configureGrid();

        add(getToolbar(), getContent());
        //updateList();
    }

    private void configureGrid() {
        menuGrid.addClassNames("menu-grid");
        menuGrid.setSizeFull();

        menuGrid.setColumns("menuTitle", "menuIngredients", "menuPrice");
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
    }

    private HorizontalLayout getContent() {
        HorizontalLayout content = new HorizontalLayout(menuGrid);
        content.setFlexGrow(1, menuGrid);
        content.addClassNames("content");
        content.setSizeFull();
        return content;
    }

    private HorizontalLayout getToolbar() {
        filterText.setPlaceholder("Suche im Warenkorb...");
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

    /*private void updateList() {
        String filter = filterText.getValue();
        List<Menu> menus;

        if (filter == null || filter.isEmpty()) {
            menus = cartService.getMenusFromCart(currentCartId);
        } else {
            // Filterung: einfache Filterung nach Menütitel, kannst du nach Bedarf erweitern
            menus = cartService.getMenusFromCart(currentCartId).stream()
                    .filter(menu -> menu.getMenuTitle() != null &&
                            menu.getMenuTitle().toLowerCase().contains(filter.toLowerCase()))
                    .toList();
        }

        menuGrid.setItems(menus);
    }
*/
    private boolean isUserInRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(role));
    }
}