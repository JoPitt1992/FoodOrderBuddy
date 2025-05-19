package edu.mci.foodorderbuddy.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.RegexpValidator;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import edu.mci.foodorderbuddy.data.entity.Cart;
import edu.mci.foodorderbuddy.data.entity.CartItem;
import edu.mci.foodorderbuddy.data.entity.Menu;
import edu.mci.foodorderbuddy.security.SecurityService;
import edu.mci.foodorderbuddy.service.CartService;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.core.userdetails.UserDetails;

import java.text.NumberFormat;
import java.util.Locale;

@RolesAllowed({"ROLE_USER", "ROLE_ADMIN"})
@Route(value = "payment", layout = MainLayout.class)
@PageTitle("Bezahlung | Food Order Buddy")
public class PaymentView extends VerticalLayout implements BeforeEnterObserver {

    private final CartService cartService;
    private final SecurityService securityService;

    private Cart currentCart;

    private VerticalLayout cartSummary;
    private TextField cardholderName;
    private TextField cardNumber;
    private TextField expiryDate;
    private TextField cvv;
    private Button payButton;

    private Binder<PaymentData> binder;

    public PaymentView(CartService cartService, SecurityService securityService) {
        this.cartService = cartService;
        this.securityService = securityService;

        addClassName("payment-view");
        setWidth("800px");
        setMargin(true);
        setHorizontalComponentAlignment(Alignment.CENTER, this);

        H2 title = new H2("Bezahlung");

        cartSummary = new VerticalLayout();
        cartSummary.setPadding(true);
        cartSummary.getStyle().set("background-color", "var(--lumo-contrast-5pct)");
        cartSummary.getStyle().set("border-radius", "var(--lumo-border-radius-m)");

        add(title, cartSummary, createPaymentForm());

        initializeBinder();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        UserDetails user = securityService.getAuthenticatedUser();

        if (user != null) {
            currentCart = cartService.getOrCreateCart(user.getUsername());

            if (currentCart == null || currentCart.getCartList() == null || currentCart.getCartList().isEmpty()) {
                Notification.show("Ihr Warenkorb ist leer",
                                3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                event.forwardTo("menu");
                return;
            }

            if (Boolean.TRUE.equals(currentCart.getCartPayed())) {
                Notification.show("Dieser Warenkorb wurde bereits bezahlt",
                        3000, Notification.Position.MIDDLE);
                event.forwardTo("order-confirmation/" + currentCart.getCartId());
                return;
            }

            updateCartSummary();
        } else {
            event.forwardTo("login");
        }
    }

    private void updateCartSummary() {
        cartSummary.removeAll();

        H3 summaryTitle = new H3("Bestellübersicht");
        cartSummary.add(summaryTitle);

        if (currentCart == null || currentCart.getCartItems() == null || currentCart.getCartItems().isEmpty()) {
            cartSummary.add(new Span("Ihr Warenkorb ist leer"));
            return;
        }

        // Menüs anzeigen
        for (CartItem item : currentCart.getCartItems()) {
            HorizontalLayout menuItem = new HorizontalLayout();
            menuItem.setWidthFull();

            Span menuTitle = new Span(item.getQuantity() + " × " + item.getMenu().getMenuTitle());
            menuTitle.setWidth("70%");

            NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.GERMANY);
            double itemTotal = item.getMenu().getMenuPrice() * item.getQuantity();
            Span menuPrice = new Span(formatter.format(itemTotal));
            menuPrice.getStyle().set("text-align", "right");
            menuPrice.setWidth("30%");

            menuItem.add(menuTitle, menuPrice);
            cartSummary.add(menuItem);
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

        double total = cartService.calculateTotalPrice(currentCart);
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.GERMANY);
        Span totalAmount = new Span(formatter.format(total));
        totalAmount.getStyle().set("font-weight", "bold");
        totalAmount.getStyle().set("text-align", "right");
        totalAmount.setWidth("30%");

        totalRow.add(totalLabel, totalAmount);
        cartSummary.add(totalRow);
    }

