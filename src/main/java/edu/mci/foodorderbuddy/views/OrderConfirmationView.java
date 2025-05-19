package edu.mci.foodorderbuddy.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import edu.mci.foodorderbuddy.data.entity.Cart;
import edu.mci.foodorderbuddy.data.entity.CartItem;
import edu.mci.foodorderbuddy.data.entity.Menu;
import edu.mci.foodorderbuddy.data.repository.CartRepository;
import jakarta.annotation.security.RolesAllowed;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Optional;

@RolesAllowed({"ROLE_USER", "ROLE_ADMIN"})
@Route(value = "order-confirmation", layout = MainLayout.class)
@PageTitle("Bestellbestätigung | Food Order Buddy")
public class OrderConfirmationView extends VerticalLayout implements HasUrlParameter<Long> {

    private final CartRepository cartRepository;

    private VerticalLayout confirmationContainer;

    public OrderConfirmationView(CartRepository cartRepository) {
        this.cartRepository = cartRepository;

        addClassName("order-confirmation-view");
        setSizeFull();
        setHorizontalComponentAlignment(Alignment.CENTER, this);

        confirmationContainer = new VerticalLayout();
        confirmationContainer.setWidth("800px");
        confirmationContainer.setPadding(true);
        confirmationContainer.getStyle().set("background-color", "var(--lumo-contrast-5pct)");
        confirmationContainer.getStyle().set("border-radius", "var(--lumo-border-radius-m)");

        add(confirmationContainer);
    }

    @Override
    public void setParameter(BeforeEvent event, Long cartId) {
        confirmationContainer.removeAll();

        Optional<Cart> optionalCart = cartRepository.findById(cartId);

        if (optionalCart.isPresent() && Boolean.TRUE.equals(optionalCart.get().getCartPayed())) {
            Cart cart = optionalCart.get();
            displayOrderConfirmation(cart);
        } else {
            displayNotFoundMessage();
        }
    }

    private void displayOrderConfirmation(Cart cart) {
        Icon checkIcon = new Icon(VaadinIcon.CHECK_CIRCLE);
        checkIcon.setColor("var(--lumo-success-color)");
        checkIcon.setSize("3em");

        H2 title = new H2("Bestellung erfolgreich aufgegeben");

        VerticalLayout infoLayout = new VerticalLayout();
        infoLayout.setPadding(false);
        infoLayout.setSpacing(false);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        Paragraph dateInfo = new Paragraph("Bestelldatum: " + dateFormat.format(cart.getCartPaydate()));

        Paragraph referenceInfo = new Paragraph("Bestellnummer: " + cart.getPaymentReference());

        infoLayout.add(dateInfo, referenceInfo);

        // Bestellübersicht
        H3 orderSummaryTitle = new H3("Bestellübersicht");

        VerticalLayout orderSummary = new VerticalLayout();
        orderSummary.setPadding(false);
        orderSummary.setSpacing(false);

        if (cart.getCartItems() != null) {
            for (CartItem item : cart.getCartItems()) {
                HorizontalLayout menuItem = new HorizontalLayout();
                menuItem.setWidthFull();

                Span menuTitle = new Span(item.getQuantity() + " × " + item.getMenu().getMenuTitle());
                menuTitle.setWidth("70%");

                NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.GERMANY);
                double itemTotal = item.getMenu().getMenuPrice() * item.getQuantity();
                Span menuPrice = new Span(formatter.format(itemTotal));
                menuPrice.setWidth("30%");
                menuPrice.getStyle().set("text-align", "right");

                menuItem.add(menuTitle, menuPrice);
                orderSummary.add(menuItem);
            }
        }
        // Gesamtsumme
        HorizontalLayout totalRow = new HorizontalLayout();
        totalRow.setWidthFull();
        totalRow.getStyle().set("margin-top", "1rem");
        totalRow.getStyle().set("padding-top", "0.5rem");
        totalRow.getStyle().set("border-top", "1px solid var(--lumo-contrast-20pct)");

        Span totalLabel = new Span("Gesamtbetrag:");
        totalLabel.getStyle().set("font-weight", "bold");
        totalLabel.setWidth("70%");

        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.GERMANY);
        Span totalAmount = new Span(formatter.format(cart.getCartPrice()));
        totalAmount.getStyle().set("font-weight", "bold");
        totalAmount.getStyle().set("text-align", "right");
        totalAmount.setWidth("30%");

        totalRow.add(totalLabel, totalAmount);

        // Lieferhinweis
        Paragraph deliveryInfo = new Paragraph("Vielen Dank für Ihre Bestellung! Wir bereiten Ihre Speisen zu und werden sie in Kürze liefern.");
        deliveryInfo.getStyle().set("margin-top", "1.5rem");

        // Buttons
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setWidthFull();
        buttonLayout.setJustifyContentMode(JustifyContentMode.CENTER);

        Button backToMenuButton = new Button("Zurück zur Speisekarte", e -> getUI().ifPresent(ui -> ui.navigate("menu")));

        buttonLayout.add(backToMenuButton);

        confirmationContainer.add(
                checkIcon, title, infoLayout,
                orderSummaryTitle, orderSummary, totalRow,
                deliveryInfo, buttonLayout
        );
    }

    private void displayNotFoundMessage() {
        Icon warningIcon = new Icon(VaadinIcon.WARNING);
        warningIcon.setColor("var(--lumo-error-color)");
        warningIcon.setSize("3em");

        H2 title = new H2("Bestellung nicht gefunden");

        Paragraph message = new Paragraph("Die angeforderte Bestellung wurde nicht gefunden oder noch nicht bezahlt.");

        Button backButton = new Button("Zurück zur Speisekarte", e -> getUI().ifPresent(ui -> ui.navigate("menu")));
        backButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        confirmationContainer.add(warningIcon, title, message, backButton);
    }
}