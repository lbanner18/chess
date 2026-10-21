package service;

import dataaccess.DataAccessException;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryUserDAO;
import model.RegisterRequest;
import model.RegisterResult;
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
        RegisterRequest request = new RegisterRequest("Luke", "password123", "luke@email.com");
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
}