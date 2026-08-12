package nz.ac.canterbury.seng302.homehelper.dto;

/**
 * RegisterForm object that holds input values from the registration page. This object is added to
 * the model so that thymeleaf can repopulate the fields when a submission is invalid.
 */
public class UserDTO {

    private String firstName;
    private String lastName;
    private String password;
    private String passwordConfirm;
    private String email;
    private String profileImagePath;
    private String street;
    private String suburb;
    private String city;
    private String postcode;
    private String country;

    public UserDTO() {
        this.firstName = "";
        this.lastName = "";
        this.password = "";
        this.passwordConfirm = "";
        this.email = "";
        this.street = "";
        this.suburb = "";
        this.city = "";
        this.postcode = "";
        this.country = "";
    }

    public UserDTO(String firstName, String lastName, String password, String passwordConfirm,
            String email, String street, String suburb, String city, String postcode,
            String country) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        this.passwordConfirm = passwordConfirm;
        this.email = email;
        this.street = street;
        this.suburb = suburb;
        this.city = city;
        this.postcode = postcode;
        this.country = country;
    }

    /**
     * For form submissions to update only first name, last name, email
     *
     * @param firstName first name String from the form
     * @param lastName  last name String from the form
     * @param email     email String from the form
     */
    public UserDTO(String firstName, String lastName, String email, String street, String suburb,
            String city, String postcode, String country) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordConfirm() {
        return passwordConfirm;
    }

    public void setPasswordConfirm(String passwordConfirm) {
        this.passwordConfirm = passwordConfirm;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfileImagePath() {
        return profileImagePath;
    }

    public void setProfileImagePath(String profileImagePath) {
        this.profileImagePath = profileImagePath;
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

    public String getPostcode() {
        return this.postcode;
    }

    public String getCountry() {
        return this.country;
    }
}
