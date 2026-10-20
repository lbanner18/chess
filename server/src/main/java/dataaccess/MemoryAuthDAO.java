package dataaccess;

import model.AuthData;
import java.util.HashMap;

public class MemoryAuthDAO implements AuthDAO {
    private final HashMap<String, AuthData> authTokens = new HashMap<>();

    @Override
    public void clear() {
        authTokens.clear();
    }
}