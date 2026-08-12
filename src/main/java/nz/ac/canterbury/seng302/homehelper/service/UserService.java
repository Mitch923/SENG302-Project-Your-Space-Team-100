package nz.ac.canterbury.seng302.homehelper.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import nz.ac.canterbury.seng302.homehelper.entity.DynamicUserDetails;
import nz.ac.canterbury.seng302.homehelper.entity.Location;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Token;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.TokenRepository;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.utils.FileUtilities;
import nz.ac.canterbury.seng302.homehelper.utils.JarResourceFileLoader;
import nz.ac.canterbury.seng302.homehelper.utils.LocationValidator;
import nz.ac.canterbury.seng302.homehelper.utils.ResetPasswordUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

// ChatGPT generated javaDoc

/**
 * Service class for managing user-related operations such as registration, retrieval, and updating
 * user details. This service handles user persistence via the UserRepository, user authentication,
 * and interaction with the password encoder. It also provides methods to check user existence, get
 * the currently logged-in user, and determine the authentication status.
 */
@Service
public class UserService {

    private static final String USERNAME_PATH = "/userNames.txt";
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenRepository tokenRepository;
    private final JarResourceFileLoader jarResourceFileLoader;
    private final Path testUserImageDir = Paths.get(
            "static",
            "utils"
    );
    Logger logger = LoggerFactory.getLogger(UserService.class);


    @Value("${static.resource.folder}")
    private String uploadsFolder;

