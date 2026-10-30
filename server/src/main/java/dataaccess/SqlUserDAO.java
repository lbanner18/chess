package dataaccess;

import model.UserData;
import java.sql.SQLException;

public class SqlUserDAO implements UserDAO {

    public SqlUserDAO() throws DataAccessException {
        configureDatabase();
    }

    private void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection()) {
            var createTableSql = """
                        CREATE TABLE IF NOT EXISTS userData (
                            username VARCHAR(255) NOT NULL,
                            password VARCHAR(255) NOT NULL,
                            email VARCHAR(255) NOT NULL,
                            PRIMARY KEY (username)
                        )
                    """;
            try (var preparedStatement = conn.prepareStatement(createTableSql)) {
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("Unable to configure database: %s", e.getMessage()));
        }
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {

    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        return null;
    }

    @Override
    public void clear() throws DataAccessException {

    }
}


