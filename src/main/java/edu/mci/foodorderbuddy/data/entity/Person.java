package edu.mci.foodorderbuddy.data.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

@Entity
@Table(name = "person")
public class Person {
    @Column(name = "person_id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long personId;

    @Column(name = "person_firstname", nullable = false)
    private String personFirstName = "";

    @Column(name = "person_lastname", nullable = false)
    @NotEmpty
    private String personLastName = "";

    @Column(name = "person_username", nullable = false)
    @NotEmpty
    private String personUserName = "";

    @Column(name = "person_password", nullable = false)
    @NotEmpty
    private String personPassword = "";

    @Column(name = "person_address")
    private String personAddress = "";

    @Column(name = "person_postalcode")
    private String personPostalCode;

    @Column(name = "person_city")
    private String personCity = "";

    @Column(name = "person_email")
    @Email
    @NotEmpty
    private String personEmail = "";

    @Column(name = "person_phonenumber")
    private String personPhonenumber = "";

    @Column(name = "person_role")
    private String role = "";

    @OneToOne(mappedBy = "person", cascade = CascadeType.ALL)
    private OrderHistory orderhistory;

    public Person(){}

    // Konstruktor mit den Pflichtfeldern beim Registrieren
    public Person(String personFirstName, String personLastName, String personUserName, String personEmail, String personPassword){
        this.personFirstName = personFirstName;
        this.personLastName = personLastName;
        this.personUserName = personUserName;
        this.personEmail = personEmail;
        this.personPassword = personPassword;
    }

    public Long getPersonId() {return personId;}
    public String getPersonFirstName() {
        return personFirstName;
    }
    public String getPersonLastName() {
        return personLastName;
    }
    public String getPersonUserName() {
        return personUserName;
    }
    public String getPersonPassword() {return personPassword; }
    public String getPersonAddress() {return personAddress;}
    public String getPersonPostalCode() {return personPostalCode;}
    public String getPersonCity() {return personCity;}
    public String getPersonEmail() {
        return personEmail;
    }
    public String getPersonPhonenumber() {return personPhonenumber;}
    public String getPersonRole() {return role;}
    public OrderHistory getOrderhistory() {return orderhistory;}

    public void setPersonFirstName(String firstName) {
        this.personFirstName = firstName;
    }
    public void setPersonLastName(String lastName) {
        this.personLastName = lastName;
    }
    public void setPersonUserName(String userName) {
        this.personUserName = userName;
    }
    public void setPersonPassword(String password) {this.personPassword = password; }
    public void setPersonAddress(String address) {this.personAddress = address; }
    public void setPersonPostalCode(String postalCode) {this.personPostalCode = postalCode;}
    public void setPersonCity(String city) {this.personCity = city;}
    public void setPersonEmail(String email) {
        this.personEmail = email;
    }
    public void setPersonPhonenumber(String phonenumber) {this.personPhonenumber = phonenumber; }
    public void setPersonRole(String role) {this.role = role; }
    public void setOrderhistory(OrderHistory orderhistory) {this.orderhistory = orderhistory; }

    @Override
    public String toString() {
        return personFirstName + " " + personLastName;
    }
}
