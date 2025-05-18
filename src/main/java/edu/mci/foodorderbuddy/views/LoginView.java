package edu.mci.foodorderbuddy.views;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import edu.mci.foodorderbuddy.data.repository.PersonRepository;
import edu.mci.foodorderbuddy.data.test.DbInitializer;
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
        Anchor registryLink = new Anchor("registry", "Neu registrieren");

        add(new H2("Food-Order-Buddy"),
                login,
                registryLink);

        // DbInitializer dbInitializer = new DbInitializer(personRepository);
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