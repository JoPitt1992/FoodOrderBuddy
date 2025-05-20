package edu.mci.foodorderbuddy.views;

import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import edu.mci.foodorderbuddy.data.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;

@Route("login")
@PageTitle("Login | FoodOrder Buddy")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final LoginForm login = new LoginForm();
    private final PersonRepository personRepository;

    @Autowired
    public LoginView(PersonRepository personRepository){
        addClassName("login-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        login.setAction("login");

        // Important: Make sure this is set to handle the redirect after login
        login.setForgotPasswordButtonVisible(false);

        // Logo hinzufügen
        Image logo = new Image("icons/icon.png", "Food Order Buddy Logo");
        logo.setHeight("200px"); // Höhe anpassen nach Bedarf
        logo.getStyle().set("margin-bottom", "20px"); // Etwas Abstand zum Login-Formular

        Anchor registryLink = new Anchor("registry", "Neu registrieren");

        // H2-Überschrift entfernt, nur Logo, Login-Formular und Registrierungslink
        add(logo,
            login,
            registryLink);

        this.personRepository = personRepository;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        if(beforeEnterEvent
                .getLocation()
                .getQueryParameters()
                .getParameters()
                .containsKey("error")) {
            login.setError(true);
        }
    }
}