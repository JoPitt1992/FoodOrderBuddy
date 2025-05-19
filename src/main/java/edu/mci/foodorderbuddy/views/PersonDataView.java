package edu.mci.foodorderbuddy.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.converter.StringToIntegerConverter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import edu.mci.foodorderbuddy.data.entity.Person;
import edu.mci.foodorderbuddy.service.PersonService;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

@PermitAll
@Route(value = "userdata", layout = MainLayout.class)
@PageTitle("My Profile | Food Order Buddy")
public class PersonDataView extends VerticalLayout {

    private final TextField personFirstName = new TextField("Vorname *");
    private final TextField personLastName = new TextField("Nachname *");
    private final TextField personUserName = new TextField("Username *");
    private final TextField personEmail = new TextField("E-Mail Adresse *");
    private final TextField personAddress = new TextField("Adresse");
    private final TextField personPostalCode = new TextField("Postleitzahl");
    private final TextField personCity = new TextField("Stadt");
    private final TextField personPhonenumber = new TextField("Telefonnummer");
    private final Button save = new Button("Benutzerdaten aktualisieren");

    private final Binder<Person> binder = new Binder<>(Person.class);
    private final PersonService personService;
    private Person currentPerson;

    public PersonDataView(PersonService personService) {
        this.personService = personService;
        add(header());
        add(createFormLayout());
        add(createSaveButton());

        // Hole aktuell eingeloggten Benutzer
        String username = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        Optional<Person> optionalPerson = personService.findByUsername(username);

        if (optionalPerson.isPresent()) {
            currentPerson = optionalPerson.get();

            binder.forField(personFirstName).bind(Person::getPersonFirstName, Person::setPersonFirstName);
            binder.forField(personLastName).bind(Person::getPersonLastName, Person::setPersonLastName);
            binder.forField(personUserName).bind(Person::getPersonUserName, Person::setPersonUserName);
            binder.forField(personEmail).bind(Person::getPersonEmail, Person::setPersonEmail);
            binder.forField(personPostalCode).bind(Person::getPersonPostalCode, Person::setPersonPostalCode);
            binder.forField(personAddress).bind(Person::getPersonAddress, Person::setPersonAddress);
            binder.forField(personCity).bind(Person::getPersonCity, Person::setPersonCity);
            binder.forField(personPhonenumber).bind(Person::getPersonPhonenumber, Person::setPersonPhonenumber);

            binder.setBean(currentPerson);
            save.setEnabled(true); // standardmäßig deaktiviert
        } else {
            Notification.show("Benutzer nicht gefunden").addThemeVariants(NotificationVariant.LUMO_ERROR);
        }

        save.addClickListener(event -> saveProfileChanges());
    }

    private Component header() {
        H2 header = new H2("Benutzerdaten bearbeiten");
        header.getStyle().set("text-align", "center");
        header.setWidthFull();
        return header;
    }

    private Component createFormLayout() {
        FormLayout formLayout = new FormLayout();
        formLayout.add(personFirstName, personLastName,
                personEmail, personAddress,
                personPostalCode, personCity,
                personPhonenumber
                );
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 2)
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
            binder.readBean(currentPerson); // Änderungen neu einlesen
            save.setEnabled(false); // Button wieder deaktivieren
            Notification.show("Benutzerdaten erfolgreich aktualisiert")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } else {
            Notification.show("Bitte alle Felder korrekt ausfüllen")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}
