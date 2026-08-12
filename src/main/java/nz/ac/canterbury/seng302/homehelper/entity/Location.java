package nz.ac.canterbury.seng302.homehelper.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

/**
 * Database entity that represents a location as a street address, optionally a suburb, city,
 * postcode and country.
 */
@Entity
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String street;

    @Column
    private String suburb;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String postCode;

    @Column(nullable = false)
    private String country;

    @OneToOne(mappedBy = "renovationLocation")
    private RenovationRecord renovationRecord;

    @OneToOne(mappedBy = "userLocation")
    private User user;

    public Location(String street, String suburb, String city, String postCode, String country) {
        this.street = street.trim();
        this.suburb = suburb.trim();
        this.city = city.trim();
        this.postCode = postCode.trim();
        this.country = country.trim();
    }

    /**
     * JPA required no args constructor
     */
    protected Location() {

    }

    /**
     * Retrieves the address in street, suburb, city, postcode, country format where a suburb may or
     * may not be present
     *
     * @return address as comma seperated string
     */
    public String getFullAddress() {
        StringBuilder address = new StringBuilder();
        address.append(street).append(", ");
        if (suburb != null && !suburb.isBlank()) {
            address.append(suburb).append(", ");
        }
        address.append(city).append(", ")
                .append(postCode).append(", ")
                .append(country);
        return address.toString();
    }

    public String getStreet() {
        return this.street;
    }

    public String getSuburb() {
        return this.suburb;
    }

    public String getCity() {
        return this.city;
    }

    public String getPostCode() {
        return this.postCode;
    }

    public String getCountry() {
        return this.country;
    }

}
