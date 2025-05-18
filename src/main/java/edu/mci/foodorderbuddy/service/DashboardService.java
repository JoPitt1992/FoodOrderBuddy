package edu.mci.foodorderbuddy.service;

import edu.mci.foodorderbuddy.data.repository.OrderHistoryRepository;
import edu.mci.foodorderbuddy.data.repository.PersonRepository;
import org.springframework.stereotype.Service;


@Service
public class DashboardService {

    private static OrderHistoryRepository orderHistoryRepository;
    private static PersonRepository personRepository;

    public DashboardService(OrderHistoryRepository orderHistoryRepository, PersonRepository personRepository) {
        this.orderHistoryRepository = orderHistoryRepository;
        this.personRepository = personRepository;
    }


}
