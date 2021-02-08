package com.skydhs.czruby.database;

import com.skydhs.czruby.FileUtil;
import com.skydhs.czruby.database.exception.MySQLException;
import com.skydhs.czruby.database.tables.RubyTable;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Database implements DatabaseConnection {
    private static Database instance;

    // ----------
    // Database information.
    // ----------
    private String host, database, username, password;
    private int port;

    // Hikari Connection.
    private HikariDataSource hikari;

    // Field tables.
    private RubyTable rubyTable;

    /*
     * This list below stores all sql connection.
     */
    private final List<Sql> CONNECTIONS = new ArrayList<>(32);

    public static Database from(FileUtil file) {
        FileUtil.FileManager mysqlFile = FileUtil.getFile("config");
        if (!file.getBoolean(mysqlFile, "MySQL.enabled")) return new Database();

        String host = file.getString(mysqlFile, "MySQL.host").asString();
        int port = file.getInt(mysqlFile, "MySQL.port");
        String database = file.getString(mysqlFile, "MySQL.database").asString();
        String username = file.getString(mysqlFile, "MySQL.username").asString();
        String password = file.getString(mysqlFile, "MySQL.password").asString();

        // Create new Object with the given values.
        return new Database(host, port, database, username, password);
    }

    /**
     * Empty constructor.
     */
    private Database() {
        Database.instance = this;
    }

    private Database(String host, int port, String database, String username, String password) {
        Database.instance = this;

        this.host = Objects.requireNonNull(host, "host");
        this.database = Objects.requireNonNull(database, "database");
        this.username = Objects.requireNonNull(username, "username");
        this.port = port;
        this.password = Objects.requireNonNull(password, "password");
    }

    @Override
    public String host() {
        return host;
    }

    @Override
    public int port() {
        return port;
    }

    @Override
    public String database() {
        return database;
    }

    @Override
    public String username() {
        return username;
    }

    @Override
    public String password() {
        return password;
    }

    @Override
    public boolean isConnected() {
        return hikari == null ? Boolean.FALSE : Boolean.TRUE;
    }

    @Override
    public HikariDataSource getHikari() {
        return hikari;
    }

    public RubyTable getRubyTable() {
        return rubyTable;
    }

    /**
     * Get Database Connection.
     *
     * @return Database Connection.
     * @throws SQLException If connection is null.
     * @throws NullPointerException null.
     */
    protected Connection getConnection() throws SQLException, NullPointerException {
        Connection connection = hikari.getConnection();

        if (connection == null || !isConnected() || connection.isClosed()) {
            this.reconnect((byte) 1);
            connection = hikari.getConnection();
        }

        return connection;
    }

    @Override
    public boolean connect() throws MySQLException {
        if (isConnected()) throw new MySQLException("Connection is alive already.");

        if (instance != null) {
            if (hikari == null) {
                this.hikari = new HikariDataSource();
            }

            // Hikari settings.
            hikari.setMaxLifetime(60000);
            hikari.setIdleTimeout(45000);
            hikari.setMaximumPoolSize(16);
            // Setup the Database connection.
            hikari.setDriverClassName("com.mysql.jdbc.Driver");
            hikari.setJdbcUrl("jdbc:mysql://" + host + ':' + port + '/' + database);
            hikari.setUsername(username);
            hikari.setPassword(password);

            // Successfully connected.
            this.setupTables();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void close() {
        if (hikari != null && isConnected()) {
            // Close all connections.
            CONNECTIONS.forEach(Sql::onDisable);
            // Close Hikari connection.
            hikari.close();

            // Clear database connections list.
            CONNECTIONS.clear();
        }
    }

    public boolean reconnect(byte value) {
        if (value == 1) {
            close();
        } else {
            if (isConnected()) return false;
        }

        connect();
        return true;
    }

    private void setupTables() {
        this.rubyTable = new RubyTable();
    }

    /**
     * Register your connection into our list.
     *
     * @param sql SQL to be registered.
     */
    protected static void registerConnection(final Sql sql) {
        if (sql != null && !getInstance().CONNECTIONS.contains(sql)) {
            getInstance().CONNECTIONS.add(sql);
        }
    }

    public static Database getInstance() {
        return instance;
    }
}