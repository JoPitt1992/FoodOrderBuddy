package edu.mci.foodorderbuddy.data.repository;

import edu.mci.foodorderbuddy.data.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByOwnerPersonUserName(String personUserName);
}