    private VerticalLayout createPaymentForm() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);

        H3 paymentTitle = new H3("Zahlungsinformationen");

        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2)
        );

        cardholderName = new TextField("Name des Karteninhabers");
        cardholderName.setPlaceholder("Max Mustermann");
        cardholderName.setRequired(true);
        cardholderName.setWidthFull();

        cardNumber = new TextField("Kartennummer");
        cardNumber.setPlaceholder("1234 5678 9012 3456");
        cardNumber.setRequired(true);
        cardNumber.setWidthFull();

        expiryDate = new TextField("Gültig bis (MM/JJ)");
        expiryDate.setPlaceholder("12/25");
        expiryDate.setRequired(true);

        cvv = new TextField("Sicherheitscode (CVV)");
        cvv.setPlaceholder("123");
        cvv.setRequired(true);
        cvv.setMaxLength(3);

        formLayout.add(cardholderName, 2);
        formLayout.add(cardNumber, 2);
        formLayout.add(expiryDate, cvv);

        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setWidthFull();
        buttonLayout.setJustifyContentMode(JustifyContentMode.CENTER);

        Button cancelButton = new Button("Abbrechen");
        cancelButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("cart")));

        payButton = new Button("Jetzt bezahlen");
        payButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        payButton.addClickListener(e -> processPayment());

        buttonLayout.add(cancelButton, payButton);

        layout.add(paymentTitle, formLayout, buttonLayout);

        return layout;
    }

    private void initializeBinder() {
        binder = new Binder<>(PaymentData.class);

        binder.forField(cardholderName)
                .asRequired("Bitte geben Sie den Namen des Karteninhabers ein")
                .bind(PaymentData::getCardholderName, PaymentData::setCardholderName);

        binder.forField(cardNumber)
                .asRequired("Bitte geben Sie eine gültige Kartennummer ein")
                .withValidator(new RegexpValidator(
                        "Bitte geben Sie eine gültige 16-stellige Kartennummer ein",
                        "^[0-9]{16}$"))
                .bind(PaymentData::getCardNumber, PaymentData::setCardNumber);

        binder.forField(expiryDate)
                .asRequired("Bitte geben Sie das Ablaufdatum ein")
                .withValidator(new RegexpValidator(
                        "Bitte verwenden Sie das Format MM/JJ",
                        "^(0[1-9]|1[0-2])/[0-9]{2}$"))
                .bind(PaymentData::getExpiryDate, PaymentData::setExpiryDate);

        binder.forField(cvv)
                .asRequired("Bitte geben Sie den Sicherheitscode ein")
                .withValidator(new RegexpValidator(
                        "Der Sicherheitscode besteht aus 3 Ziffern",
                        "^[0-9]{3}$"))
                .bind(PaymentData::getCvv, PaymentData::setCvv);

        binder.setBean(new PaymentData());

        // Button aktivieren/deaktivieren je nach Validität des Formulars
        binder.addStatusChangeListener(e -> payButton.setEnabled(binder.isValid()));
    }

    private void processPayment() {
        if (!binder.validate().isOk()) {
            return;
        }

        PaymentData data = binder.getBean();

        boolean success = cartService.processPayment(
                currentCart,
                "Kreditkarte",
                data.getCardNumber(),
                data.getExpiryDate(),
                data.getCvv()
        );

        if (success) {
            Notification.show("Zahlung erfolgreich!",
                            3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            // Weiterleitung zur Bestellbestätigung
            getUI().ifPresent(ui -> ui.navigate("order-confirmation/" + currentCart.getCartId()));
        } else {
            Notification.show("Die Zahlung konnte nicht verarbeitet werden. Bitte überprüfen Sie Ihre Angaben.",
                            5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    // Datenklasse für das Binding
    public static class PaymentData {
        private String cardholderName = "";
        private String cardNumber = "";
        private String expiryDate = "";
        private String cvv = "";

        public String getCardholderName() { return cardholderName; }
        public void setCardholderName(String cardholderName) { this.cardholderName = cardholderName; }

        public String getCardNumber() { return cardNumber; }
        public void setCardNumber(String cardNumber) {
            // Leerzeichen entfernen für einfachere Validierung
            this.cardNumber = cardNumber != null ? cardNumber.replaceAll("\\s", "") : "";
        }

        public String getExpiryDate() { return expiryDate; }
        public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }

        public String getCvv() { return cvv; }
        public void setCvv(String cvv) { this.cvv = cvv; }
    }
}