package com.skydhs.czruby.integration;

public interface IntegrationInterface<T> {

    /**
     * Get the main class for
     * a specific plugin.
     *
     * @return main class.
     */
    T getMain();
}