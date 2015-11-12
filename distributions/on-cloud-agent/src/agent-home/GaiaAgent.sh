#!/bin/bash

if [ -z "$GAIA_AGENT_HOME" ];then
  export GAIA_AGENT_HOME=`pwd`
fi

echo Using $GAIA_AGENT_HOME as GAIA_AGENT_HOME

java -jar $GAIA_AGENT_HOME/lib/gaia-agent.jar
