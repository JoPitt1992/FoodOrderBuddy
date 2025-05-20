package edu.mci.foodorderbuddy.views.list;

import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.testbench.unit.UIUnitTest;
import edu.mci.foodorderbuddy.data.entity.Person;
import edu.mci.foodorderbuddy.service.PersonService;
import edu.mci.foodorderbuddy.views.PersonDataView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class PersonDataViewTest extends UIUnitTest {

    private PersonService mockService;
    private Person testPerson;

    @BeforeEach
    public void setup() {
        // Setup Security Context
        UserDetails userDetails = User.withUsername("testuser").password("password").roles("USER").build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));

        // Mock PersonService
        mockService = mock(PersonService.class);
        testPerson = new Person();
        testPerson.setPersonFirstName("Test");
        testPerson.setPersonUserName("testuser");

        when(mockService.findByUsername("testuser")).thenReturn(Optional.of(testPerson));
    }

    @Test
    public void view_shouldInitializeWithPersonData() {
        PersonDataView view = new PersonDataView(mockService);
        TextField firstNameField = view.getPersonFirstNameField();
        assertEquals("Test", firstNameField.getValue());
        assertEquals(testPerson, view.getCurrentPerson());
    }

    @Test
    public void save_shouldInvokeUpdateUser() {
        PersonDataView view = new PersonDataView(mockService);
        view.saveProfileChanges();
        verify(mockService, times(1)).updateUser(any(Person.class));
    }

    @Test
    public void view_shouldHandleMissingUserGracefully() {
        when(mockService.findByUsername("testuser"))
                .thenReturn(Optional.empty());

        PersonDataView view = new PersonDataView(mockService);
        assertNull(view.getCurrentPerson());
    }
}
