package com.sprintly.util;

import com.sprintly.config.HikariCPConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Utility class for obtaining database connections via HikariCP.
 * Provides centralized access to connection pooling.
 */
@Component
public class DBUtil {

    private static final Logger logger = LoggerFactory.getLogger(DBUtil.class);

    private final HikariCPConfig hikariCPConfig;

    @Autowired
    public DBUtil(HikariCPConfig hikariCPConfig) {
        this.hikariCPConfig = hikariCPConfig;
    }

    /**
     * Returns a connection from the HikariCP connection pool.
     * The caller MUST close this connection when done (returns it to pool).
     *
     * @return A JDBC Connection object
     * @throws SQLException if a database access error occurs
     */
    public Connection getConnection() throws SQLException {
        Connection connection = hikariCPConfig.getConnection();
        logger.debug("Database connection acquired from pool");
        return connection;
    }
}
