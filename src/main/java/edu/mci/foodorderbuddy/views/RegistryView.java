package edu.mci.foodorderbuddy.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import edu.mci.foodorderbuddy.data.entity.Person;
import edu.mci.foodorderbuddy.data.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;

@Route("registry")
@PageTitle("Registrieren | FoodOrder Buddy")
@AnonymousAllowed
public class RegistryView extends VerticalLayout {

    @Autowired
    public RegistryView(PersonRepository personRepository) {
        // Gesamtlayout
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        getStyle().set("background-color", "#f9f9f9");

        // Überschrift
        H1 title = new H1("Registrierung");
        title.getStyle().set("text-align", "center");

        // Textfelder
        TextField firstNameField = new TextField("Vorname");
        TextField lastNameField = new TextField("Nachname");
        EmailField emailField = new EmailField("E-Mail");

        TextField usernameField = new TextField("Benutzername");
        PasswordField passwordField = new PasswordField("Passwort");
        PasswordField confirmPasswordField = new PasswordField("Passwort wiederholen");

        // Zwei Spalten nebeneinander
        VerticalLayout leftColumn = new VerticalLayout(firstNameField, lastNameField, emailField);
        VerticalLayout rightColumn = new VerticalLayout(usernameField, passwordField, confirmPasswordField);

        for (VerticalLayout col : new VerticalLayout[]{leftColumn, rightColumn}) {
            col.setSpacing(true);
            col.setPadding(false);
            col.setAlignItems(Alignment.STRETCH);
            col.setWidth("250px");
        }

        HorizontalLayout formLayout = new HorizontalLayout(leftColumn, rightColumn);
        formLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        formLayout.setAlignItems(Alignment.START);
        formLayout.setSpacing(true);
        formLayout.setMargin(true);

        // Registrieren-Button
        Button registerButton = new Button("Registrieren", event -> {
            if (firstNameField.isEmpty() || lastNameField.isEmpty() || usernameField.isEmpty()
                    || emailField.isEmpty() || passwordField.isEmpty() || confirmPasswordField.isEmpty()) {
                Notification.show("Bitte alle Felder ausfüllen.");
                return;
            }

            if (!passwordField.getValue().equals(confirmPasswordField.getValue())) {
                Notification.show("Passwörter stimmen nicht überein.");
                return;
            }

            Person person = new Person();
            person.setPersonFirstName(firstNameField.getValue());
            person.setPersonLastName(lastNameField.getValue());
            person.setPersonUserName(usernameField.getValue());
            person.setPersonEmail(emailField.getValue());
            person.setPersonPassword(passwordField.getValue());
            person.setPersonRole("ROLE_USER");

            try {
                personRepository.save(person);
                Notification.show("Registrierung erfolgreich!", 3000, Notification.Position.MIDDLE);
                UI.getCurrent().navigate("login");
            } catch (Exception e) {
                e.printStackTrace();
                Notification.show("Registrierung fehlgeschlagen: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
            }
        });

        // Button mittig
        HorizontalLayout buttonLayout = new HorizontalLayout(registerButton);
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        buttonLayout.setPadding(true);

        // Alles direkt zur RegistryView hinzufügen
        add(title, formLayout, buttonLayout);
    }
}
