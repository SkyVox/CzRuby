package com.skydhs.czruby.database.tables;

import com.skydhs.czruby.ThreadContext;
import com.skydhs.czruby.database.Sql;

public class RubyTable extends Sql {

    public RubyTable() {
        super("vote_daily", "CREATE TABLE IF NOT EXISTS `ruby_store` (`player_name` VARCHAR(16) NOT NULL, PRIMARY KEY(`player_name`));");
    }

    @Override
    public void onDisable() {
    }

    public Boolean contains(final String value) {
        return contains(value, "player_name");
    }

    public void delete(final String value, ThreadContext context) {
        this.delete(value, "player_name", context);
    }
}