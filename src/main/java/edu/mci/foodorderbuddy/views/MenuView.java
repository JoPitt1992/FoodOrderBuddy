package edu.mci.foodorderbuddy.views;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import edu.mci.foodorderbuddy.data.entity.Menu;
import edu.mci.foodorderbuddy.service.MenuService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@RolesAllowed({"ROLE_USER", "ROLE_ADMIN"})
@Route(value = "menu", layout = MainLayout.class)
@PageTitle("Speisekarte | Food Order Buddy")
public class MenuView extends VerticalLayout {
    Grid<Menu> menuGrid = new Grid<>(Menu.class);
    TextField filterText = new TextField();
    MenuForm form;
    MenuService service;

    public MenuView(MenuService service) {
        this.service = service;
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
    }

    private void configureMenuGrid() {
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
        filterText.setPlaceholder("Finde dein Lieblingsmenü... ");
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
            toolbar.add(new com.vaadin.flow.component.HtmlComponent("div"));
            toolbar.setFlexGrow(1, toolbar.getComponentAt(1));
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
        service.saveMenu(event.getMenu());
        updateList();
        closeEditor();
    }

    private void deleteMenu(MenuForm.DeleteEvent event) {
        service.deleteMenu(event.getMenu());
        updateList();
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
        menuGrid.setItems(service.findAllMenus(filterText.getValue()));
    }

    private boolean isUserInRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(role));
    }
}