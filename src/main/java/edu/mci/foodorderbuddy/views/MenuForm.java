package edu.mci.foodorderbuddy.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.validator.DoubleRangeValidator;
import com.vaadin.flow.shared.Registration;
import edu.mci.foodorderbuddy.data.entity.Menu;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class MenuForm extends FormLayout {
    TextField menuTitle = new TextField("Menübezeichnung");
    TextField menuIngredients = new TextField("Zutaten");
    NumberField menuPrice = new NumberField("Preis");
    Checkbox menuDaily = new Checkbox("Tagesmenü");

    Button save = new Button("Speichern");
    Button delete = new Button("Löschen");
    Button close = new Button("Abbrechen");
    Button addToCart = new Button("In den Warenkorb");

    private Menu menu;

    Binder<Menu> binder = new Binder<>(Menu.class);

    public MenuForm() {
        addClassName("menu-form");
        menuPrice.setMin(0.01);
        menuPrice.setStep(0.01);
        binder.forField(menuPrice)
                .withValidator(new DoubleRangeValidator("Preis muss größer als 0 sein", 0.01, null))
                .bind(Menu::getMenuPrice, Menu::setMenuPrice);

        binder.forField(menuDaily)
                .bind(Menu::isMenuDaily, Menu::setMenuDaily);
        binder.bindInstanceFields(this);

        add(menuTitle, menuIngredients, menuPrice, menuDaily, createButtonsLayout());
    }

    public void setMenu(Menu menu) {
        this.menu = menu;
        binder.readBean(menu);

        if (isUserInRole("ROLE_ADMIN")) {
            menuTitle.setReadOnly(false);
            menuIngredients.setReadOnly(false);
            menuPrice.setReadOnly(false);
            menuDaily.setEnabled(true);

            save.setVisible(true);
            delete.setVisible(true);
            addToCart.setVisible(true);

        } else {
            // für User nur Read-Only Textfelder
            menuTitle.setReadOnly(true);
            menuIngredients.setReadOnly(true);
            menuPrice.setReadOnly(true);
            menuDaily.setEnabled(false);

            save.setVisible(false);
            delete.setVisible(false);
            addToCart.setVisible(true);
        }
    }

    private Component createButtonsLayout() {
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
        close.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        addToCart.addThemeVariants(ButtonVariant.LUMO_SUCCESS);

        save.addClickShortcut(Key.ENTER);
        close.addClickShortcut(Key.ESCAPE);

        save.addClickListener(event -> validateAndSave());
        delete.addClickListener(event -> fireEvent(new DeleteEvent(this, menu)));
        close.addClickListener(event -> fireEvent(new CloseEvent(this)));
        addToCart.addClickListener(event -> fireEvent(new AddToCartEvent(this, menu)));

        binder.addStatusChangeListener(e -> save.setEnabled(binder.isValid()));

        // Hintergrundfarbe für das gesamte Formular setzen
        getElement().getStyle().set("background-color", "#f5f5f5");
        getElement().getStyle().set("padding", "1rem");
        getElement().getStyle().set("border-radius", "12px");

        // Buttons Breite und Margin setzen
        addToCart.setWidth("100%");

        save.setWidth("30%");
        delete.setWidth("30%");
        close.setWidth("30%");

        // Manuelle Abstände zwischen Admin-Buttons (5% Margin rechts für save und delete)
        save.getStyle().set("margin-right", "5%");
        delete.getStyle().set("margin-right", "5%");
        close.getStyle().remove("margin-right");


        // Oberste Zeile: Warenkorb-Button (100% Breite)
        HorizontalLayout topRow = new HorizontalLayout(addToCart);
        topRow.setWidthFull();
        topRow.setPadding(false);
        topRow.setMargin(false);
        topRow.setSpacing(false);

        // Untere Zeile: Admin Buttons (save, delete, close) mit Abstand
        HorizontalLayout bottomRow = new HorizontalLayout(save, delete, close);
        bottomRow.setWidthFull();
        bottomRow.setPadding(false);
        bottomRow.setMargin(false);
        bottomRow.setSpacing(false);

        // Vertikal kombinieren
        VerticalLayout buttonLayout = new VerticalLayout(topRow, bottomRow);
        buttonLayout.setPadding(false);
        buttonLayout.setSpacing(true);
        buttonLayout.setWidthFull();

        buttonLayout.getStyle().set("margin-top", "var(--lumo-space-m)");

        return buttonLayout;
    }


    private void validateAndSave() {
        try {
            binder.writeBean(menu);
            fireEvent(new SaveEvent(this, menu));
        } catch (ValidationException e) {
            e.printStackTrace();
        }
    }

    private boolean isUserInRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(role));
    }

    public static abstract class MenuFormEvent extends ComponentEvent<MenuForm> {
        private final Menu menu;

        protected MenuFormEvent(MenuForm source, Menu menu) {
            super(source, false);
            this.menu = menu;
        }

        public Menu getMenu() {
            return menu;
        }
    }

    public static class SaveEvent extends MenuFormEvent {
        SaveEvent(MenuForm source, Menu menu) {
            super(source, menu);
        }
    }

    public static class DeleteEvent extends MenuFormEvent {
        DeleteEvent(MenuForm source, Menu menu) {
            super(source, menu);
        }
    }

    public static class CloseEvent extends MenuFormEvent {
        CloseEvent(MenuForm source) {
            super(source, null);
        }
    }

    public static class AddToCartEvent extends MenuFormEvent {
        AddToCartEvent(MenuForm source, Menu menu) {
            super(source, menu);
        }
    }

    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType, ComponentEventListener<T> listener) {
        return getEventBus().addListener(eventType, listener);
    }
}