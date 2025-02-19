package no.yyz.services;

import org.identityconnectors.common.logging.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class SqlLiteStorage implements AutoCloseable {

    private final Log log;
    private Connection connection;
    private String storageName;
    private String url = "jdbc:sqlite:";

    public SqlLiteStorage(String storageName) {

        log = org.identityconnectors.common.logging.Log.getLog(SqlLiteStorage.class);
        url += storageName +".db";
        try {
            Class.forName("org.sqlite.JDBC");
            log.info("Loaded everything!");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public Connection connect() {
        return connect(url);
    }
    public Connection connect(String url) {
        try {
            if ( connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(url);
            }
            assert(connection != null);
            return connection;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            log.error(e, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws Exception {
       if (connection != null) {
           if (!connection.isClosed()) {
               connection.close();
           }
       }
    }
}
