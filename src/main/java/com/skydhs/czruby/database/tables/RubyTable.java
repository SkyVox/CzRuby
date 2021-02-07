package com.skydhs.czruby.database.tables;

import com.skydhs.czruby.ThreadContext;
import com.skydhs.czruby.database.Sql;
import com.skydhs.czruby.manager.entity.Ruby;
import org.jetbrains.annotations.NotNull;

import java.sql.*;

public class RubyTable extends Sql {

    public RubyTable() {
        super("vote_daily", "CREATE TABLE IF NOT EXISTS `ruby_store` (`player_name` VARCHAR(16) NOT NULL, `fragments` INT, `rubies` INT, `log` JSON, PRIMARY KEY(`player_name`));");
    }

    @Override
    public void onDisable() {
        Ruby.getRubiesCache().values().forEach(this::update);
        Ruby.getRubiesCache().clear();
    }

    public Boolean contains(final String value) {
        return contains(value, "player_name");
    }

    public void delete(final String value, ThreadContext context) {
        this.delete(value, "player_name", context);
    }

    public void update(@NotNull Ruby ruby) {
        Connection connection = null;
        Statement statement = null;

        final String log = ruby.getLog().toString(); // TODO Convert as JSON..
        final String query = "INSERT INTO `" + TABLE + "` (`player_name`, `fragments`, `rubies`, `log`) VALUE (" +
                "'" + ruby.getPlayerName() + "', " +
                "'" + ruby.getFragments() + "', " +
                "'" + ruby.getRubies() + "', " +
                "'" + log + "')" +
                " ON DUPLICATE KEY " +
                "UPDATE " +
                "`fragments`='" + ruby.getFragments() + "', " +
                "`rubies`='" + ruby.getRubies() + "', " +
                "`log`='" + log + "';";

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
     * Get new Ruby object for the given playerName.
     *
     * @param playerName Player Name to search.
     * @param online Whether this player is online or not.
     * @return New Ruby Object.
     *          This might return a null value if player
     *          isn't registered on our database yet.
     */
    public Ruby getPlayerRuby(@NotNull final String playerName, boolean online) {
        Ruby ret = null;

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet result = null;
        final String query = "SELECT * FROM `" + TABLE + "` WHERE `player_name`='" + playerName + "';";

        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(query);
            result = preparedStatement.executeQuery();

            if (result.next()) {
                long fragments = result.getLong("fragments");
                long rubies = result.getLong("rubies");
                String log = result.getString("log");

                // TODO, Load this log.

                ret = new Ruby(playerName, fragments, rubies, online);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            closeConnection(connection, preparedStatement, result);
        }

        return ret;
    }
}