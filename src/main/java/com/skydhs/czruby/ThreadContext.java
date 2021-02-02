package com.skydhs.czruby;

import org.bukkit.Bukkit;

public enum ThreadContext {
    /**
     * Represents the main "server" thread
     */
    SYNC,

    /**
     * Represents anything which isn't the {@link #SYNC} thread.
     */
    ASYNC;

    /**
     * Get ThreadContext for the current Thread.
     *
     * @return If is primary thread {@link #SYNC} otherwise {@link #ASYNC}.
     */
    public static ThreadContext forCurrentThread() {
        return Bukkit.isPrimaryThread() ? SYNC : ASYNC;
    }
}