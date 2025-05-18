package edu.mci.foodorderbuddy.data.entity;

import com.vaadin.flow.server.VaadinService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CartCookie {

    private static final String cookieId = "FoodOrderBuddyCard";
    //Zeit in Sekunden welches das Coockie aktiv ist -> 60 * 60 * 24 * 30 -> 30 Tage
    private static final int cookieMaxAge = 60 * 60 * 24 * 30;

    private String cookieValue;

    private Date expiresAt;

    // Singleton instance
    private static CartCookie instance;

    private CartCookie() {}

    // Static method to get the singleton instance
    public static synchronized CartCookie getInstance() {
        if (instance == null) {
            instance = new CartCookie();
        }
        return instance;
    }

    // Constructor with parameters
    public static synchronized CartCookie getInstance(String cookieValue, Date expiresAt) {
        if (instance == null) {
            instance = new CartCookie(cookieValue, expiresAt);
        }
        return instance;
    }

    // Initialize the singleton instance with parameters
    private CartCookie(String cookieValue, Date expiresAt) {
        this.cookieValue = cookieValue;
        this.expiresAt = expiresAt;
    }

    public String getCookieId() { return cookieId; }
    public String getCookieValue() { return cookieValue; }
    public Date getExpiresAt() { return expiresAt; }

    public void setCookieValue(String cookieValue) { this.cookieValue = cookieValue; }
    public void setExpiresAt(Date expiresAt) { this.expiresAt = expiresAt; }

    private Cookie getCookie(){
        Cookie[] cookies = VaadinService.getCurrentRequest().getCookies();
        if(cookies != null) {
            for(Cookie cookie : cookies) {
                if(cookie.getName().equals(cookieId)) {
                    return cookie;
                }
            }
        }
        return null;
    }

    //Holt alle Cookies aus dem Speicher und überprüft ob ein Cart Cookie dabei ist.
    public boolean checkCookieExists() {
        Cookie[] cookies = VaadinService.getCurrentRequest().getCookies();
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
    public void deleteCookie() {
        Cookie cookie = getCookie();
        if(cookie != null){
            cookie.setMaxAge(0);
            cookie.setValue("");
            cookie.setPath("/");
            cookie.setPath(VaadinService.getCurrentRequest().getContextPath());
            VaadinService.getCurrentResponse().addCookie(cookie);
        }else{
            System.out.println("Cookie not found");
        }
    }

    // liest das Cookie ein und returnt es als hashmap damit muss dann eine abfrage an die db erstellt werden um den Warenkorb zu befüllen
    public Map<Integer, Integer> readCookie() {
        Map<Integer, Integer> cartItems = new HashMap<>();
        Cookie mycookie = getCookie();
        try{
            if(mycookie != null) {
                for (String item : mycookie.getValue().split(";")) {
                    String[] parts = item.split(":");
                    int id = Integer.parseInt(parts[0]);
                    int amount = Integer.parseInt(parts[1]);
                    cartItems.put(id, amount);
                }
            }
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
        return cartItems;
    }

    // Aktualisiert oder erstellt das Cookie mit der neuen Liste von IDs und deren Anzahl
    public void updateCookie(Map<Integer, Integer> cartItems) {
        Cookie cookie = getCookie();
        if(cookie != null) {
            StringBuilder cookieValue = new StringBuilder();
            for (Map.Entry<Integer, Integer> entry : cartItems.entrySet()) {
                if (!cookieValue.isEmpty()) {
                    cookieValue.append(";");
                }
                cookieValue.append(entry.getKey()).append(":").append(entry.getValue());
            }
            Cookie newcookie = new Cookie(cookieId, cookieValue.toString());
            newcookie.setMaxAge(cookieMaxAge);
            newcookie.setPath(VaadinService.getCurrentRequest().getContextPath());
            VaadinService.getCurrentResponse().addCookie(newcookie);
        }
    }

}