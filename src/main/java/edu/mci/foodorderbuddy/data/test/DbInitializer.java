package edu.mci.foodorderbuddy.data.test;

import edu.mci.foodorderbuddy.data.entity.Person;
import edu.mci.foodorderbuddy.data.repository.PersonRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DbInitializer {

    private final PersonRepository personRepository;

    @Autowired
    public DbInitializer(PersonRepository personRepository) {
        this.personRepository = personRepository;

    }

    @PostConstruct
    public void initializeDatabase() {
        if (isDatabaseEmpty()) {
            addDummyData();
        }
    }

    private boolean isDatabaseEmpty() {
        return personRepository.count() == 0;
    }

    private void addDummyData() {
        Person p = new Person();
        p.setPersonFirstName("Hans");
        p.setPersonLastName("Peter");
        p.setPersonUserName("Pez");
        p.setPersonAddress("Hinterberg 13");
        p.setPersonPostalCode(123);
        p.setPersonCity("Innsbruck");
        p.setPersonEmail("asdfasfd@swd.de");
        p.setPersonPhonenumber("066534234");
        p.setRole("User");

        personRepository.save(p);
    }
}