package edu.mci.foodorderbuddy.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import edu.mci.foodorderbuddy.data.entity.Person;
import edu.mci.foodorderbuddy.service.PersonService;
import jakarta.annotation.security.PermitAll;

@PermitAll
@Route(value = "userprofile", layout = MainLayout.class)
@PageTitle("My Profile | Food Order Buddy")
public class PersonDataView extends VerticalLayout {

    private final TextField personFirstName = new TextField("Vorname");
    private final TextField personLastName = new TextField("Nachname");
    private final TextField personUserName = new TextField("Username");
    private final TextField personEmail = new TextField("E-Mail Adresse");
    private final TextField personAddress = new TextField("Adresse");
    private final TextField personPostalCode = new TextField("Postleitzahl");
    private final TextField personCity = new TextField("Stadt");
    private final TextField personPhonenumber = new TextField("Telefonnummer");
    private final Button save = new Button("Benutzerdaten aktualisieren");

    private final Binder<Person> binder = new Binder<>(Person.class);
    private final PersonService personService;

    public PersonDataView(PersonService personService) {
        this.personService = personService;

        add(createFormLayout());
        add(createSaveButton());

        binder.bindInstanceFields(this);

        //UserEntity currentUser = userService.getCurrentUser();
        //binder.setBean(currentUser);

        save.addClickListener(event -> saveProfileChanges());
    }

    // Darstellung der Felder in 2 Spalten
    private Component createFormLayout() {
        FormLayout formLayout = new FormLayout();
        formLayout.add(personFirstName, personLastName,
                personUserName, personAddress,
                personPostalCode, personCity,
                personEmail, personPhonenumber);
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 2) // 2 columns on all screen sizes
        );
        formLayout.setWidth("700px");
        formLayout.getStyle().set("margin", "0 auto");

        return formLayout;
    }

    private Button createSaveButton() {
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        save.setWidth("700px");
        save.getStyle().set("margin", "0 auto");
        return save;
    }

    private void saveProfileChanges() {
        if (binder.validate().isOk()) {
            personService.updateUser(binder.getBean());
            Notification.show("Benutzerdaten erfolgreich aktualisiert")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } else {
            Notification.show("Bitte alle Felder korrekt ausfüllen")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}