package edu.mci.foodorderbuddy.data.repository;

import edu.mci.foodorderbuddy.data.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.List;
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


    //Dashboard

    @Query("SELECT SUM(c.cartPrice) FROM Cart c WHERE c.cartPaydate BETWEEN :startDate AND :endDate")
    Double findMonthlyRevenue(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    @Query("SELECT SUM(c.cartPrice) FROM Cart c WHERE c.cartPaydate BETWEEN :startDate AND :endDate")
    Double findYearlyRevenue(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    @Query("SELECT COUNT(c) FROM Cart c WHERE c.cartPaydate BETWEEN :startDate AND :endDate")
    Long countOrdersInMonth(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    @Query("SELECT COUNT(c) FROM Cart c WHERE c.cartPaydate BETWEEN :startDate AND :endDate")
    Long countOrdersInYear(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    @Query("SELECT m, COUNT(m) as count FROM Cart c JOIN c.cartList m GROUP BY m ORDER BY count DESC")
    List<Object[]> findTop3MostSoldDishes();

    @Query("SELECT m, COUNT(m) as count FROM Cart c JOIN c.cartList m GROUP BY m ORDER BY count ASC")
    List<Object[]> findTop3LeastSoldDishes();


    List<Cart> findAll();

}