package com.hp.gaia.provider.agm;

/**
 * Created by belozovs on 8/25/2015.
 * Bookmark to keep in AGM issue change state, represents the latest auditID for the given project that was successfully sent to ResultsUploader
 */
public class IssueChangeBookmark {

    private int lastAuditId;

    public int getLastAuditId() {
        return lastAuditId;
    }

    public void setLastAuditId(int lastAuditId) {
        if(lastAuditId > this.lastAuditId ){
            this.lastAuditId = lastAuditId;
        }
    }

    @Override
    public String toString() {
        return "IssueChangeBookmark{" + "lastAuditId=" + lastAuditId + '}';
    }

}