    @Autowired
    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            TokenRepository tokenRepository,
            JarResourceFileLoader jarResourceFileLoader
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenRepository = tokenRepository;
        this.jarResourceFileLoader = jarResourceFileLoader;
    }

    /**
     * Registers a user based on provided details
     *
     * @param email     User's email
     * @param password  User's raw password to be hashed
     * @param firstName User's first name
     * @param lastName  User's last name
     * @return user that was saved
     */
    public User registerUser(String email, String password, String firstName, String lastName)
            throws UsernameNotFoundException {
        email = email.toLowerCase();
        String hashedPassword = passwordEncoder.encode(password);
        User user = new User(email, hashedPassword, firstName, lastName);

        if (!userRepository.existsByEmail(email)) { // Check if user exists
            userRepository.save(user);
            return user;
        } else {
            // Email already in use
            throw new UsernameNotFoundException("User with email " + email + " already exists");
        }
    }

    /**
     * Gets and returns a user by their email
     *
     * @param email the email of the user
     * @return an {@code Optional<>} either with a user if one was found, or no user.
     */
    public Optional<User> getUserByEmail(String email) {
        return this.userRepository.findByEmailIgnoreCase(email);
    }

    /**
     * Gets and returns a user by their id
     *
     * @param id the id of the user
     * @return an {@code Optional<>} of the user if it was found
     */
    public Optional<User> getUserById(long id) {
        return this.userRepository.findById(id);
    }

    public Optional<User> getUserByResetPasswordToken(String token) {
        return this.userRepository.findByResetPasswordToken(token);
    }

    /**
     * Returns a boolean for whether the user with the given email exists
     *
     * @param email the email of the user
     * @return boolean if user exists or not
     */
    public boolean existsByEmail(String email) {
        return this.userRepository.existsByEmail(email);
    }

    /**
     * Returns the current logged-in user
     *
     * @return User that is logged it. null if no user is logged
     */
    public User getLoggedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal()
                .equals("anonymousUser")) {
            throw new UsernameNotFoundException("No authenticated user found");
        }

        Object principal = auth.getPrincipal();

        if (principal instanceof DynamicUserDetails) {
            return ((DynamicUserDetails) principal).getFreshUser();
        } else {
            throw new UsernameNotFoundException("Unexpected authentication principal type");
        }
    }

    /**
     * @return Boolean if a user is logged in or not
     */
    public Boolean isLoggedIn() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal()
                .equals("anonymousUser")) {
            return false; // No authenticated user
        }

        return auth.getPrincipal() instanceof DynamicUserDetails;
    }

    /**
     * Updates the selected user
     *
     * @param firstName   first name for update
     * @param lastName    last name for update
     * @param email       email for update
     * @param targetEmail the target user's email
     * @param location    the location object, or pass in null
     */
    @Transactional
    public void updateUser(String firstName, String lastName, String email, String targetEmail,
            Location location) {
        // Get target user
        User targetUser = userRepository.findByEmailIgnoreCase(targetEmail)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User with email " + targetEmail + " does not exist"));
        logger.info("User with email {} exists", targetEmail);
        // Set new values
        targetUser.setEmail(email.toLowerCase());
        targetUser.setFirstName(firstName);
        targetUser.setLastName(lastName);
        if (location != null && !LocationValidator.isLocationEmpty(location)) {
            targetUser.setUserLocation(location);
        } else {
            targetUser.setUserLocation(null);
        }
        userRepository.save(targetUser);
    }

    /**
     * Persists any changes made to the User object in the database
     *
     * @param user a user object to be saved in persistence
     */
    public void saveUser(User user) {
        this.userRepository.save(user);
    }

    /**
     * Updates the currently logged user's role from ROLE_UNVERIFIED to ROLE_USER once they have
     * correctly entered their verification code.
     */
    @Transactional
    public void verifyUser(User user) {
        user.revokeAuthority("ROLE_UNVERIFIED");
        user.grantAuthority("ROLE_USER");
        saveUser(user);
        tokenRepository.deleteByUserId(user.getId());
    }

    /**
     * @param token a token object to be saved in persistence
     */
    public void saveToken(Token token) {
        this.tokenRepository.save(token);
    }

    /**
     * Set up the default users and verify them.
     */
    public List<User> createDefaultUsers(int totalExtraUsers) {
        String password = passwordEncoder.encode("P4$$word");
        User john = new User("john@example.com", password, "John", "Doe");
        User jane = new User("jane@example.com", password, "Jane", "Doe");
        User sarah = new User("sarahandjackthompson@gmail.com", password, "Sarah", "Thompson");
        john.revokeAuthority("ROLE_UNVERIFIED");
        john.grantAuthority("ROLE_USER");
        john = userRepository.save(john); // Ensure john is updated with new id

        jane.revokeAuthority("ROLE_UNVERIFIED");
        jane.grantAuthority("ROLE_USER");
        jane = userRepository.save(jane);

        sarah.revokeAuthority("ROLE_UNVERIFIED");
        sarah.grantAuthority("ROLE_USER");
        sarah = userRepository.save(sarah);

        // add a test profile picture to jane
        try {
            setUserProfileImage(jane);
        } catch (IOException e) {
            logger.warn("IOException while setting Jane's profile image", e);
        }
        List<String> userNames = FileUtilities.readWordsFromResource(USERNAME_PATH);
        for (int i = 0; i < totalExtraUsers; i++) {
            String name = userNames.get(i % userNames.size());
            String capitalized = (name == null || name.isEmpty())
                    ? name
                    : name.substring(0, 1).toUpperCase() + name.substring(1);
            User user = new User("generic" + i + "@example.com", password,
                    capitalized,
                    "");
            user.revokeAuthority("ROLE_UNVERIFIED");
            user.grantAuthority("ROLE_USER");
            userRepository.save(user);
        }
        List<User> users = new ArrayList<>();
        users.add(john);
        users.add(sarah);
        users.add(jane);
        return users;
    }

    /**
     * Helper function modified from ChatGPT for getting a MultiPartFile that is then used for
     * setting test profile pictures for test users NOTE: this is used only for app demonstration
     * purposes
     *
     * @return a MockMultiPartFile that is to be used for setting test profile images
     */
    public MultipartFile getMultipartProfileImage(String path) throws IOException {
        File fileTemp;
        try {
            fileTemp = jarResourceFileLoader.getResourceAsTempFile(path);
        } catch (FileNotFoundException e) {
            fileTemp = new File(path);
        }
        File file = fileTemp;

        if (!file.exists()) {
            throw new IOException("File not found: " + file.getAbsolutePath());
        }

        return FileUtilities.generateMultipartFileFromFile(file);
    }


    /**
     * Overloaded method used for testing purposes only Sets the user's profile picture to the test
     * image found in resources/static/utils
     *
     * @param user user to set image with
     */
    private void setUserProfileImage(User user) throws IOException {
        MultipartFile file;
        try {
            file = getMultipartProfileImage(testUserImageDir.resolve("test-user-pfp.png")
                    .toString());
        } catch (IOException e) {
            logger.error(e.getMessage());
            return;
        }
        String fileName = constructProfileImgFileName("image/png",
                user.getId());
        Path newPath = constructUploadsImgFilePath(fileName);
        logger.info("Attempting to write to new path: {}", newPath);
        Files.deleteIfExists(newPath);
        Files.createFile(newPath);
        Files.write(newPath, file.getBytes());
        setUserImageInfo(user, "image/png", fileName);
        logger.info("Successfully saved profile image for user id: {}", user.getId());
    }

    /**
     * Saves an image into "/uploads/profile-images/" with the file name
     * "user_profile_image_id{userId}". Updates the user's attributes profileImagePath and
     * profileImageFileType. Please note that if there is already an image previously set for the
     * user, it will be overwritten!
     *
     * @param profileImage MultipartFile image of type .png, .jpg or .svg to be saved.
     * @throws IOException If an exception occurs reading from or writing to the system
     */
    public void setUserProfileImage(MultipartFile profileImage) throws IOException {
        User targetUser = getLoggedUser();
        if (targetUser.getProfileImagePath() != null) {
            // get previous filename without /uploads/profile-images/
            String prevFileName = targetUser.getProfileImagePath()
                    .replace("/uploads/profile-images/", "");
            Path previousImagePath = constructUploadsImgFilePath(prevFileName);
            logger.info("prevFileName: {}", prevFileName);
            logger.info("Attempting to delete: {}", previousImagePath);
            Files.deleteIfExists(previousImagePath);
        }
        String fileName = constructProfileImgFileName(profileImage.getContentType(),
                targetUser.getId());

        Path newPath = constructUploadsImgFilePath(fileName);
        logger.info("Attempting to write to new path: {}", newPath);
        Files.deleteIfExists(newPath);
        Files.createFile(newPath);
        Files.write(newPath, profileImage.getBytes());
        setUserImageInfo(targetUser, profileImage.getContentType(), fileName);
        logger.info("Successfully saved profile image for user id: {}", targetUser.getId());
    }

    /**
     * Saves a file to "/uploads/profile-images/" with the file name
     * "user_fail_submission_image_id{userId}. Deletes the previous fail submission image of the
     * user if one exists. Persists the fail submission image file name to the user in the DB
     *
     * @param image Multipart file submitted by the user
     * @throws IOException If an exception occurs reading from or writing to the system
     */
    public void setUserFailSubmissionImage(MultipartFile image) throws IOException {
        User targetUser = getLoggedUser();
        if (targetUser.getFailSubmissionImagePath() != null) {
            String prevFileName = targetUser.getFailSubmissionImagePath()
                    .replace("/uploads/profile-images/", "");
            Path previousImagePath = constructUploadsImgFilePath(prevFileName);
            Files.deleteIfExists(previousImagePath);
        }
        String fileName = constructFailSubmissionImgFileName(image.getContentType(),
                targetUser.getId());

        Path newPath = constructUploadsImgFilePath(fileName);
        Files.deleteIfExists(newPath);
        Files.createFile(newPath);
        Files.write(newPath, image.getBytes());
        targetUser.setFailSubmissionImagePath("/uploads/profile-images/" + fileName);
        targetUser.setFailSubmissionImageOriginalName(image.getOriginalFilename());
        userRepository.save(targetUser);
    }

    /**
     * Sets the given users image file type and image path to the given Strings
     *
     * @param user     the user whose data to set
     * @param fileType the file type to be set
     * @param fileName the name of the image file to be set
     */
    private void setUserImageInfo(User user, String fileType, String fileName) {
        user.setProfileImageFileType(fileType);
        user.setProfileImagePath("/uploads" + "/profile-images/" + fileName);
        userRepository.save(user);
    }

    /**
     * Used by setUserProfileImage and setUserFailSubmissionImage to construct the file path to the
     * user's profile image. This path will be used to save the image to the correct location.
     *
     * @return The complete path to the profile image.
     */
    private Path constructUploadsImgFilePath(String fileName) {
        Path path = Paths.get(
                System.getProperty("user.dir"), uploadsFolder, "profile-images");
        logger.info(fileName);
        logger.info(path.toString());
        if (Files.notExists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                logger.error("Error creating directory: uploads");
            }
        }
        logger.info(path.resolve(fileName).toString());
        return path.resolve(fileName);
    }

    /**
     * Used by setUserProfileImage to construct a standard file name for the user's profile images
     * with their user id and correct extension added.
     *
     * @param fileType use MultipartFile.getContentType() to get the images file type
     * @param userId   the user's id
     * @return filename as a string
     */
    private String constructProfileImgFileName(String fileType, Long userId) {
        String extension = "";
        if (fileType != null) {
            extension = switch (fileType) {
                case "image/jpeg" -> ".jpg";
                case "image/png" -> ".png";
                case "image/svg", "image/svg+xml" -> ".svg";
                default -> extension;
            };
        }
        return "user_profile_image_id" + userId + extension;
    }

    /**
     * Used by setUserFailSubmissionImage to construct a standard file name for the user's images
     * sent by a failed submission with their user id and extention added. If the file mime type is
     * invalid, the extention defaults to .file
     *
     * @param fileType String mime type of the file
     * @param userId   Long id of the user who submitted the file
     * @return String constructed filename
     */
    private String constructFailSubmissionImgFileName(String fileType, Long userId) {
        String extention = "";
        if (fileType != null) {
            extention = switch (fileType) {
                case "image/jpeg" -> ".jpg";
                case "image/png" -> ".png";
                case "image/svg", "image/svg+xml" -> ".svg";
                default -> ".file";
            };
        }
        return "user_fail_submission_image_id" + userId + extention;
    }

    /**
     * Confirms the fail submission image to be the logged-in users profile picture. Moves the
     * content of the image saved to the server when tan invalid edit profile submission was made
     * into a new file corresponding to their profile picture. Sets the user's image info to
     * correspond to the new image in the DB
     */
    public void confirmFailSubmissionImage() {
        User user = getLoggedUser();
        String sourceFileName = user.getFailSubmissionImagePath()
                .replace("/uploads/profile-images/", "");
        Path sourcePath = constructUploadsImgFilePath(sourceFileName);
        String targetFileName = user.getFailSubmissionImagePath()
                .replace("fail_submission", "profile").replace("/uploads/profile-images/", "");
        Path targetPath = constructUploadsImgFilePath(targetFileName);

        try {
            Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            logger.info("Successfully confirmed a fail submission image for user id: {}",
                    user.getId());
        } catch (IOException e) {
            logger.warn(String.valueOf(e));
        }

        String mimeType = switch (FileUtilities.getFileExtensionFromName(targetFileName)) {
            case ".jpg" -> "image/jpeg";
            case ".png" -> "image/png";
            case ".svg" -> "image/svg+xml";
            default -> "application/octet-stream";
        };

        if (mimeType.equals("application/octet-stream")) {
            logger.warn("mime type is application/octet-stream due to extension of {} from {}",
                    FileUtilities.getFileExtensionFromName(targetFileName), targetFileName);
        }
        setUserImageInfo(user, mimeType, targetFileName);
    }

    /**
     * Update the user's password with the new given password
     *
     * @param userId      the users id
     * @param newPassword the new password the user has set
     * @return if valid user id returns updated user else returns null
     */
    public Optional<User> updateUserPassword(long userId, String newPassword) {
        String hashedPassword = passwordEncoder.encode(newPassword);
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            return user;
        } else {
            user.get().setPassword(hashedPassword);
            return Optional.of(userRepository.save(user.get()));
        }
    }

    /**
     * Generates a random reset password token for the given user and persists it in the Database
     *
     * @param user User to generate the token for
     */
    public void generateResetPasswordTokenForUser(User user) {
        String randomToken = ResetPasswordUtilities.generatePasswordResetToken();
        while (getUserByResetPasswordToken(randomToken).isPresent()) {
            randomToken = ResetPasswordUtilities.generatePasswordResetToken();
        }
        user.setResetPasswordToken(randomToken);
        saveUser(user);
    }

    /**
     * Checks that the given token is a valid reset token, if not, adds an error to the HashMap
     *
     * @param errors HashMap to add errors to
     * @param token  String token to validate
     */
    public void validateResetPasswordToken(HashMap<String, String> errors, String token) {
        Optional<User> validToken = getUserByResetPasswordToken(token);
        if (!validToken.isPresent()) {
            errors.put("token", "Reset password link has expired");
        }
    }

    /**
     * Revokes the validity of the given reset password token
     *
     * @param token String token to revoke the validity of
     */
    public void revokeResetPasswordToken(String token) {
        Optional<User> validToken = getUserByResetPasswordToken(token);
        if (validToken.isPresent()) {
            User userToRevoke = validToken.get();
            userToRevoke.setResetPasswordToken(null);
            saveUser(userToRevoke);
        }
    }

    /**
     * Encrypts and changes the password of the given user
     *
     * @param user     User whose password to change
     * @param password String plaintext password to change to
     */
    public void resetPassword(User user, String password) {
        String hashedPassword = passwordEncoder.encode(password);
        user.setPassword(hashedPassword);
        saveUser(user);
    }

    /**
     * Returns a boolean whether the logged-in user owns a given renovation record
     *
     * @param record renovation record to check
     */
    public boolean userOwnsRecord(RenovationRecord record) {
        return Objects.equals(getLoggedUser().getId(), record.getUser().getId());
    }

    public void setUserLocation(User user, Location location) {
        if (!LocationValidator.isLocationEmpty(location)) {
            user.setUserLocation(location);
            saveUser(user);
        }
    }

}
