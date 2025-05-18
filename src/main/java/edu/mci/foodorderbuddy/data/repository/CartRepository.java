package edu.mci.foodorderbuddy.data.repository;

import edu.mci.foodorderbuddy.data.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    // Existierende Methode
    Optional<Cart> findByOwnerPersonUserName(String personUserName);

    // Alternative mit expliziter Query
    @Query("SELECT c FROM Cart c WHERE c.owner.personUserName = :username AND (c.cartPayed = false OR c.cartPayed IS NULL)")
    Optional<Cart> findActiveCartByUsername(@Param("username") String username);

    // Einfachere Methode ohne verschachtelte Pfade
    @Query("SELECT c FROM Cart c JOIN c.owner p WHERE p.personUserName = :username")
    Optional<Cart> findCartByUsername(@Param("username") String username);
}