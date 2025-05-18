package edu.mci.foodorderbuddy.views;

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.*;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import edu.mci.foodorderbuddy.service.DashboardService;
import edu.mci.foodorderbuddy.service.MenuService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.awt.*;
import java.util.ArrayList;

@PermitAll
@Route(value = "dashboard", layout = MainLayout.class)
@PageTitle("Admin Dashboard | Food Order Buddy")
public class DashboardView extends VerticalLayout {
    private final MenuService service;
    private final DashboardService ds;

    public DashboardView(MenuService menuService, DashboardService dashboardService) {
        this.service = menuService;
        this.ds = dashboardService;
        addClassName("dashboard-view");

        ArrayList<DataSeriesItem> items = new ArrayList<>();
        HorizontalLayout hlCharts = new HorizontalLayout();
        HorizontalLayout hlDivs = new HorizontalLayout();
        items.add(new DataSeriesItem("Pizza", 23));
        items.add(new DataSeriesItem("Auflauf", 33));
        items.add(new DataSeriesItem("Brokolie", 5));
        items.add(new DataSeriesItem("Bier", 90));
        Component c = PieChart(items, "Top 5 Gerichte");
        Component cc = PieChart(items, "Top 5 Getränke");
        Component d = createCard("Monatsübersicht");
        Component dd = createCard("Jahresübersicht");
        Component ddd =createCustomerSatisfactionGauge();
        hlDivs.add(d,ddd, dd);
        hlDivs.setAlignItems(Alignment.AUTO);
        hlCharts.add(c, cc);
        hlCharts.setAlignItems(Alignment.AUTO);
        add(exampleMethod(),hlDivs,hlCharts );
    }

    private Component exampleMethod() {
        Span message = new Span("Dashboard");
        message.addClassNames("text-xl", "mt-m");
        return message;
    }

    private Component PieChart(ArrayList<DataSeriesItem> items, String title ) {
        Chart pieChart = new Chart(ChartType.PIE);

        Configuration conf = pieChart.getConfiguration();
        conf.setTitle(title);
        DataSeries series = new DataSeries();
        for (DataSeriesItem item : items){
            series.add(item);
        }
        conf.addSeries(series);
        pieChart.getConfiguration().getChart().setAnimation(true);
        return pieChart;
    }
    private Component createCustomerSatisfactionGauge() {
        // Gauge-Chart erstellen
        Chart gaugeChart = new Chart(ChartType.GAUGE);
        gaugeChart.setWidth("100%");
        gaugeChart.setHeight("300px");

        Configuration conf = gaugeChart.getConfiguration();
        conf.setTitle("Kundenzufriedenheit");

        // Pane-Konfiguration (für den kreisförmigen Bereich)
        Pane pane = new Pane();
        pane.setStartAngle(-90);
        pane.setEndAngle(90);
        pane.setCenter("50%", "100%");
        pane.setSize("150%");
        conf.addPane(pane);

        // Y-Achse (Wertebereich 0-100)
        YAxis yaxis = new YAxis();
        yaxis.setTitle("Zufriedenheit in %");
        yaxis.setMin(0);
        yaxis.setMax(100);
        yaxis.setTickInterval(10);

        // Farbbereiche definieren
        yaxis.setPlotBands(
                new PlotBand(0, 50, null),
                new PlotBand(50, 75, null),
                new PlotBand(75, 100, null)
        );

        conf.addyAxis(yaxis);

        // Datenreihe (hier Beispielwert - normalerweise aus Service laden)
        DataSeries series = new DataSeries("Zufriedenheit");
        // Hier würden Sie den Wert aus Ihrem Daten-Service laden
        double satisfactionScore = 82.5; // Beispielwert
        series.add(new DataSeriesItem("Score", satisfactionScore));

        conf.addSeries(series);

        // Container für besseres Layout
        Div container = new Div(gaugeChart);
        container.getStyle()
                .set("border", "1px solid var(--lumo-contrast-20pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("padding", "var(--lumo-space-s)");

        return container;
    }

    private Component createCard(String title) {
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
        // Daten vom Service holen
        //SalesData salesData = dataService.getSalesData(timeRangeFilter.getValue());

        container.add(header);

        // Drei Metriken erstellen
        container.add(
                createMetricRow("Umsatz:", "String"),
                createMetricRow("Bestellungen:", " String"),
                createMetricRow("Gewinn:", "String"),
                createMetricRow("Bestellwert im Durchschnitt:", "String")
        );

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

}