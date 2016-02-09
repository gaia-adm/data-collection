package com.hp.gaia.provider.alm;

import com.hp.gaia.provider.*;

import java.io.IOException;

/**
 * Created by belozovs on 8/23/2015.
 * Datastream for collecting ALM issue change data
 */
public class AlmDataStream implements DataStream {

    private final StateMachine stateMachine;

    public AlmDataStream(StateMachine stateMachine) {

        this.stateMachine = stateMachine;
    }

    @Override
    public boolean isNextReady() {
        return false;
    }

    @Override
    public Bookmarkable next() throws AccessDeniedException {

        return stateMachine.next();
    }

    @Override
    public void close() throws IOException {

        stateMachine.close();
    }
}
