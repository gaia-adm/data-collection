package com.hp.gaia.provider.jenkins.test.state;

import com.hp.gaia.provider.Bookmarkable;

public interface State {

    /**
     * Fetches data or returns null if this state was incapable of fetching any test data. If null is returned execution
     * of next {@link State} will continue until there is no more {@link State} to execute.
     *
     * @param stateContext
     */
    Bookmarkable execute(StateContext stateContext);
}
