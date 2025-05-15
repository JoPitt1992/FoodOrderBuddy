package edu.mci.foodorderbuddy.data.entity;


import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class CartCookie {

    private static final String cookieId = "FoodOrderBuddyCard";
    //Zeit in Sekunden welches das Coockie aktiv ist -> 60 * 60 * 24 * 30 -> 30 Tage
    private static final int cookieMaxAge = 60 * 60 * 24 * 30;

    private String cookieValue;

    private Date expiresAt;

    private Cart cart;

    public CartCookie() {}

    public CartCookie(String cookieValue, Date expiresAt, Cart cart) {
        this.cookieValue = cookieValue;
        this.expiresAt = expiresAt;
        this.cart = cart;
    }

    public String getCookieId() { return cookieId; }
    public String getCookieValue() { return cookieValue; }
    public Date getExpiresAt() { return expiresAt; }
    public Cart getCart() { return cart; }

    public void setCookieValue(String cookieValue) { this.cookieValue = cookieValue; }
    public void setExpiresAt(Date expiresAt) { this.expiresAt = expiresAt; }
    public void setCart(Cart cart) { this.cart = cart; }

    //Holt alle Cookies aus dem Speicher und überprüft ob ein Cart Cookie dabei ist.
    public boolean checkCookieExists(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if(cookies != null) {
            for(Cookie cookie : cookies) {
                if(cookie.getName().equals(cookieId)) {
                    return true;
                }
            }
        }
        return false;
    }

    // Sucht und Löscht das Cookie
    public void deleteCookie(HttpServletRequest request, HttpServletResponse response, String cookieId) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(cookieId)) {
                    cookie.setMaxAge(0); // Setzt die Lebensdauer auf 0, um das Cookie zu löschen
                    cookie.setValue(""); // Setzt den Wert auf leer
                    cookie.setPath("/"); // Setzt den Pfad, um sicherzustellen, dass das Cookie gelöscht wird
                    response.addCookie(cookie); // Fügt das Cookie zur Antwort hinzu, um es zu löschen
                }
            }
        }
    }

    // liest das Cookie ein und returnt es als hashmap damit muss dann eine abfrage an die db erstellt werden um den Warenkorb zu befüllen
    public Map<String, Integer> readCookie(HttpServletRequest request) {
        Map<String, Integer> cartItems = new HashMap<>();
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieId.equals(cookie.getName())) {
                    String value = cookie.getValue();
                    String[] items = value.split(";");
                    for (String item : items) {
                        String[] parts = item.split(":");
                        if (parts.length == 2) {
                            String id = parts[0];
                            int quantity = Integer.parseInt(parts[1]);
                            cartItems.put(id, quantity);
                        }
                    }
                }
            }
        }
        return cartItems;
    }

    // Aktualisiert oder erstellt das Cookie mit der neuen Liste von IDs und deren Anzahl
    public static void updateCookie(HttpServletResponse response, Map<String, Integer> cartItems) {
        StringBuilder cookieValue = new StringBuilder();
        for (Map.Entry<String, Integer> entry : cartItems.entrySet()) {
            if (cookieValue.length() > 0) {
                cookieValue.append(";");
            }
            cookieValue.append(entry.getKey()).append(":").append(entry.getValue());
        }
        Cookie cookie = new Cookie(cookieId, cookieValue.toString());
        cookie.setMaxAge(cookieMaxAge);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

}