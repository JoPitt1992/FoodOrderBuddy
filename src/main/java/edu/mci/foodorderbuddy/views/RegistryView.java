package edu.mci.foodorderbuddy.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
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
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        TextField firstNameField = new TextField("Vorname");
        TextField lastNameField = new TextField("Nachname");
        EmailField emailField = new EmailField("E-Mail");
        TextField usernameField = new TextField("Benutzername");
        PasswordField passwordField = new PasswordField("Passwort");
        PasswordField confirmPasswordField = new PasswordField("Passwort wiederholen");

        Button registerButton = new Button("Registrieren", event -> {
            if (firstNameField.isEmpty() || lastNameField.isEmpty() || usernameField.isEmpty() || emailField.isEmpty() || passwordField.isEmpty() || confirmPasswordField.isEmpty()) {
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
                e.printStackTrace(); // Zum Debuggen in der Konsole
                Notification.show("Registrierung fehlgeschlagen: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
            }
        });

        add(
                new H1("Registrierung"),
                firstNameField,
                lastNameField,
                usernameField,
                emailField,
                passwordField,
                confirmPasswordField,
                registerButton
        );
    }
}