package edu.mci.foodorderbuddy.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.validator.DoubleRangeValidator;
import com.vaadin.flow.shared.Registration;
import edu.mci.foodorderbuddy.data.entity.Menu;

public class MenuForm extends FormLayout {
    TextField menuTitle = new TextField("Menübezeichnung");
    TextField menuIngredients = new TextField("Zutaten");
    NumberField menuPrice = new NumberField("Preis");

    Button save = new Button("Speichern");
    Button delete = new Button("Löschen");   // Optional: z. B. für Admins
    Button close = new Button("Abbrechen");

    private Menu menu;

    Binder<Menu> binder = new Binder<>(Menu.class);

    public MenuForm() {
        addClassName("menu-form");

        menuPrice.setMin(0.01);
        menuPrice.setStep(0.01);
        binder.forField(menuPrice)
                .withValidator(new DoubleRangeValidator("Preis muss größer als 0 sein", 0.01, null))
                .bind(Menu::getMenuPrice, Menu::setMenuPrice);

        binder.bindInstanceFields(this);

        add(menuTitle,
                menuIngredients,
                menuPrice,
                createButtonsLayout());
    }

    public void setMenu(Menu menu) {
        this.menu = menu;
        binder.readBean(menu);
    }

    private Component createButtonsLayout() {
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
        close.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        save.addClickShortcut(Key.ENTER);
        close.addClickShortcut(Key.ESCAPE);

        save.addClickListener(event -> validateAndSave());
        delete.addClickListener(event -> fireEvent(new DeleteEvent(this, menu)));
        close.addClickListener(event -> fireEvent(new CloseEvent(this)));

        binder.addStatusChangeListener(e -> save.setEnabled(binder.isValid()));
        return new HorizontalLayout(save, delete, close);
    }

    private void validateAndSave() {
        try {
            binder.writeBean(menu);
            fireEvent(new SaveEvent(this, menu));
        } catch (ValidationException e) {
            e.printStackTrace();
        }
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

    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType,
                                                                  ComponentEventListener<T> listener) {
        return getEventBus().addListener(eventType, listener);
    }
}
