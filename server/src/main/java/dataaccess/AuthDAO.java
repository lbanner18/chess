package dataaccess;

import model.AuthData;

public interface AuthDAO {
    void clear() throws DataAccessException;
    AuthData getAuth(String authToken) throws DataAccessException;
    void createAuth(AuthData auth) throws DataAccessException;
}