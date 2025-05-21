package edu.mci.foodorderbuddy.it.elements;

import edu.mci.foodorderbuddy.data.repository.CartRepository;
import edu.mci.foodorderbuddy.service.DashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.*;
import java.util.Date;
import static org.junit.jupiter.api.Assertions.*;

public class DashboardServiceDateTest {

    @Mock
    private CartRepository cartRepository; // Gemockt, da nicht benötigt

    @InjectMocks
    private DashboardService dashboardService;

    // Fester Zeitpunkt für konsistente Tests (z. B. 15. Juni 2023)
    private final LocalDate TEST_DATE = LocalDate.of(2023, 6, 15);
    private Clock fixedClock;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        fixedClock = Clock.fixed(TEST_DATE.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        dashboardService = new DashboardService(cartRepository);
    }

    // --- Tests für getMonthlyDateStart() ---
    @Test
    public void testGetMonthlyDateStart_January() throws Exception {
        Method method = DashboardService.class.getDeclaredMethod("getMonthlyDateStart", YearMonth.class);
        method.setAccessible(true);

        YearMonth testMonth = YearMonth.of(2023, 1);
        Date result = (Date) method.invoke(dashboardService, testMonth);

        LocalDateTime expected = LocalDate.of(2023, 1, 1).atStartOfDay();
        assertEquals(expected, toLocalDateTime(result));
    }

    @Test
    public void testGetMonthlyDateStart_December() throws Exception {
        Method method = DashboardService.class.getDeclaredMethod("getMonthlyDateStart", YearMonth.class);
        method.setAccessible(true);

        YearMonth testMonth = YearMonth.of(2023, 12);
        Date result = (Date) method.invoke(dashboardService, testMonth);

        LocalDateTime expected = LocalDate.of(2023, 12, 1).atStartOfDay();
        assertEquals(expected, toLocalDateTime(result));
    }

    // --- Tests für getMonthlyDateEnd() ---
    @Test
    public void testGetMonthlyDateEnd_FebruaryNonLeapYear() throws Exception {
        Method method = DashboardService.class.getDeclaredMethod("getMonthlyDateEnd", YearMonth.class);
        method.setAccessible(true);

        YearMonth testMonth = YearMonth.of(2023, 2);
        Date result = (Date) method.invoke(dashboardService, testMonth);

        LocalDateTime expected = LocalDate.of(2023, 2, 28).atStartOfDay();
        assertEquals(expected, toLocalDateTime(result));
    }

    @Test
    public void testGetMonthlyDateEnd_FebruaryLeapYear() throws Exception {
        Method method = DashboardService.class.getDeclaredMethod("getMonthlyDateEnd", YearMonth.class);
        method.setAccessible(true);

        YearMonth testMonth = YearMonth.of(2023, 2);
        Date result = (Date) method.invoke(dashboardService, testMonth);

        LocalDateTime expected = LocalDate.of(2024, 2, 29).atStartOfDay();
        assertEquals(expected, toLocalDateTime(result));
    }

    // --- Tests für getYearlyDateStart() ---
    @Test
    public void testGetYearlyDateStart_2023() throws Exception {
        Method method = DashboardService.class.getDeclaredMethod("getYearlyDateStart", int.class);
        method.setAccessible(true);
        Date result = (Date) method.invoke(dashboardService, 2023);

        LocalDateTime expected = LocalDate.of(2023, 1, 1).atStartOfDay();
        assertEquals(expected, toLocalDateTime(result));
    }

    @Test
    public void testGetYearlyDateStart_2000() throws Exception {
        Method method = DashboardService.class.getDeclaredMethod("getYearlyDateStart", int.class);
        method.setAccessible(true);
        Date result = (Date) method.invoke(dashboardService, 2020);
        LocalDateTime expected = LocalDate.of(2000, 1, 1).atStartOfDay();
        assertEquals(expected, toLocalDateTime(result));
    }

    // --- Tests für getYearlyDateEnd() ---
    @Test
    public void testGetYearlyDateEnd_2023() throws Exception{
        Method method = DashboardService.class.getDeclaredMethod("getYearlyDateEnd", int.class);
        method.setAccessible(true);
        Date result = (Date) method.invoke(dashboardService, 2023);
        LocalDateTime expected = LocalDate.of(2023, 12, 31).atStartOfDay();
        assertEquals(expected, toLocalDateTime(result));
    }

    @Test
    public void testGetYearlyDateEnd_2000() throws Exception{
        Method method = DashboardService.class.getDeclaredMethod("getYearlyDateEnd", int.class);
        method.setAccessible(true);
        Date result = (Date) method.invoke(dashboardService, 2000);
        LocalDateTime expected = LocalDate.of(2000, 12, 31).atStartOfDay();
        assertEquals(expected, toLocalDateTime(result));
    }

    // --- Hilfsmethode zur Konvertierung von Date zu LocalDateTime ---
    private LocalDateTime toLocalDateTime(Date date) {
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }
}