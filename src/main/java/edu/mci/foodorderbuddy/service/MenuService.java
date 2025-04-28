package edu.mci.foodorderbuddy.service;

import edu.mci.foodorderbuddy.data.entity.Menu;
import edu.mci.foodorderbuddy.data.repository.MenuRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MenuService {

    private final MenuRepository menuRepository;

    public MenuService(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    public List<Menu> findAllMenus(String stringFilter) {
        if (stringFilter == null || stringFilter.isEmpty()) {
            return menuRepository.findAll();
        } else {
            return menuRepository.search(stringFilter);
        }
    }

    public long countMenu() {
        return menuRepository.count();
    }

    public void deleteMenu(Menu menu) {
        menuRepository.delete(menu);
    }

    public void saveMenu(Menu menu) {
        if (menu == null) {
            System.err.println("Menu is null. Are you sure you have connected your form to the application?");
            return;
        }
        menuRepository.save(menu);
    }

}