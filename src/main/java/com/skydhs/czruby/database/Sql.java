package com.skydhs.czruby.database;

import com.skydhs.czruby.Core;
import com.skydhs.czruby.ThreadContext;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

import static com.skydhs.czruby.database.Database.getInstance;

public abstract class Sql {
    public final String TABLE;

    public Sql(final @NotNull String table, String tableQuery) {
        this.TABLE = table;
        Database.registerConnection(this);

        // Execute an update for table.
        this.executeUpdate(Objects.requireNonNull(tableQuery, "Table Query cannot be null"));
    }

    /**
     * This method is called when
     * server is attempt to close or
     * database connection is interrupted.
     */
    public abstract void onDisable();

    /**
     * Verify if the given value contains
     * on {@link #TABLE}.
     *
     * @param value Value to search.
     * @param column Database column to search.
     * @return If given value contains on our Database.
     */
    public Boolean contains(final @NotNull String value, @NotNull String column) {
        final String query = "SELECT `" + column + "` FROM `" + TABLE + "` WHERE UPPER(`" + column + "`)='" + value.toUpperCase() + "';";
        ResultSet result = executeQuery(query);

        try {
            return result.next();
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        } finally {
            closeConnection(result);
        }
    }

    /**
     * Delete single value from database.
     *
     * @param value Value to be deleted.
     * @param column Database column to delete.
     * @param context The execution context,
     *                SYNC or ASYNC.
     */
    public void delete(final String value, String column, ThreadContext context) {
        final String query = "DELETE FROM `" + TABLE + "` WHERE `" + column + "`='" + value + "';";

        if (context.equals(ThreadContext.ASYNC)) {
            executeUpdate(query, context);
        } else {
            executeUpdate(query);
        }
    }

    /**
     * Delete multiple values from database.
     *
     * @param values Values to be deleted.
     * @param column Database/MySQL column.
     * @param context if this task should be
     *              executed async or not.
     */
    public void delete(final String[] values, String column, ThreadContext context) {
        if (values == null || values.length <= 0) return;
        StringBuilder builder = new StringBuilder(values.length);

        for (String value : values) {
            if (builder.length() != 0) builder.append(", ");
            builder.append("'").append(value).append("'");
        }

        // Create delete query.
        final String query = "DELETE FROM `" + TABLE + "` WHERE `" + column + "` IN (" + builder.toString() + ");";

        if (context.equals(ThreadContext.ASYNC)) {
            executeUpdate(query, context);
        } else {
            executeUpdate(query);
        }
    }

    /**
     * Executes an update on database,
     * this uses the current connection.
     *
     * @param query Query to be executed.
     */
    public void executeUpdate(final String query) {
        Connection connection = null;
        Statement statement = null;

        try {
            connection = getConnection();
            statement = connection.createStatement();
            statement.executeUpdate(query);
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            closeConnection(connection, statement);
        }
    }

    /**
     * Executes an update on database,
     * this uses the current connection.
     *
     * @param query Query to be executed.
     * @param context The execution context,
     *                SYNC or ASYNC.
     */
    public void executeUpdate(final String query, ThreadContext context) {
        switch (context) {
            case SYNC:
                Bukkit.getScheduler().runTask(Core.getInstance(), () -> executeQuery(query));
                break;
            case ASYNC:
                Bukkit.getScheduler().runTaskAsynchronously(Core.getInstance(), () -> executeUpdate(query));
                break;
        }
    }

    /**
     * Execute query using current
     * connection.
     *
     * @param query Query to be executed.
     * @return statement#executeQuery() or null
     *          if any error was thrown.
     */
    public ResultSet executeQuery(final String query) {
        Connection connection = null;
        Statement statement = null;

        try {
            connection = getConnection();
            statement = connection.createStatement();
            return statement.executeQuery(query);
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            closeConnection(connection, statement);
        }

        return null;
    }

    /**
     * Closes all opened connection after
     * use it.
     *
     * @param closeables Connections to be closed.
     */
    protected static void closeConnection(AutoCloseable... closeables) {
        try {
            for (AutoCloseable closeable : closeables) {
                if (closeable != null) {
                    closeable.close();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public Connection getConnection() throws SQLException {
        return getInstance().getConnection();
    }
}