package edu.mci.foodorderbuddy.service;

import edu.mci.foodorderbuddy.data.entity.Person;
import edu.mci.foodorderbuddy.data.repository.PersonRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PersonService {

    private final PersonRepository personRepository;

    public PersonService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    public void updateUser(Person updatedUser) {
        if (updatedUser == null || updatedUser.getPersonId() == null) {
            throw new IllegalArgumentException("Person or ID must not be null");
        }
        personRepository.save(updatedUser);
    }

    public Optional<Person> findByUsername(String username) {
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }
        return personRepository.findByPersonUserName(username);
    }

    public boolean userExists(String username) {
        return findByUsername(username).isPresent();
    }
}
