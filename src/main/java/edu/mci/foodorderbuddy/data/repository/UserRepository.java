package edu.mci.foodorderbuddy.data.repository;

import edu.mci.foodorderbuddy.data.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    //Optional<UserEntity> findByUsername(String userName);
}