package com.hp.gaia.agent.service;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CollectionState {

    public enum State {
        /**
         * Data collection is about to be executed.
         */
        PENDING,
        RUNNING,
        FINISHED
    }

    public enum Result {
        SUCCESS,
        FAILURE
    }

    @JsonProperty("configId")
    private String providerConfigId;

    @JsonProperty("lastCollectionTimestamp")
    private Long lastCollectionTimestamp;

    @JsonProperty("nextCollectionTimestamp")
    private Long nextCollectionTimestamp;

    @JsonProperty("bookmark")
    private String bookmark;

    @JsonProperty("state")
    private State state;

    @JsonProperty("result")
    private Result result;

    public CollectionState() {
    }

    public CollectionState(final String providerConfigId) {
        this.providerConfigId = providerConfigId;
    }

    public String getProviderConfigId() {
        return providerConfigId;
    }

    public void setProviderConfigId(final String providerConfigId) {
        this.providerConfigId = providerConfigId;
    }

    public Long getLastCollectionTimestamp() {
        return lastCollectionTimestamp;
    }

    public void setLastCollectionTimestamp(final Long lastCollectionTimestamp) {
        this.lastCollectionTimestamp = lastCollectionTimestamp;
    }

    public Long getNextCollectionTimestamp() {
        return nextCollectionTimestamp;
    }

    public void setNextCollectionTimestamp(final Long nextCollectionTimestamp) {
        this.nextCollectionTimestamp = nextCollectionTimestamp;
    }

    public String getBookmark() {
        return bookmark;
    }

    public void setBookmark(final String bookmark) {
        this.bookmark = bookmark;
    }

    public State getState() {
        return state;
    }

    public void setState(final State state) {
        this.state = state;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(final Result result) {
        this.result = result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CollectionState{");
        sb.append("providerConfigId='").append(providerConfigId).append('\'');
        sb.append(", lastCollectionTimestamp=").append(lastCollectionTimestamp);
        sb.append(", nextCollectionTimestamp=").append(nextCollectionTimestamp);
        sb.append(", bookmark='").append(bookmark).append('\'');
        sb.append(", state=").append(state);
        sb.append(", result=").append(result);
        sb.append('}');
        return sb.toString();
    }
}
