package edu.mci.foodorderbuddy.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import edu.mci.foodorderbuddy.data.entity.Person;
import edu.mci.foodorderbuddy.service.UserService;
import jakarta.annotation.security.PermitAll;

@PermitAll
@Route(value = "profile", layout = MainLayout.class)
@PageTitle("My Profile | Food Order Buddy")
public class MyProfileView extends VerticalLayout {

    private final TextField firstName = new TextField("First name");
    private final TextField lastName = new TextField("Last name");
    private final TextField userName = new TextField("Username");
    private final TextField email = new TextField("E-Mail");
    private final Button save = new Button("Save");

    private final Binder<Person> binder = new Binder<>(Person.class);
    private final UserService userService;

    public MyProfileView(UserService userService) {
        this.userService = userService;

        add(createFormLayout());
        add(createSaveButton());

        binder.bindInstanceFields(this);

        //UserEntity currentUser = userService.getCurrentUser();
        //binder.setBean(currentUser);

        save.addClickListener(event -> saveProfileChanges());
    }

    private Component createFormLayout() {
        VerticalLayout layout = new VerticalLayout();
        layout.add(firstName, lastName, userName, email);
        layout.setPadding(false);
        layout.setSpacing(true);
        return layout;
    }

    private Button createSaveButton() {
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        return save;
    }

    private void saveProfileChanges() {
        if (binder.validate().isOk()) {
            userService.updateUser(binder.getBean());
            Notification.show("Profile updated successfully")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } else {
            Notification.show("Please fill in all fields correctly")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}