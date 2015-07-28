package com.hp.gaia.agent.service;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CollectionState implements Cloneable {

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
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CollectionState)) {
            return false;
        }

        final CollectionState that = (CollectionState) o;

        if (providerConfigId != null ?
                !providerConfigId.equals(that.providerConfigId) :
                that.providerConfigId != null) {
            return false;
        }
        if (lastCollectionTimestamp != null ?
                !lastCollectionTimestamp.equals(that.lastCollectionTimestamp) :
                that.lastCollectionTimestamp != null) {
            return false;
        }
        if (nextCollectionTimestamp != null ?
                !nextCollectionTimestamp.equals(that.nextCollectionTimestamp) :
                that.nextCollectionTimestamp != null) {
            return false;
        }
        if (bookmark != null ? !bookmark.equals(that.bookmark) : that.bookmark != null) {
            return false;
        }
        if (state != that.state) {
            return false;
        }
        return result == that.result;

    }

    @Override
    public int hashCode() {
        int result1 = providerConfigId != null ? providerConfigId.hashCode() : 0;
        result1 = 31 * result1 + (lastCollectionTimestamp != null ? lastCollectionTimestamp.hashCode() : 0);
        result1 = 31 * result1 + (nextCollectionTimestamp != null ? nextCollectionTimestamp.hashCode() : 0);
        result1 = 31 * result1 + (bookmark != null ? bookmark.hashCode() : 0);
        result1 = 31 * result1 + (state != null ? state.hashCode() : 0);
        result1 = 31 * result1 + (result != null ? result.hashCode() : 0);
        return result1;
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
