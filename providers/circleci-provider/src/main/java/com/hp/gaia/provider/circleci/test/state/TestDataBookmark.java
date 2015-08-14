package com.hp.gaia.provider.circleci.test.state;

public class TestDataBookmark {

    private int buildNumber;

    public TestDataBookmark() {
    }

    public TestDataBookmark(final int buildNumber) {
        this.buildNumber = buildNumber;
    }

    public int getBuildNumber() {
        return buildNumber;
    }

    public void setBuildNumber(final int buildNumber) {
        this.buildNumber = buildNumber;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TestDataBookmark{");
        sb.append("buildNumber=").append(buildNumber);
        sb.append('}');
        return sb.toString();
    }
}
