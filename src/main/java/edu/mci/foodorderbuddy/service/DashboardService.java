package edu.mci.foodorderbuddy.service;


import edu.mci.foodorderbuddy.data.entity.Cart;
import edu.mci.foodorderbuddy.data.repository.CartRepository;
import edu.mci.foodorderbuddy.data.repository.OrderHistoryRepository;
import edu.mci.foodorderbuddy.data.repository.PersonRepository;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class DashboardService {

    private final CartRepository cartRepository;


    @Autowired
    public DashboardService(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    public Long countCartsInProcess(){
        return cartRepository.countCartsInProcess();
    }

    public Long countCartsInDelivery(){
        return cartRepository.countCartsInDelivery();
    }
    public Long countUnpaidCarts(){
        return cartRepository.countUnpaidCarts();
    }

    public Double calculateAverageOrderQuantityPerCart(){
        return cartRepository.calculateAverageOrderQuantityPerCart();
    }

    public Cart getMaxOrderValue(){
        // Hole alle Bestellungen aus der Datenbank
        List<Cart> carts = cartRepository.findAll();

        // Verwende Java Streams und Lambda-Ausdrücke, um die größte Bestellung zu finden
        return carts.stream()
                .max((cart1, cart2) -> Double.compare(cart1.getCartPrice(), cart2.getCartPrice()))
                .orElse(null);
    }

    public Double getMonthlyRevenue(int year, int month) {
        // Berechne den ersten und letzten Tag des Monats
        YearMonth yearMonth = YearMonth.of(year, month);
        Date firstDayOfMonth = getMonthlyDateStart(yearMonth);
        Date lastDayOfMonth = getMonthlyDateEnd(yearMonth);

        // Verwende das CartRepository, um den Umsatz für den Monat zu berechnen
        return cartRepository.findMonthlyRevenue(firstDayOfMonth, lastDayOfMonth) != null ?
                cartRepository.findMonthlyRevenue(firstDayOfMonth, lastDayOfMonth) : 0.0;
    }

    public Double getYearlyRevenue(int year) {
        // Berechne den ersten und letzten Tag des Jahres
        Date firstDayOfYear = getYearlyDateStart(year);
        Date lastDayOfYear = getYearlyDateEnd(year);

        // Verwende das CartRepository, um den Umsatz für das Jahr zu berechnen
        return cartRepository.findYearlyRevenue(firstDayOfYear, lastDayOfYear) != null ?
                cartRepository.findYearlyRevenue(firstDayOfYear, lastDayOfYear) : 0.0;
    }

    public Long getOrderCountInMonth(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        Date firstDayOfMonth = getMonthlyDateStart(yearMonth);
        Date lastDayOfMonth = getMonthlyDateEnd(yearMonth);
        return cartRepository.countOrdersInMonth(firstDayOfMonth, lastDayOfMonth);
    }

    public Long getOrderCountInYear(int year) {
        Date firstDayOfYear = getYearlyDateStart(year);
        Date lastDayOfYear = getYearlyDateEnd(year);

        return cartRepository.countOrdersInYear(firstDayOfYear, lastDayOfYear);
    }

    public List<Object[]> getTop3MostSoldDishes() {
        return cartRepository.findTop3MostSoldDishes();
    }

    public List<Object[]> getTop3LeastSoldDishes() {
        return cartRepository.findTop3LeastSoldDishes();
    }


    private Date getMonthlyDateStart(YearMonth yearMonth){
        return Date.from(
            yearMonth.atDay(1)
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
            );
    }

    private Date getMonthlyDateEnd(YearMonth yearMonth){
        return Date.from(
            yearMonth.atEndOfMonth()
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant());
    }

    private Date getYearlyDateStart(int year){
        return Date.from(
                LocalDate.of(year, 1, 1)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
        );
    }

    private Date getYearlyDateEnd(int year){

        return Date.from(
                LocalDate.of(year, 12, 31)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
        );
    }


}
