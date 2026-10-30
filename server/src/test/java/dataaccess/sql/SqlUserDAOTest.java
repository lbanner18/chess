package dataaccess.sql;

import dataaccess.DataAccessException;
import dataaccess.SqlUserDAO;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import static org.junit.jupiter.api.Assertions.*;

public class SqlUserDAOTest {

    private SqlUserDAO userDAO;

    @BeforeEach
    public void setUp() throws DataAccessException {
        userDAO = new SqlUserDAO();
        userDAO.clear();
    }

    @Test
    @DisplayName("createUser() Positive: Successfully insert user data")
    public void createUserPositive() throws DataAccessException {
        UserData newUser = new UserData("testUsername", "securePassword123", "test@email.com");
        userDAO.createUser(newUser);

        UserData retrievedUser = userDAO.getUser("testUsername");
        assertNotNull(retrievedUser, "UserData should be found in the database");
        assertEquals("testUsername", retrievedUser.username(), "Usernames should match");
        assertTrue(BCrypt.checkpw("securePassword123", retrievedUser.password()), "Password hash should match the original password");
        assertEquals("test@email.com", retrievedUser.email(), "Emails should match");
    }

    @Test
    @DisplayName("createUser() Negative: Throws exception on null username")
    public void createUserNegative() {
        UserData badUser = new UserData(null, "password", "email@test.com");

        assertThrows(DataAccessException.class, () -> {
            userDAO.createUser(badUser);
        }, "Inserting a null username should throw a DataAccessException");
    }

    @Test
    @DisplayName("getUser() Positive: Retrieve existing user")
    public void getUserPositive() throws DataAccessException {
        UserData user = new UserData("existingUser", "pass456", "existing@email.com");
        userDAO.createUser(user);

        UserData found = userDAO.getUser("existingUser");
        assertNotNull(found);
        assertEquals("existingUser", found.username());
    }

    @Test
    @DisplayName("getUser() Negative: Return null for missing user")
    public void getUserNegative() throws DataAccessException {
        UserData found = userDAO.getUser("fakeUser999");
        assertNull(found, "Searching for a non-existent user should return null");
    }

    @Test
    @DisplayName("clear() Positive: Remove all users")
    public void clearPositive() throws DataAccessException {
        userDAO.createUser(new UserData("user1", "pass1", "email1@test.com"));
        userDAO.createUser(new UserData("user2", "pass2", "email2@test.com"));

        userDAO.clear();

        assertNull(userDAO.getUser("user1"));
        assertNull(userDAO.getUser("user2"));
    }
}