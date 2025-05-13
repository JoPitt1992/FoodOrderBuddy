package edu.mci.foodorderbuddy.service;

import edu.mci.foodorderbuddy.data.entity.Person;
import edu.mci.foodorderbuddy.data.repository.UserRepository;
import org.springframework.stereotype.Service;


@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void updateUser(Person updatedUser) {
        userRepository.save(updatedUser);
    }
}