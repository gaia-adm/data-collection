package com.hp.gaia.provider.jenkins;

import com.hp.gaia.provider.Data;

public interface State {

    /**
     * Fetches data or returns null if this state was incapable of fetching any test data. If null is returned execution
     * of next {@link State} will continue until there is no more {@link State} to execute.
     *
     * @param stateContext
     */
    Data execute(StateContext stateContext);
}
