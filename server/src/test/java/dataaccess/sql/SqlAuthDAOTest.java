package dataaccess.sql;

import dataaccess.DataAccessException;
import model.AuthData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SqlAuthDAOTest {

    private SqlAuthDAO authDAO;

    @BeforeEach
    public void setUp() throws DataAccessException {
        authDAO = new SqlAuthDAO();
        authDAO.clear();
    }

    @Test
    @DisplayName("createAuth() Positive: Successfully insert auth data")
    public void createAuthPositive() throws DataAccessException {
        AuthData newAuth = new AuthData("test-token-123", "testUser");
        authDAO.createAuth(newAuth);

        AuthData retrievedAuth = authDAO.getAuth("test-token-123");
        assertNotNull(retrievedAuth, "AuthData should be found in the database");
        assertEquals("testUser", retrievedAuth.username(), "Usernames should match");
    }

    @Test
    @DisplayName("createAuth() Negative: Throws exception on null token")
    public void createAuthNegative() {
        AuthData badAuth = new AuthData(null, "testUser");

        assertThrows(DataAccessException.class, () -> {
            authDAO.createAuth(badAuth);
        }, "Inserting a null token should throw a DataAccessException");
    }

    @Test
    @DisplayName("getAuth() Positive: Retrieve existing auth")
    public void getAuthPositive() throws DataAccessException {
        AuthData auth = new AuthData("token-456", "existingUser");
        authDAO.createAuth(auth);

        AuthData found = authDAO.getAuth("token-456");
        assertNotNull(found);
        assertEquals("token-456", found.authToken());
    }

    @Test
    @DisplayName("getAuth() Negative: Return null for missing token")
    public void getAuthNegative() throws DataAccessException {
        AuthData found = authDAO.getAuth("fake-token-999");
        assertNull(found, "Searching for a non-existent token should return null");
    }

    @Test
    @DisplayName("deleteAuth() Positive: Remove an existing token")
    public void deleteAuthPositive() throws DataAccessException {
        AuthData auth = new AuthData("token-to-delete", "doomedUser");
        authDAO.createAuth(auth);

        authDAO.deleteAuth("token-to-delete");

        assertNull(authDAO.getAuth("token-to-delete"), "Token should no longer exist after deletion");
    }

    @Test
    @DisplayName("clear() Positive: Remove all tokens")
    public void clearPositive() throws DataAccessException {
        authDAO.createAuth(new AuthData("token-1", "user1"));
        authDAO.createAuth(new AuthData("token-2", "user2"));

        authDAO.clear();

        assertNull(authDAO.getAuth("token-1"));
        assertNull(authDAO.getAuth("token-2"));
    }

    @Test
    @DisplayName("createAuth() Negative: Throws exception on duplicate auth token")
    public void createAuthDuplicate() throws DataAccessException {
        AuthData auth = new AuthData("duplicate-token-123", "testUser");
        authDAO.createAuth(auth);

        assertThrows(DataAccessException.class, () -> {
            authDAO.createAuth(auth);
        }, "Inserting a duplicate auth token should throw a DataAccessException");
    }

    @Test
    @DisplayName("getAuth() Negative: Throws exception on database error")
    public void getAuthException() throws Exception {
        try (var conn = dataaccess.DatabaseManager.getConnection();
             var ps = conn.prepareStatement("DROP TABLE authData")) {
            ps.executeUpdate();
        }

        assertThrows(DataAccessException.class, () -> {
            authDAO.getAuth("testToken");
        });
    }
}