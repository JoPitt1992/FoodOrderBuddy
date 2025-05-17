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
        personRepository.save(updatedUser);
    }
}