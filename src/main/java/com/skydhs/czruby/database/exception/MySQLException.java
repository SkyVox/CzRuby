package com.skydhs.czruby.database.exception;

public class MySQLException extends RuntimeException {

    public MySQLException() {
    }

    public MySQLException(String message) {
        super(message);
    }

    public MySQLException(String message, Throwable cause) {
        super(message, cause);
    }

    public MySQLException(Throwable cause) {
        super(cause);
    }
}