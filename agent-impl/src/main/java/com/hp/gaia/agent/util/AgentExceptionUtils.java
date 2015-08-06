package com.hp.gaia.agent.util;

import javax.validation.constraints.NotNull;

public class AgentExceptionUtils {

    private AgentExceptionUtils() {
    }

    /**
     * Returns cause of given class if found.
     */
    public static <T extends Throwable> T getCause(@NotNull final Throwable throwable,
                                                   @NotNull final Class<? extends Throwable> throwableType) {
        Throwable current = throwable;
        while (current != null) {
            if (throwableType.isInstance(current)) {
                return (T) current;
            }
            current = current.getCause();
        }
        return null;
    }

}
