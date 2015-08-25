package com.hp.gaia.provider.alm;

/**
 * Created by belozovs on 8/25/2015.
 * Bookmark to keep in ALM issue change state, represents the latest auditID for the given project that was successfully sent to ResultsUploader
 */
public class IssueChangeBookmark {

    private int lastAuditId;

    public int getLastAuditId() {
        return lastAuditId;
    }

    public void setLastAuditId(int lastAuditId) {
        this.lastAuditId = lastAuditId;
    }

    @Override
    public String toString() {
        return "IssueChangeBookmark{" + "lastAuditId=" + lastAuditId + '}';
    }

}
