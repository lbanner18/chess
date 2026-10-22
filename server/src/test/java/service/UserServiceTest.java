package service;

import dataaccess.DataAccessException;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryUserDAO;
import model.RegisterRequest;
import model.RegisterResult;
import model.LoginRequest;
import model.LoginResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {
    private UserService userService;
    private MemoryUserDAO userDAO;
    private MemoryAuthDAO authDAO;

    @BeforeEach
    public void setup() {
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        userService = new UserService(userDAO, authDAO);
    }

    @Test
    public void registerSuccess() throws DataAccessException {
        RegisterRequest request = new RegisterRequest("Bear", "password123", "luke@email.com");
        RegisterResult result = userService.register(request);

        assertEquals("Bear", result.username());
        assertNotNull(result.authToken());

        assertNotNull(userDAO.getUser("Bear"));
    }

    @Test
    public void registerDuplicateUserFails() throws DataAccessException {

        RegisterRequest request = new RegisterRequest("Luke", "password123", "luke@email.com");
        userService.register(request);

        RegisterRequest duplicateRequest = new RegisterRequest("Luke", "newpassword", "new@email.com");

        assertThrows(DataAccessException.class, () -> userService.register(duplicateRequest));
    }

    @Test
    public void loginSuccess() throws DataAccessException {
        userService.register(new RegisterRequest("Luke", "password123", "luke@email.com"));

        LoginRequest loginRequest = new LoginRequest("Luke", "password123");
        LoginResult result = userService.login(loginRequest);

        assertNotNull(result.authToken());
        assertEquals("Luke", result.username());
    }

    @Test
    public void loginWrongPasswordFails() throws DataAccessException {
        userService.register(new RegisterRequest("Luke", "password123", "luke@email.com"));

        LoginRequest badLogin = new LoginRequest("Luke", "wrongpassword");

        assertThrows(DataAccessException.class, () -> userService.login(badLogin));
    }

    @Test
    public void logoutSuccess() throws DataAccessException {
        RegisterResult registerResult = userService.register(new RegisterRequest("Luke", "password123", "luke@email.com"));
        String token = registerResult.authToken();

        userService.logout(token);

        assertNull(authDAO.getAuth(token));
    }

    @Test
    public void logoutInvalidTokenFails() {
        assertThrows(DataAccessException.class, () -> userService.logout("fake-made-up-token"));
    }

    @Test
    public void registerBadRequestFails() {
        RegisterRequest badRequest = new RegisterRequest(null, "password123", "luke@email.com");

        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            userService.register(badRequest);
        });
        assertEquals("Error: bad request", exception.getMessage());
    }
}