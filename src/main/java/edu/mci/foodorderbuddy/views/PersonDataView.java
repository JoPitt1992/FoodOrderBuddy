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

    protected final TextField personFirstName = new TextField("Vorname *");
    protected final TextField personLastName = new TextField("Nachname *");
    protected final TextField personUserName = new TextField("Username *");
    protected final TextField personEmail = new TextField("E-Mail Adresse *");
    protected final TextField personAddress = new TextField("Adresse");
    protected final TextField personPostalCode = new TextField("Postleitzahl");
    protected final TextField personCity = new TextField("Stadt");
    protected final TextField personPhonenumber = new TextField("Telefonnummer");
    protected final Button save = new Button("Benutzerdaten aktualisieren");

    protected final Binder<Person> binder = new Binder<>(Person.class);
    protected final PersonService personService;
    protected Person currentPerson;

    public PersonDataView(PersonService personService) {
        this.personService = personService;
        add(header());
        add(createFormLayout());
        add(createSaveButton());

        initUserData();
        save.addClickListener(event -> saveProfileChanges());
    }

    protected void initUserData() {
        String username = getCurrentUsername();
        Optional<Person> optionalPerson = personService.findByUsername(username);

        if (optionalPerson.isPresent()) {
            currentPerson = optionalPerson.get();
            bindFields();
            binder.setBean(currentPerson);
            save.setEnabled(true);
        } else {
            Notification.show("Benutzer nicht gefunden").addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    protected String getCurrentUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        return "anonymous";
    }

    protected void bindFields() {
        binder.bindInstanceFields(this);
    }

    protected Component header() {
        H2 header = new H2("Benutzerdaten bearbeiten");
        header.getStyle().set("text-align", "center");
        header.setWidthFull();
        return header;
    }

    protected Component createFormLayout() {
        FormLayout formLayout = new FormLayout();
        formLayout.add(
                personFirstName, personLastName,
                personEmail, personAddress,
                personPostalCode, personCity,
                personPhonenumber
        );
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));
        formLayout.setWidth("700px");
        formLayout.getStyle().set("margin", "0 auto");
        return formLayout;
    }

    protected Button createSaveButton() {
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        save.setWidth("700px");
        save.getStyle().set("margin", "0 auto");
        return save;
    }

    public void saveProfileChanges() {
        if (binder.validate().isOk()) {
            personService.updateUser(binder.getBean());
            binder.readBean(currentPerson);
            save.setEnabled(false);
            Notification.show("Benutzerdaten erfolgreich aktualisiert")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } else {
            Notification.show("Bitte alle Felder korrekt ausfüllen")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    // For test access
    public TextField getPersonFirstNameField() { return personFirstName; }
    public Button getSaveButton() { return save; }
    public Binder<Person> getBinder() { return binder; }
    public Person getCurrentPerson() { return currentPerson; }
}
