package edu.mci.foodorderbuddy.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import edu.mci.foodorderbuddy.data.entity.Cart;
import edu.mci.foodorderbuddy.data.entity.Menu;
import edu.mci.foodorderbuddy.data.repository.CartRepository;
import edu.mci.foodorderbuddy.service.CartService;
import edu.mci.foodorderbuddy.service.DashboardService;
import edu.mci.foodorderbuddy.service.MenuService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.NumberFormat;
import java.util.Locale;

import java.time.YearMonth;
import java.util.*;
import java.time.LocalDate;
import java.time.Year;
import java.util.stream.Collectors;

@PermitAll
@Route(value = "dashboard", layout = MainLayout.class)
@PageTitle("Admin Dashboard | Food Order Buddy")
public class DashboardView extends VerticalLayout {
    private final DashboardService ds;

    public DashboardView(DashboardService dashboardService) {
        this.ds = dashboardService;
        UI.getCurrent().getPage().addJavaScript("https://www.gstatic.com/charts/loader.js");
        addClassName("dashboard-view");
        HorizontalLayout dataLine = new HorizontalLayout();
        dataLine.setAlignItems(Alignment.CENTER);

        int currentYear = Year.now().getValue();
        int currentMonth = YearMonth.now().getMonthValue();

        Component month = createMonthlyOverhead(currentYear, currentMonth);
        Component year = createYearlyOverhead(currentYear, currentMonth);
        Component general = createGeneralOverview(currentYear);
        Component pieCharts = createPieCharts();
        Component orderStatusCharts = createOrderStatusCharts();
        dataLine.add(month, year, general);
        add(dataLine, pieCharts, orderStatusCharts);
    }

    private Component createOrderStatusCharts() {
        Div cartsComp = new Div();
        Map<String, Number> data = new HashMap<>();

        Long inDelivery = ds.countCartsInDelivery();
        Long inProcess = ds.countCartsInProcess();
        System.out.print("Process" + inProcess);
        System.out.print("Delivery" + inDelivery);
        data.put("In Lieferung", inDelivery);
        data.put("In Bearbeitung", inProcess);

        cartsComp.add(createGaugeChartGreenRed("Bestellstatus", data));
        return cartsComp;
    }

    private Component createGeneralOverview(int currentYear) {
        Div generalComp = createCard("Allgemeine Infos");
        Cart bestCart = ds.getMaxOrderValue();
        Double price = bestCart.getCartPrice();
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.GERMANY);
        String bestRevenue = currencyFormat.format(price);

        Long unpaid = ds.countUnpaidCarts();
        Double avgItemsPerCart = ds.calculateAverageOrderQuantityPerCart();

