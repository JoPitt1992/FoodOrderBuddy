package edu.mci.foodorderbuddy.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import edu.mci.foodorderbuddy.data.entity.OrderHistory;
import edu.mci.foodorderbuddy.data.entity.Person;

import java.util.Optional;

@Repository
public interface OrderHistoryRepository extends JpaRepository<OrderHistory, Long> {
    Optional<OrderHistory> findByPerson(Person person);
}