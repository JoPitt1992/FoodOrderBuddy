package edu.mci.foodorderbuddy.it.elements;


import edu.mci.foodorderbuddy.data.entity.Menu;
import edu.mci.foodorderbuddy.data.repository.MenuRepository;
import edu.mci.foodorderbuddy.service.MenuService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MenuServiceTest {

    @Mock
    private MenuRepository menuRepository;

    @InjectMocks
    private MenuService menuService;

    private Menu pizza;
    private Menu salad;

    @BeforeEach
    public void setup() {
        // Testdaten initialisieren
        pizza = new Menu();
        pizza.setMenuTitle("Pizza Margherita");
        pizza.setMenuPrice(8.99);

        salad = new Menu();
        salad.setMenuTitle("Caesar Salad");
        salad.setMenuPrice(5.99);
    }

    // ---- Tests für findAllMenus() ----
    @Test
    public void testFindAllMenus_NoFilter() {
        // Arrange
        when(menuRepository.findAll()).thenReturn(Arrays.asList(pizza, salad));

        // Act
        List<Menu> result = menuService.findAllMenus(null);

        // Assert
        assertEquals(2, result.size());
        verify(menuRepository).findAll();
        verify(menuRepository, never()).search(anyString());
    }

    @Test
    public void testFindAllMenus_WithFilter() {
        // Arrange
        String filter = "Pizza";
        when(menuRepository.search(filter)).thenReturn(Arrays.asList(pizza));

        // Act
        List<Menu> result = menuService.findAllMenus(filter);

        // Assert
        assertEquals(1, result.size());
        assertEquals("Pizza Margherita", result.get(0).getMenuTitle());
        verify(menuRepository).search(filter);
        verify(menuRepository, never()).findAll();
    }

    // ---- Tests für countMenu() ----
    @Test
    public void testCountMenu() {
        // Arrange
        when(menuRepository.count()).thenReturn(5L);

        // Act
        long count = menuService.countMenu();

        // Assert
        assertEquals(5L, count);
        verify(menuRepository).count();
    }

    // ---- Tests für deleteMenu() ----
    @Test
    public void testDeleteMenu() {
        // Act
        menuService.deleteMenu(pizza);

        // Assert
        verify(menuRepository).delete(pizza);
    }

    // ---- Tests für saveMenu() ----
    @Test
    public void testSaveMenu_ValidMenu() {
        // Act
        menuService.saveMenu(salad);

        // Assert
        verify(menuRepository).save(salad);
    }

    @Test
    public void testSaveMenu_NullMenu() {
        // Act
        menuService.saveMenu(null);

        // Assert
        verify(menuRepository, never()).save(any());
    }
}