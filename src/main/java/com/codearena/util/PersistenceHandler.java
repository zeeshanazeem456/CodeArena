package com.codearena.util;

import java.sql.SQLException;

public final class PersistenceHandler {

    private PersistenceHandler() {
    }

    public static void initialize() throws SQLException {
        DBConnection.getConnection();
        SchemaInitializer.run();
        SeedInitializer.run();
    }

    public static void shutdown() throws SQLException {
        DBConnection.closeConnection();
    }
}
