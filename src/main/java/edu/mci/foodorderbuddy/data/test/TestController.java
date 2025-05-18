package edu.mci.foodorderbuddy.data.test;

import edu.mci.foodorderbuddy.data.entity.Person;
import edu.mci.foodorderbuddy.data.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @Autowired
    PersonRepository repo;

    @GetMapping("/db-test")
    public String test() {
        repo.save(new Person("Max", "Mustermann", "mmuster", "max@example.com", "letmepass"));
        return "Verbindung und Save erfolgreich!";
    }
}