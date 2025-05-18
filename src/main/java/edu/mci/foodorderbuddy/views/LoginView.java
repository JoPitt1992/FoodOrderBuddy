package edu.mci.foodorderbuddy.views;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import edu.mci.foodorderbuddy.data.repository.PersonRepository;
import edu.mci.foodorderbuddy.data.test.DbInitializer;
import org.springframework.beans.factory.annotation.Autowired;

@Route("login")
@PageTitle("Login | FoodOrder Buddy - Now on VM")
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

        add(new H1("Food-Order-Buddy - Now On VM - After Image Deletion N1"), login);

        DbInitializer dbInitializer = new DbInitializer(personRepository);
        this.personRepository = personRepository;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        // inform the user about an authentication error
        if(beforeEnterEvent.getLocation()
                .getQueryParameters()
                .getParameters()
                .containsKey("error")) {
            login.setError(true);
        }
    }
}