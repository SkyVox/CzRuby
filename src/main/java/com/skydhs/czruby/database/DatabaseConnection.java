package com.skydhs.czruby.database;

import com.zaxxer.hikari.HikariDataSource;

import java.io.Closeable;

public interface DatabaseConnection extends Closeable {

    /**
     * Get the Database address. This is
     * used to make the main database connection.
     *
     * @return Database host address.
     */
    String host();

    /**
     * Get the Database address port. This
     * port is used to make the main database
     * connection.
     *
     * @return Database port.
     */
    int port();

    /**
     * This is the Database Name.
     * Where the user want to store all those
     * information.
     *
     * This is used to create the main database
     * connection.
     *
     * @return Database name.
     */
    String database();

    /**
     * Get the user name. This is
     * used to create the main database
     * connection.
     *
     * @return User name.
     */
    String username();

    /**
     * Get the user password.
     * This is used to create the main
     * database connection.
     *
     * @return User password.
     */
    String password();

    /**
     * Quick verify if we still has connection
     * with this database.
     *
     * @return is Connected
     */
    boolean isConnected();

    /**
     * This is the Database connection.
     * This may be null if the connection
     * hasn't established yet.
     *
     * @return HikariDataSource Database connection.
     */
    HikariDataSource getHikari();

    /**
     * Creates the database connection.
     * This should only be called if this
     * application hasn't Database Connection.
     */
    boolean connect();
}