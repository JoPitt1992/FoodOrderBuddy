package edu.mci.foodorderbuddy.data.repository;

import edu.mci.foodorderbuddy.data.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonRepository extends JpaRepository<Person, Long> {
    //Optional<UserEntity> findByUsername(String userName);
}