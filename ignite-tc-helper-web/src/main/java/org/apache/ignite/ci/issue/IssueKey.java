package org.apache.ignite.ci.issue;

import com.google.common.base.MoreObjects;

public class IssueKey {
    public String server;
    public Integer buildId;
    public String testOrBuildName;

    public IssueKey(String server, Integer buildId, String testOrBuildName) {
        this.server = server;
        this.buildId = buildId;
        this.testOrBuildName = testOrBuildName;
    }

    public String getServer() {
        return server;
    }

    public Integer getBuildId() {
        return buildId;
    }

    public String getTestOrBuildName() {
        return testOrBuildName;
    }

    @Override public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("server", server)
            .add("buildId", buildId)
            .add("testOrBuildName", testOrBuildName)
            .toString();
    }
}
