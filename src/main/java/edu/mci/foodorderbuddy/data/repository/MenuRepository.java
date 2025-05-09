package edu.mci.foodorderbuddy.data.repository;

import edu.mci.foodorderbuddy.data.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Long> {
    @Query("select m from Menu m " +
            "where lower(m.menuName) like lower(concat('%', :searchTerm, '%')) " +
            "or lower(m.menuIngredients) like lower(concat('%', :searchTerm, '%'))")
    List<Menu> search(@Param("searchTerm") String searchTerm);
}
