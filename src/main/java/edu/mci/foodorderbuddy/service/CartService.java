package edu.mci.foodorderbuddy.service;

import edu.mci.foodorderbuddy.data.entity.*;
import edu.mci.foodorderbuddy.data.repository.CartItemRepository;
import edu.mci.foodorderbuddy.data.repository.CartRepository;
import edu.mci.foodorderbuddy.data.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final PersonRepository personRepository;
    private final CartItemRepository cartItemRepository;

    @Autowired
    public CartService(CartRepository cartRepository, PersonRepository personRepository, CartItemRepository cartItemRepository) {
        this.cartRepository = cartRepository;
        this.personRepository = personRepository;
        this.cartItemRepository = cartItemRepository;
    }

    /**
     * Holt den Warenkorb eines Benutzers oder erstellt einen neuen, falls keiner existiert
     */
    @Transactional
    public Cart getOrCreateCart(String username) {
        System.out.println("getOrCreateCart: Start - Username: " + username);

        try {
            // Finden Sie den Benutzer
            Optional<Person> personOpt = personRepository.findByPersonUserName(username);

            Person person;

            if (personOpt.isEmpty()) {
                System.out.println("getOrCreateCart: Benutzer nicht in DB gefunden - erstelle temporären Benutzer für " + username);

                // Temporären Benutzer erstellen mit gültiger E-Mail-Adresse
                person = new Person();
                person.setPersonUserName(username);
                person.setPersonFirstName("Temporärer");
                person.setPersonLastName("Benutzer");
                person.setPersonEmail(username + "@example.com"); // Wichtig: E-Mail setzen
                person.setPersonRole("ROLE_USER"); // Rolle setzen

                // Benutzer in der Datenbank speichern
                person = personRepository.save(person);
                System.out.println("getOrCreateCart: Temporärer Benutzer erstellt und gespeichert mit ID: " + person.getPersonId());
            } else {
                person = personOpt.get();
                System.out.println("getOrCreateCart: Benutzer gefunden - " +
                        person.getPersonFirstName() + " " + person.getPersonLastName());
            }

            // Verwenden Sie die explizite Query anstelle der automatisch generierten Methode
            Optional<Cart> cartOpt = cartRepository.findActiveCartByUsername(username);

            if (cartOpt.isPresent()) {
                Cart cart = cartOpt.get();
                System.out.println("getOrCreateCart: Existierenden Warenkorb gefunden - ID: " +
                        cart.getCartId());
                return cart;
            }

            // Keinen aktiven Warenkorb gefunden, erstellen Sie einen neuen
            System.out.println("getOrCreateCart: Kein aktiver Warenkorb gefunden, erstelle neuen");

            Cart newCart = new Cart();
            newCart.setOwner(person);
            newCart.setCartItems(new ArrayList<>());
            newCart.setCartList(new ArrayList<>());
            newCart.setCartPrice(0.0);
            newCart.setCartPayed(false);
            newCart.setCartOrderStatus(OrderStatus.IN_BEARBEITUNG);

            Cart savedCart = cartRepository.save(newCart);
            System.out.println("getOrCreateCart: Neuer Warenkorb erstellt - ID: " + savedCart.getCartId());

            return savedCart;

        } catch (Exception e) {
            System.err.println("getOrCreateCart: Fehler - " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Fügt ein Menü zum Warenkorb hinzu mit einer bestimmten Menge
     */
    @Transactional
    public void addMenuToCart(Cart cart, Menu menu, int quantity) {
        System.out.println("addMenuToCart: Start - Cart ID: " +
                (cart != null ? cart.getCartId() : "null") +
                ", Menu: " + (menu != null ? menu.getMenuTitle() : "null") +
                ", Quantity: " + quantity);

        if (cart == null || menu == null || quantity <= 0) {
            System.out.println("addMenuToCart: Fehler - Ungültige Parameter");
            return;
        }

        try {
            // Prüfen, ob das Menü bereits im Warenkorb ist
            CartItem existingItem = cart.findCartItemByMenu(menu);

            if (existingItem != null) {
                // Menü ist bereits im Warenkorb, Anzahl erhöhen
                existingItem.setQuantity(existingItem.getQuantity() + quantity);
                System.out.println("addMenuToCart: Menge für existierendes Menü erhöht: " +
                        existingItem.getMenu().getMenuTitle() + ", neue Menge: " + existingItem.getQuantity());
            } else {
                // Neues CartItem erstellen
                CartItem cartItem = new CartItem(cart, menu, quantity);
                cart.addCartItem(cartItem);
                System.out.println("addMenuToCart: Neues Menü hinzugefügt: " +
                        menu.getMenuTitle() + ", Menge: " + quantity);

                // Auch zur alten Liste hinzufügen für Kompatibilität
                cart.getCartList().add(menu);
            }

            // Preis aktualisieren
            updateCartPrice(cart);

            // Warenkorb speichern
            cartRepository.save(cart);
            System.out.println("addMenuToCart: Warenkorb gespeichert");

        } catch (Exception e) {
            System.err.println("addMenuToCart: Fehler - " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Rückwärtskompatible Methode - fügt ein Menü mit Menge 1 hinzu
     */
    @Transactional
    public void addMenuToCart(Cart cart, Menu menu) {
        addMenuToCart(cart, menu, 1);
    }

    /**
     * Entfernt ein CartItem aus dem Warenkorb
     */
    @Transactional
    public void removeCartItem(Cart cart, CartItem item) {
        if (cart != null && item != null) {
            cart.removeCartItem(item);

            // Auch aus der alten Liste entfernen
            cart.getCartList().remove(item.getMenu());

            // Preis aktualisieren
            updateCartPrice(cart);

            cartRepository.save(cart);
        }
    }

    /**
     * Rückwärtskompatible Methode - entfernt ein Menü aus dem Warenkorb
     */
    @Transactional
    public void removeMenuFromCart(Cart cart, Menu menu) {
        if (cart != null && menu != null) {
            CartItem item = cart.findCartItemByMenu(menu);
            if (item != null) {
                removeCartItem(cart, item);
            }
        }
    }

    /**
     * Berechnet den Gesamtpreis des Warenkorbs
     */
    public double calculateTotalPrice(Cart cart) {
        if (cart == null) {
            return 0.0;
        }

        // Wenn cartItems vorhanden sind, berechne Preis basierend auf diesen
        if (cart.getCartItems() != null && !cart.getCartItems().isEmpty()) {
            return cart.getCartItems().stream()
                    .mapToDouble(item -> item.getMenu().getMenuPrice() * item.getQuantity())
                    .sum();
        }
        // Sonst verwende die alte Liste ohne Mengenangaben
        else if (cart.getCartList() != null && !cart.getCartList().isEmpty()) {
            return cart.getCartList().stream()
                    .mapToDouble(Menu::getMenuPrice)
                    .sum();
        }

        return 0.0;
    }

    /**
     * Aktualisiert den Preis des Warenkorbs basierend auf den enthaltenen Menüs
     */
    private void updateCartPrice(Cart cart) {
        double total = calculateTotalPrice(cart);
        cart.setCartPrice(total);
    }

    /**
     * Verarbeitet die Bezahlung für einen Warenkorb
     */
    @Transactional
    public boolean processPayment(Cart cart, String paymentMethod, String cardNumber, String expiryDate, String cvv) {
        if (cart == null || Boolean.TRUE.equals(cart.getCartPayed())) {
            return false;
        }

        // In einer realen Anwendung würde hier die Kommunikation mit einem Payment-Gateway erfolgen
        // Hier simulieren wir eine erfolgreiche Zahlung mit einfacher Validierung
        boolean paymentSuccessful = validatePaymentDetails(cardNumber, expiryDate, cvv);

        if (paymentSuccessful) {
            cart.setCartPayed(true);
            cart.setCartPaydate(new Date());

            // Generieren einer eindeutigen Bestellnummer
            String paymentReference = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            cart.setPaymentReference(paymentReference);
            cart.setPaymentMethod(paymentMethod);

            cartRepository.save(cart);

            addToOrderHistory(cart);
            return true;
        }

        return false;
    }

    @Autowired
    private OrderHistoryService orderHistoryService;

    /**
     * Nach der Bezahlung wird der Warenkorb der Bestellhistorie hinzugefügt
     */
    private void addToOrderHistory(Cart cart) {
        if (cart != null && Boolean.TRUE.equals(cart.getCartPayed())) {
            orderHistoryService.addCartToOrderHistory(cart);
        }
    }

    /**
     * Einfache Validierung der Zahlungsdaten
     */
    private boolean validatePaymentDetails(String cardNumber, String expiryDate, String cvv) {
        // Kreditkartennummer: genau 16 Ziffern, ohne Leerzeichen
        boolean validCardNumber = cardNumber != null && cardNumber.replaceAll("\\s", "").matches("\\d{16}");

        // Ablaufdatum: Format MM/JJ
        boolean validExpiryDate = expiryDate != null && expiryDate.matches("(0[1-9]|1[0-2])/[0-9]{2}");

        // CVV: genau 3 Ziffern
        boolean validCVV = cvv != null && cvv.matches("\\d{3}");

        return validCardNumber && validExpiryDate && validCVV;
    }
}