        generalComp.add(
                createMetricRow("Höchster Einkaufspreis: ", bestRevenue),
                createMetricRow("Unbezahlre Rechnungen: ", unpaid.toString()),
                createMetricRow("Durchschnittliche Bestellgröße: ", String.format("%.2f", avgItemsPerCart))
        );
        return generalComp;
    }

    private Component createMonthlyOverhead(int year, int month){
        Div monthComp = createCard("Monatsübersicht");

        Double revenue = ds.getMonthlyRevenue(year, month);
        // Formatierung des Umsatzes
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.GERMANY);
        String formattedRevenue = currencyFormat.format(revenue);

        // Anzahl der Bestellungen
        Long orders = ds.getOrderCountInMonth(year, month);

        // Berechnung Durchschnittsbestellwert
        Double avgOrderValue = orders > 0 ? revenue/orders : 0.0;
        String formattedAvg = currencyFormat.format(avgOrderValue);

        monthComp.add(
                createMetricRow("Umsatz: ", formattedRevenue),
                createMetricRow("Bestellungen: ", orders.toString()),
                createMetricRow("Bestellwert im Durchschnitt: ", formattedAvg)
        );


        return monthComp;
    }

    private Component createYearlyOverhead(int year, int month){
        Div yearComp = createCard("Jahresübersicht");

        Double revenue = ds.getYearlyRevenue(year);
        // Formatierung des Umsatzes
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.GERMANY);
        String formattedRevenue = currencyFormat.format(revenue);

        // Anzahl der Bestellungen
        Long orders = ds.getOrderCountInYear(year);

        // Berechnung Durchschnittsbestellwert
        Double avgOrderValue = orders > 0 ? revenue/orders : 0.0;
        String formattedAvg = currencyFormat.format(avgOrderValue);

        yearComp.add(
                createMetricRow("Umsatz: ", formattedRevenue),
                createMetricRow("Bestellungen: ", orders.toString()),
                createMetricRow("Bestellwert im Durchschnitt: ", formattedAvg)
        );

        return yearComp;
    }

    private Component createPieCharts(){
        HorizontalLayout hlCharts = new HorizontalLayout();
        List<Object[]> top3Most = ds.getTop3MostSoldDishes();
        List<Object[]> top3Least = ds.getTop3LeastSoldDishes();

        Map<String, Number> top3MostData = new HashMap<>();
        Map<String, Number> top3LeastData = new HashMap<>();

        for (Object[] row : top3Most) {
            Menu menu = (Menu) row[0];
            String title = menu.getMenuTitle();
            Number count = (Number) row[1];
            top3MostData.put(title, count);
        }

        for (Object[] row : top3Least) {
            Menu menu = (Menu) row[0];
            String title = menu.getMenuTitle();
            Number count = (Number) row[1];
            top3LeastData.put(title, count);
        }

        Component pieChartTopMost = createPieChart("Am meisten bestellt", top3MostData);
        Component pieChartTopLeast = createPieChart("Am wenigsten bestellt", top3LeastData);

        hlCharts.add(pieChartTopMost, pieChartTopLeast);
        hlCharts.setAlignItems(Alignment.CENTER);

        return hlCharts;
    }

    private Component exampleMethod() {
        Span message = new Span("Dashboard");
        message.addClassNames("text-xl", "mt-m");
        return message;
    }

    private Div createCard(String title) {
        Div container = new Div();
        container.getStyle()
                .set("border", "1px solid var(--lumo-contrast-20pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("padding", "var(--lumo-space-s)");

        H3 header = new H3(title);
        header.getStyle()
                .set("margin-top", "0")
                .set("margin-bottom", "var(--lumo-space-s)")
                .set("font-size", "var(--lumo-font-size-l)");

        container.add(header);

        return container;
    }

    private Component createMetricRow(String label, String value) {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setPadding(false);
        row.setSpacing(false);

        // Label (links)
        Span labelSpan = new Span(label);
        labelSpan.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("margin-right", "var(--lumo-space-xs)");

        // Wert (rechts)
        Span valueSpan = new Span(value);
        valueSpan.getStyle()
                .set("font-weight", "600")
                .set("margin-left", "auto"); // schiebt den Wert nach rechts

        row.add(labelSpan, valueSpan);
        return row;
    }


    public Component createPieChart(String title, Map<String, Number> data) {
        Div chartContainer = new Div();
        chartContainer.setId("chart-" + UUID.randomUUID());

        // Google Charts laden
        UI.getCurrent().getPage().addJavaScript("https://www.gstatic.com/charts/loader.js");

        // JavaScript-Code zum Rendern des Charts
        String jsCode =
                "google.charts.load('current', {'packages':['corechart']});" +
                        "google.charts.setOnLoadCallback(drawChart);" +
                        "function drawChart() {" +
                        "   var data = new google.visualization.DataTable();" +
                        "   data.addColumn('string', 'Label');" +
                        "   data.addColumn('number', 'Value');" +
                        "   data.addRows([" +
                        data.entrySet().stream()
                                .map(e -> "['" + e.getKey() + "', " + e.getValue() + "]")
                                .collect(Collectors.joining(",")) +
                        "   ]);" +
                        "   var options = {'title':'" + title + "', 'width':400, 'height':300};" +
                        "   var chart = new google.visualization.PieChart(document.getElementById('" + chartContainer.getId().orElse("") + "'));" +
                        "   chart.draw(data, options);" +
                        "}";

        // JavaScript ausführen, sobald das Element im DOM ist
        chartContainer.getElement().executeJs(jsCode);

        return chartContainer;
    }

    public Component createGaugeChartGreenRed(String title, Map<String, Number> data) {
        Div gaugeContainer = new Div();
        String uniqueId = "gauge-" + UUID.randomUUID(); // Eindeutige ID
        gaugeContainer.setId(uniqueId);
        int minValue = 0;
        int maxValue = 30;

        // CSS für die Größe des Containers
        gaugeContainer.getStyle()
                .set("width", "500px")
                .set("height", "200px")
                .set("display", "inline-block"); // Für nebeneinander angeordnete Charts

        // JavaScript-Code für das Tachometerdiagramm
        String jsCode =
                "if (typeof google !== 'undefined') {" +
                        "   google.charts.load('current', {'packages':['gauge']});" +
                        "   google.charts.setOnLoadCallback(function() {" +
                        "       drawGauge('" + uniqueId + "', " + minValue + ", " + maxValue + ");" +
                        "   });" +
                        "}" +
                        "function drawGauge(containerId, min, max) {" +
                        "   var data = google.visualization.arrayToDataTable([" +
                        "       ['Label', 'Value']," +
                        data.entrySet().stream()
                                .map(e -> "['" + e.getKey() + "', " + e.getValue() + "]")
                                .collect(Collectors.joining(",")) +
                        "   ]);" +
                        "   var options = {" +
                        "       width: 500," +
                        "       height: 200," +
                        "       min: min," +      // Minimalwert
                        "       max: max," +      // Maximalwert
                        "       redFrom: max * 0.9, redTo: max," +     // Rot ab 90% des Maximalwerts
                        "       yellowFrom: max * 0.75, yellowTo: max * 0.9," + // Gelb ab 75%
                        "       minorTicks: 5" +
                        "   };" +
                        "   var chart = new google.visualization.Gauge(document.getElementById(containerId));" +
                        "   chart.draw(data, options);" +
                        "}";

        // JavaScript ausführen
        gaugeContainer.getElement().executeJs(jsCode);
        return gaugeContainer;
    }
}












